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
import com.google.firebase.Timestamp
import java.util.Calendar
import kotlin.math.min

class HomeFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()

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

        tvMeses = view.findViewById(R.id.tvMeses)
        tvMensagem = view.findViewById(R.id.tvMensagem)

        setupButtons(view)
        setupMap(view, savedInstanceState)

        setupObservers()

        userViewModel.loadGestanteData()
    }

    private fun setupObservers() {
        // Observa os dados da gestante
        userViewModel.gestanteData.observe(viewLifecycleOwner) { dados ->
            if (dados != null) {
                updateUI(dados)
            } else {
                Toast.makeText(requireContext(), "Dados da gestante não encontrados.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(dados: Map<String, Any>) {
        // Pega os dados salvos no Firebase
        val mesInicial = dados["mesGestacaoInicial"] as? Long ?: 0
        val dataCadastroTimestamp = dados["dataDoCadastro"] as? Timestamp

        var mesAtual = mesInicial

        if (dataCadastroTimestamp != null) {
            // Converte a data do Firebase para um objeto Calendar
            val dataCadastro = Calendar.getInstance()
            dataCadastro.time = dataCadastroTimestamp.toDate()

            // Pega a data atual
            val hoje = Calendar.getInstance()

            // Calcula a diferença de anos e meses
            var anosPassados = hoje.get(Calendar.YEAR) - dataCadastro.get(Calendar.YEAR)
            var mesesPassados = hoje.get(Calendar.MONTH) - dataCadastro.get(Calendar.MONTH)

            // Se o dia atual for menor que o dia do cadastro, o mês atual não "completou"
            if (hoje.get(Calendar.DAY_OF_MONTH) < dataCadastro.get(Calendar.DAY_OF_MONTH)) {
                mesesPassados--
            }

            // Converte os anos em meses e soma tudo
            val totalMesesPassados = (anosPassados * 12) + mesesPassados

            // Soma os meses passados ao mês inicial
            if (totalMesesPassados > 0) {
                mesAtual += totalMesesPassados
            }
        }

        // Garante que o mês de gestação não passe de 9
        val mesFinal = min(mesAtual, 9L)

        // Atualiza a UI com o mês calculado
        tvMeses.text = "$mesFinal meses"

        tvMensagem.text = when (mesFinal) {
            1L, 2L, 3L -> "Cuidados do primeiro trimestre!"
            4L, 5L, 6L -> "Aproveite o segundo trimestre!"
            7L, 8L, 9L -> "Reta final! Prepare-se para a chegada!"
            else -> "Bem-vinda a esta jornada especial!"
        }
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