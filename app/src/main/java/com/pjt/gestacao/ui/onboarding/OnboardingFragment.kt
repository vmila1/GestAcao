package com.pjt.gestacao.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.pjt.gestacao.FirebaseUtils
import com.pjt.gestacao.R

class OnboardingFragment : Fragment() {

    private lateinit var etMeses: TextInputEditText // MODIFICADO
    private lateinit var rgGenero: RadioGroup
    private lateinit var btnConcluir: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etMeses = view.findViewById(R.id.etMeses) // MODIFICADO
        rgGenero = view.findViewById(R.id.rgGenero)
        btnConcluir = view.findViewById(R.id.btnConcluirOnboarding)
        progressBar = view.findViewById(R.id.onboardingProgressBar)

        btnConcluir.setOnClickListener {
            salvarDados()
        }
    }

    private fun salvarDados() {
        val mesesTexto = etMeses.text.toString() // MODIFICADO

        if (mesesTexto.isBlank()) {
            etMeses.error = "Campo obrigatório"
            return
        }

        val meses = mesesTexto.toInt() // MODIFICADO
        val generoSelecionado = when (rgGenero.checkedRadioButtonId) {
            R.id.rbMenina -> "Menina"
            R.id.rbMenino -> "Masculino" // Corrigido para corresponder ao seu banco
            else -> "Não informado"
        }

        setLoading(true)

        val latitude = 0.0
        val longitude = 0.0

        FirebaseUtils.salvarDadosGestante(
            meses = meses,
            generoBebe = generoSelecionado,
            latitude = latitude,
            longitude = longitude,
            onSuccess = {
                Toast.makeText(requireContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_onboardingFragment_to_navigation_home)
            },
            onFailure = { e ->
                setLoading(false)
                Toast.makeText(requireContext(), "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnConcluir.isEnabled = !isLoading
    }
}