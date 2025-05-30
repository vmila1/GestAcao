package com.pjt.gestacao.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pjt.gestacao.R
import com.pjt.gestacao.adapter.MessageAdapter
import com.pjt.gestacao.data.Message
import com.pjt.gestacao.data.Sender
// Imports para as NOVAS classes de rede
import com.pjt.gestacao.network.FlaskApiRequestBody // <<<--- NOVO
import com.pjt.gestacao.network.FlaskApiResponseBody // <<<--- NOVO
import com.pjt.gestacao.network.RetrofitClient
// Remova o import de com.pjt.gestacao.network.ChatMessage se não for mais usado diretamente aqui
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {

    private lateinit var inputMessage: EditText
    private lateinit var micButton: ImageButton
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var initialSuggestionsLayout: LinearLayout

    private lateinit var btnAlimentacao: Button
    private lateinit var btnPreNatal: Button
    private lateinit var btnInchaco: Button
    private lateinit var btnEnjoo: Button

    private lateinit var messageAdapter: MessageAdapter
    private val messagesList = mutableListOf<Message>()

    // ID do usuário para a API Flask (pode ser fixo ou obtido de algum lugar)
    private val userIdForFlaskApi = "AndroidAppUser"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        inputMessage = view.findViewById(R.id.inputMessage)
        micButton = view.findViewById(R.id.micButton)
        sendButton = view.findViewById(R.id.sendButton)
        backButton = view.findViewById(R.id.backButton)
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages)
        initialSuggestionsLayout = view.findViewById(R.id.initialSuggestionsLayout)

        btnAlimentacao = view.findViewById(R.id.btnAlimentacao)
        btnPreNatal = view.findViewById(R.id.btnPreNatal)
        btnInchaco = view.findViewById(R.id.btnInchaco)
        btnEnjoo = view.findViewById(R.id.btnEnjoo)

        setupRecyclerView()
        setupClickListeners()

        return view
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messagesList)
        recyclerViewMessages.adapter = messageAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(context)
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val messageText = inputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                addUserMessageAndSendToFlask(messageText)
                inputMessage.text.clear()
            } else {
                Toast.makeText(context, "Por favor, digite uma mensagem", Toast.LENGTH_SHORT).show()
            }
        }

        micButton.setOnClickListener {
            Toast.makeText(context, "Funcionalidade de microfone ainda não implementada.", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val suggestionClickListener = View.OnClickListener { v ->
            val suggestionText = (v as Button).text.toString()
            addUserMessageAndSendToFlask(suggestionText)
        }
        btnAlimentacao.setOnClickListener(suggestionClickListener)
        btnPreNatal.setOnClickListener(suggestionClickListener)
        btnInchaco.setOnClickListener(suggestionClickListener)
        btnEnjoo.setOnClickListener(suggestionClickListener)
    }

    private fun hideInitialSuggestions() {
        if (initialSuggestionsLayout.visibility == View.VISIBLE) {
            initialSuggestionsLayout.visibility = View.GONE
        }
    }

    private fun addUserMessageAndSendToFlask(text: String) {
        hideInitialSuggestions()

        val userMessage = Message(text, Sender.USER)
        messagesList.add(userMessage)
        messageAdapter.notifyItemInserted(messagesList.size - 1)
        recyclerViewMessages.scrollToPosition(messageAdapter.itemCount - 1)

        val typingMessage = Message("Digitando...", Sender.AI)
        messagesList.add(typingMessage)
        messageAdapter.notifyItemInserted(messagesList.size - 1)
        recyclerViewMessages.scrollToPosition(messageAdapter.itemCount - 1)

        // Prepara a requisição para a API Flask
        val requestBody = FlaskApiRequestBody(
            user_id = userIdForFlaskApi,
            message = text
        )

        Log.d("ChatFragment", "Enviando para API Flask: user_id=${requestBody.user_id}, message=${requestBody.message}")

        RetrofitClient.instance.sendMessageToFlask(requestBody).enqueue(object : Callback<FlaskApiResponseBody> {
            override fun onResponse(call: Call<FlaskApiResponseBody>, response: Response<FlaskApiResponseBody>) {
                removeTypingIndicator()

                if (response.isSuccessful) {
                    val flaskApiResponse = response.body()
                    // A resposta da IA agora vem diretamente do campo "response"
                    val aiTextResponse = flaskApiResponse?.response?.trim() ?: "Não obtive uma resposta válida."

                    Log.d("ChatFragment", "Resposta da API Flask: $aiTextResponse")

                    val aiMessage = Message(aiTextResponse, Sender.AI)
                    messagesList.add(aiMessage)
                    messageAdapter.notifyItemInserted(messagesList.size - 1)
                    recyclerViewMessages.scrollToPosition(messageAdapter.itemCount - 1)

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido na API Flask"
                    Log.e("ChatFragment", "Erro na API Flask: ${response.code()} - ${response.message()} - $errorBody")
                    val errorMessage = Message("Desculpe, ocorreu um erro com a API (${response.code()}).", Sender.AI)
                    messagesList.add(errorMessage)
                    messageAdapter.notifyItemInserted(messagesList.size - 1)
                    recyclerViewMessages.scrollToPosition(messageAdapter.itemCount - 1)
                }
            }

            override fun onFailure(call: Call<FlaskApiResponseBody>, t: Throwable) {
                removeTypingIndicator()
                Log.e("ChatFragment", "Falha na chamada da API Flask: ${t.message}", t)
                val failureMessage = Message("Falha na conexão com o servidor. Verifique sua internet ou a URL do ngrok.", Sender.AI)
                messagesList.add(failureMessage)
                messageAdapter.notifyItemInserted(messagesList.size - 1)
                recyclerViewMessages.scrollToPosition(messageAdapter.itemCount - 1)
            }
        })
    }

    private fun removeTypingIndicator() {
        if (messagesList.isNotEmpty() && messagesList.last().text == "Digitando..." && messagesList.last().sender == Sender.AI) {
            val lastIndex = messagesList.size - 1
            messagesList.removeAt(lastIndex)
            messageAdapter.notifyItemRemoved(lastIndex)
        }
    }
}