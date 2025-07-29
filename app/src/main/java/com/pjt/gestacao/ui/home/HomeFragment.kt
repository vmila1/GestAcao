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
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.pjt.gestacao.R
import com.pjt.gestacao.ui.UserViewModel
import java.util.Calendar
import kotlin.math.min

class HomeFragment : Fragment() {

    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var tvMeses: TextView
    private lateinit var tvMensagem: TextView
    private lateinit var etDuvida: EditText

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

        setupButtons(view)
        setupMap(view, savedInstanceState)

        setupObservers()

        userViewModel.loadGestanteData()
    }

    private fun setupObservers() {
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
        val mesInicial = dados["mesGestacaoInicial"] as? Long ?: 0
        val dataCadastroTimestamp = dados["dataDoCadastro"] as? Timestamp

        var mesAtual = mesInicial

        if (dataCadastroTimestamp != null) {
            val dataCadastro = Calendar.getInstance()
            dataCadastro.time = dataCadastroTimestamp.toDate()

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
        val botaoMaisInfo: Button = view.findViewById(R.id.btnSaibaMais)
        botaoMaisInfo.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_info)
        }

        val btnEnviar: Button = view.findViewById(R.id.btnEnviar)
        btnEnviar.setOnClickListener {
            val question = etDuvida.text.toString().trim()
            if (question.isNotEmpty()) {
                navigateToChatWithQuestion(question)
            } else {
                Toast.makeText(requireContext(), "Digite sua dúvida", Toast.LENGTH_SHORT).show()
            }
        }

        val btnAlimentacao: ImageButton = view.findViewById(R.id.btnAlimentacao)
        val btnExames: ImageButton = view.findViewById(R.id.btnExames)
        val btnMudancasCorpo: ImageButton = view.findViewById(R.id.btnMudancasCorpo)
        val btnParto: ImageButton = view.findViewById(R.id.btnParto)

        btnAlimentacao.setOnClickListener {
            navigateToChatWithQuestion("Me fale sobre alimentação na gravidez")
        }

        btnExames.setOnClickListener {
            navigateToChatWithQuestion("Quais são os principais exames do pré-natal?")
        }

        btnMudancasCorpo.setOnClickListener {
            navigateToChatWithQuestion("Quais são as principais mudanças no corpo durante a gestação?")
        }

        btnParto.setOnClickListener {
            navigateToChatWithQuestion("Quais são os sinais de que o trabalho de parto está começando?")
        }
    }

    private fun navigateToChatWithQuestion(question: String) {
        val bundle = bundleOf("question" to question)
        findNavController().navigate(R.id.action_home_to_chat, bundle)
    }

    private fun setupMap(view: View, savedInstanceState: Bundle?) {
        val mapView: MapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-8.0476, -34.8770), 12f))
        }
    }

    override fun onResume() {
        super.onResume()
        if (::etDuvida.isInitialized) {
            etDuvida.setText("")
        }
    }
}