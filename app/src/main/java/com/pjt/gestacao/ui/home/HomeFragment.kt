package com.pjt.gestacao.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pjt.gestacao.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Exemplo: atualizando o TextView com o valor do ViewModel
        val gestationTextView: TextView = binding.textView
        homeViewModel.text.observe(viewLifecycleOwner) {
            gestationTextView.text = it
        }

        // Exemplo: clicando no botão
        binding.button.setOnClickListener {
            // Aqui você pode executar alguma ação
            // Por exemplo: mostrar uma mensagem ou navegar
            homeViewModel.updateText("8 Meses")
        }

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
