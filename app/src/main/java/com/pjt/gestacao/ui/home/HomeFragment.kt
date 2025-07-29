package com.pjt.gestacao.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Timestamp
import com.pjt.gestacao.R
import com.pjt.gestacao.model.Place
import com.pjt.gestacao.ui.UserViewModel
import com.pjt.gestacao.ui.mapa.MapViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.min

class HomeFragment : Fragment(), OnMapReadyCallback {

    private val userViewModel: UserViewModel by activityViewModels()
    private val mapViewModel: MapViewModel by viewModels()

    private lateinit var tvMeses: TextView
    private lateinit var tvMensagem: TextView
    private lateinit var etDuvida: EditText
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvMeses = view.findViewById(R.id.tvMeses)
        tvMensagem = view.findViewById(R.id.tvMensagem)
        etDuvida = view.findViewById(R.id.etDuvida)
        mapView = view.findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this) // Define este fragmento como o callback

        setupButtons(view)
        userViewModel.loadGestanteData()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMapListeners()
        setupObservers()
    }

    private fun setupObservers() {
        userViewModel.gestanteData.observe(viewLifecycleOwner) { dados ->
            if (dados != null) {
                updateUI(dados)
            } else {
                Toast.makeText(requireContext(), "Dados da gestante não encontrados.", Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mapViewModel.uiState.collectLatest { mapState ->
                mapState.userLocation?.let { userLocation ->
                    if (mapState.nearbyPlaces.isNotEmpty()) {
                        updateMapWithPlaces(userLocation, mapState.nearbyPlaces)
                    } else if (!mapState.isSearching) {
                        mapViewModel.onFilterChanged("Todos")
                        mapViewModel.onSearchQueryChanged("hospital maternidade posto de saude ONG de apoio para gestantes")
                    }
                }
            }
        }
    }

    private fun updateMapWithPlaces(userLocation: LatLng, places: List<Place>) {
        googleMap?.let { map ->
            map.clear()
            map.uiSettings.setAllGesturesEnabled(false)
            map.uiSettings.isMapToolbarEnabled = false

            // Adiciona os marcadores com o pino vermelho padrão
            places.take(10).forEach { place ->
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(place.latitude, place.longitude))
                        .title(place.name) // O título que aparece ao clicar
                )
            }

            // Move a câmera para a localização da usuária com um zoom mais próximo
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 11f))
        }
    }

    private fun setupMapListeners() {
        // Listener para o clique na área do mapa
        view?.findViewById<View>(R.id.layoutMap)?.setOnClickListener {
            navigateToFullMap()
        }

        // Listener para o clique na janela de informações do pino
        googleMap?.setOnInfoWindowClickListener {
            navigateToFullMap()
        }
    }

    /**
     * Navega para a tela de mapa simulando um clique no item da BottomNavigationView.
     */
    private fun navigateToFullMap() {
        try {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNav.selectedItemId = R.id.navigation_mapa
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Não foi possível abrir o mapa.", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateUI(dados: Map<String, Any>) {
        val mesInicial = dados["mesGestacaoInicial"] as? Long ?: 0
        val dataCadastroTimestamp = dados["dataDoCadastro"] as? Timestamp
        var mesAtual = mesInicial

        if (dataCadastroTimestamp != null) {
            val dataCadastro = Calendar.getInstance().apply { time = dataCadastroTimestamp.toDate() }
            val hoje = Calendar.getInstance()

            var anosPassados = hoje.get(Calendar.YEAR) - dataCadastro.get(Calendar.YEAR)
            var mesesPassados = hoje.get(Calendar.MONTH) - dataCadastro.get(Calendar.MONTH)

            if (hoje.get(Calendar.DAY_OF_MONTH) < dataCadastro.get(Calendar.DAY_OF_MONTH)) {
                mesesPassados--
            }

            val totalMesesPassados = (anosPassados * 12) + mesesPassados

            if (totalMesesPassados > 0) {
                mesAtual += totalMesesPassados
            }
        }

        val mesFinal = min(mesAtual, 9L)

        tvMeses.text = "$mesFinal meses"
        tvMensagem.text = when (mesFinal) {
            1L, 2L, 3L -> "Cuidados do primeiro trimestre!"
            4L, 5L, 6L -> "Aproveite o segundo trimestre!"
            7L, 8L, 9L -> "Reta final! Prepare-se para a chegada!"
            else -> "Bem-vinda a esta jornada especial!"
        }
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.btnSaibaMais).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_info)
        }

        view.findViewById<Button>(R.id.btnEnviar).setOnClickListener {
            val question = etDuvida.text.toString().trim()
            if (question.isNotEmpty()) {
                navigateToChatWithQuestion(question)
            } else {
                Toast.makeText(requireContext(), "Digite sua dúvida", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Botões de Módulos (Seu código) ---
        view.findViewById<ImageButton>(R.id.btnAlimentacao).setOnClickListener {
            navigateToChatWithQuestion("Me fale sobre alimentação na gravidez")
        }
        view.findViewById<ImageButton>(R.id.btnExames).setOnClickListener {
            navigateToChatWithQuestion("Quais são os principais exames do pré-natal?")
        }
        view.findViewById<ImageButton>(R.id.btnMudancasCorpo).setOnClickListener {
            navigateToChatWithQuestion("Quais são as principais mudanças no corpo durante a gestação?")
        }
        view.findViewById<ImageButton>(R.id.btnParto).setOnClickListener {
            navigateToChatWithQuestion("Quais são os sinais de que o trabalho de parto está começando?")
        }

    }

    private fun navigateToChatWithQuestion(question: String) {
        val bundle = bundleOf("question" to question)
        findNavController().navigate(R.id.action_home_to_chat, bundle)
    }

    // Gerenciamento do ciclo de vida do MapView e do Fragment
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (::etDuvida.isInitialized) {
            etDuvida.setText("")
        }
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}