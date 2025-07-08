package com.pjt.gestacao.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.pjt.gestacao.MainActivity
import com.pjt.gestacao.R
import com.pjt.gestacao.ui.UserViewModel

class HomeFragment : Fragment() {

    // Obtém a instância do ViewModel compartilhado pela MainActivity
    private val userViewModel: UserViewModel by activityViewModels()

    // Referências para os elementos da UI
    private lateinit var tvMeses: TextView
    private lateinit var tvMensagem: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa as Views
        tvMeses = view.findViewById(R.id.tvMeses)
        tvMensagem = view.findViewById(R.id.tvMensagem)

        setupButtons(view)
        setupMap(view, savedInstanceState)

        // Configura os observadores para reagir às mudanças no ViewModel
        setupObservers()

        // Pede ao ViewModel para carregar os dados da gestante.
        // Isso só será chamado quando a Home for exibida, ou seja, o usuário já está logado.
        userViewModel.loadGestanteData()
    }

    private fun setupObservers() {
        // 1. Observa o estado de carregamento para mostrar/esconder o ProgressBar
        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            (activity as? MainActivity)?.setProgressBar(isLoading)
        }

        // 2. Observa os dados da gestante
        userViewModel.gestanteData.observe(viewLifecycleOwner) { dados ->
            if (dados != null) {
                // Se os dados existem, atualiza a UI
                updateUI(dados)
            } else {
                // Isso não deveria acontecer nesse fluxo, mas é uma segurança.
                // Indica que o usuário está logado mas não tem dados no Firestore.
                Toast.makeText(requireContext(), "Dados da gestante não encontrados.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(dados: Map<String, Any>) {
        val meses = dados["mesGestacao"] as? Long ?: 0

        tvMeses.text = "$meses meses"

        tvMensagem.text = when (meses) {
            1L, 2L, 3L -> "Cuidados do primeiro trimestre!"
            4L, 5L, 6L -> "Aproveite o segundo trimestre!"
            7L, 8L, 9L -> "Reta final! Prepare-se para a chegada!"
            else -> "Bem-vinda a esta jornada especial!"
        }
        // Lógicas aqui para o mapa, etc.
    }

    private fun setupButtons(view: View) {
        val botaoMaisInfo: Button = view.findViewById(R.id.btnSaibaMais)
        botaoMaisInfo.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_dashboard)
        }
        val botao1: ImageButton = view.findViewById(R.id.imageButton)
        val botao2: ImageButton = view.findViewById(R.id.imageButton6)
        val botao3: ImageButton = view.findViewById(R.id.imageButton4)
        val botao4: ImageButton = view.findViewById(R.id.imageButton5)
        val botao5: ImageButton = view.findViewById(R.id.imageButton7)

        botao1.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 1 clicado!", Toast.LENGTH_SHORT).show()
        }

        botao2.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 2 clicado!", Toast.LENGTH_SHORT).show()
        }

        botao3.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 3 clicado!", Toast.LENGTH_SHORT).show()
        }

        botao4.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 4 clicado!", Toast.LENGTH_SHORT).show()
        }

        botao5.setOnClickListener {
            Toast.makeText(requireContext(), "Botão 5 clicado!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMap(view: View, savedInstanceState: Bundle?) {
        val mapView: MapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-8.0476, -34.8770), 12f))
        }
    }
}