package com.pjt.gestacao.ui // Verifique se este é o seu pacote correto!

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pjt.gestacao.R

class ChatFragment : Fragment() {

    // Declare as variáveis para os elementos do layout
    private lateinit var inputMessage: EditText
    private lateinit var micButton: ImageButton
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton
    // Você também pode declarar os botões de sugestão se for interagir com eles aqui

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla o layout para este fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Inicialize as views (elementos do layout)
        inputMessage = view.findViewById(R.id.inputMessage)
        micButton = view.findViewById(R.id.micButton)
        sendButton = view.findViewById(R.id.sendButton)
        backButton = view.findViewById(R.id.backButton)

        // Configurar listeners (o que acontece quando os botões são clicados)
        sendButton.setOnClickListener {
            val message = inputMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                Toast.makeText(context, "Mensagem enviada: $message", Toast.LENGTH_SHORT).show()
                inputMessage.text.clear() // Limpa o campo de texto
            } else {
                Toast.makeText(context, "Por favor, digite uma mensagem", Toast.LENGTH_SHORT).show()
            }
        }

        micButton.setOnClickListener {
            Toast.makeText(context, "Botão de microfone clicado", Toast.LENGTH_SHORT).show()
            // Aqui você implementaria a lógica de reconhecimento de voz
        }

        backButton.setOnClickListener {
            // Lógica para voltar, geralmente remover o fragmento atual ou navegar para o anterior
            // Exemplo: se este fragmento foi adicionado à back stack, você pode usar:
            parentFragmentManager.popBackStack()
            // Ou se estiver em uma Activity, pode ser:
            // activity?.onBackPressed()
        }

        // Exemplo de como configurar listeners para os botões de sugestão
        // Você precisaria de IDs individuais para cada um no XML
        // val btnAlimentacao = view.findViewById<Button>(R.id.btnAlimentacao) // Supondo que você deu um ID a ele
        // btnAlimentacao.setOnClickListener {
        //     Toast.makeText(context, "Alimentação clicado", Toast.LENGTH_SHORT).show()
        // }

        return view
    }
}