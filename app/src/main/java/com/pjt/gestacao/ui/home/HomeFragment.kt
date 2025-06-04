package com.pjt.gestacao.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.pjt.gestacao.R

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar o layout que você colou acima
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Move the following code INSIDE this onViewCreated function
        val botao1: ImageButton = view.findViewById(R.id.imageButton)
        val botao2: ImageButton = view.findViewById(R.id.imageButton6)
        val botao3: ImageButton = view.findViewById(R.id.imageButton4)
        val botao4: ImageButton = view.findViewById(R.id.imageButton5)
        val botao5: ImageButton = view.findViewById(R.id.imageButton7)
        // Assuming 'Button' was a typo and you meant ImageButton or you have a Button with this ID.
        // If it's a regular Button, ensure you import android.widget.Button
        val botaoMaisInfo: Button = view.findViewById(R.id.btnSaibaMais) // Or android.widget.Button

        botaoMaisInfo.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_dashboard)
        }

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

        // Exemplo de uso com ViewModel
        viewModel.texto.observe(viewLifecycleOwner) { texto ->
            // Exibir mensagem vinda do ViewModel se quiser
            Toast.makeText(requireContext(), texto, Toast.LENGTH_SHORT).show()
        }
    }
}