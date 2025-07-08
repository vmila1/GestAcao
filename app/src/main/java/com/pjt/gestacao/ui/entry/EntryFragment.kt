package com.pjt.gestacao.ui.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.pjt.gestacao.R
import com.pjt.gestacao.ui.UserState
import com.pjt.gestacao.ui.UserViewModel

class EntryFragment : Fragment() {

    // Usa 'activityViewModels' para compartilhar o ViewModel com a MainActivity e outros fragments
    private val userViewModel: UserViewModel by activityViewModels()

    private lateinit var btnEntrar: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnEntrar = view.findViewById(R.id.btnEntrar)
        progressBar = view.findViewById(R.id.progressBar)

        btnEntrar.setOnClickListener {
            // Desabilita o botão e inicia a verificação
            setLoading(true)
            userViewModel.checkUserStatus()
        }

        // Observa o estado do usuário vindo do ViewModel
        userViewModel.userState.observe(viewLifecycleOwner) { state ->
            // Só reage se o estado não for o inicial ou autenticando
            if (state != UserState.UNKNOWN && state != UserState.AUTHENTICATING) {
                setLoading(false)
            }

            when (state) {
                UserState.LOGGED_IN -> {
                    // Usuário logado e com dados, vai para a Home
                    findNavController().navigate(R.id.action_entryFragment_to_navigation_home)
                }
                UserState.NEEDS_ONBOARDING -> {
                    // Usuário logado mas sem dados, vai para o Onboarding
                    findNavController().navigate(R.id.action_entryFragment_to_onboardingFragment) // Crie esta ação
                }
                UserState.AUTH_ERROR -> {
                    // Erro de autenticação ou de rede
                    Toast.makeText(requireContext(), "Erro de conexão. Tente novamente.", Toast.LENGTH_SHORT).show()
                }
                else -> { /* Não faz nada nos estados UNKNOWN ou AUTHENTICATING */ }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            btnEntrar.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            btnEntrar.visibility = View.VISIBLE
        }
    }
}