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
import androidx.navigation.fragment.findNavController // Para Jetpack Navigation (Opção 1)
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pjt.gestacao.R
import com.pjt.gestacao.adapter.MessageAdapter
import com.pjt.gestacao.data.ActionType
import com.pjt.gestacao.data.Message
import com.pjt.gestacao.data.Sender
import com.pjt.gestacao.network.FlaskApiRequestBody
import com.pjt.gestacao.network.FlaskApiResponseBody
import com.pjt.gestacao.network.RetrofitClient
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

    // Botões de sugestão
    private lateinit var btnAlimentacao: Button
    private lateinit var btnPreNatal: Button
    private lateinit var btnInchaco: Button
    private lateinit var btnEnjoo: Button

    private lateinit var messageAdapter: MessageAdapter
    private val messagesList = mutableListOf<Message>()
    private val userIdForFlaskApi = "AndroidAppUser" // Ou um ID de usuário dinâmico

    companion object {
        // Formato do Comando da IA: [ACAO_TextoDoBotaoComEspacos_IDENTIFICADORDAACAO]
        // Exemplo: [ACAO_Ir para Início_NAVIGATE_TO_HOME]
        private const val ACTION_COMMAND_PREFIX = "[ACAO_"
        private const val ACTION_COMMAND_SEPARATOR = "_" // Usado para separar TextoDoBotao de IDENTIFICADORDAACAO no comando bruto
        private const val ACTION_COMMAND_SUFFIX = "]"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Inicialização das views
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
        messageAdapter = MessageAdapter(messagesList) { actionType ->
            // Este é o lambda que será chamado quando um botão de ação no chat for clicado
            handleChatAction(actionType)
        }
        recyclerViewMessages.adapter = messageAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun handleChatAction(actionType: ActionType) {
        Log.d("ChatFragment", "Ação do chat clicada: $actionType")
        when (actionType) {
            ActionType.NAVIGATE_TO_HOME -> navigateToHome()
            // Adicione outros casos para ActionType aqui se necessário
            // ActionType.NAVIGATE_TO_BABY_DEVELOPMENT -> navigateToBabyDevelopmentScreen()
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val messageText = inputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                addUserMessageAndSendToFlask(messageText)
                inputMessage.text.clear()
            } else {
                Toast.makeText(requireContext(), "Por favor, digite uma mensagem", Toast.LENGTH_SHORT).show()
            }
        }

        micButton.setOnClickListener {
            Toast.makeText(requireContext(), "Funcionalidade de microfone ainda não implementada.", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnAlimentacao.setOnClickListener {
            val question = "Quais cuidados devo ter com a alimentação durante a gravidez? e qual a principal dica para esse período?"
            addUserMessageAndSendToFlask(question)
        }
        btnPreNatal.setOnClickListener {
            val question = "Qual a importância do pré-natal e quais são os principais exames e acompanhamentos feitos durante a gestação?"
            addUserMessageAndSendToFlask(question)
        }
        btnInchaco.setOnClickListener {
            val question = "É normal ter inchaço durante a gravidez? O que pode causar e como posso aliviar esse desconforto?"
            addUserMessageAndSendToFlask(question)
        }
        btnEnjoo.setOnClickListener {
            val question = "Os enjoos são muito comuns na minha gravidez. Como posso aliviar os enjoos matinais e ao longo do dia? Existem dicas caseiras eficazes?"
            addUserMessageAndSendToFlask(question)
        }
    }

    private fun hideInitialSuggestions() {
        if (initialSuggestionsLayout.visibility == View.VISIBLE) {
            initialSuggestionsLayout.visibility = View.GONE
            if (recyclerViewMessages.visibility == View.GONE) {
                recyclerViewMessages.visibility = View.VISIBLE
            }
        }
    }

    private fun addUserMessageAndSendToFlask(text: String) {
        hideInitialSuggestions()

        val userMessage = Message(text = text, sender = Sender.USER)
        messagesList.add(userMessage)
        messageAdapter.notifyItemInserted(messagesList.size - 1)
        scrollToBottom()

        val typingMessage = Message(text = "Digitando...", sender = Sender.AI)
        messagesList.add(typingMessage)
        messageAdapter.notifyItemInserted(messagesList.size - 1)
        scrollToBottom()

        val requestBody = FlaskApiRequestBody(user_id = userIdForFlaskApi, message = text)
        Log.d("ChatFragment", "Enviando para API Flask: user_id=${requestBody.user_id}, message=${requestBody.message}")

        RetrofitClient.instance.sendMessageToFlask(requestBody).enqueue(object : Callback<FlaskApiResponseBody> {
            override fun onResponse(call: Call<FlaskApiResponseBody>, response: Response<FlaskApiResponseBody>) {
                removeTypingIndicator()

                if (response.isSuccessful) {
                    val flaskApiResponse = response.body()
                    val aiRawResponse = flaskApiResponse?.response?.trim() ?: "Não obtive uma resposta válida."
                    Log.d("ChatFragment", "Resposta da API Flask (bruta): $aiRawResponse")

                    var mainAiTextMessage = aiRawResponse
                    var commandProcessedSuccessfully = false

                    if (aiRawResponse.endsWith(ACTION_COMMAND_SUFFIX)) {
                        val lastPrefixIndex = aiRawResponse.lastIndexOf(ACTION_COMMAND_PREFIX)
                        if (lastPrefixIndex != -1 && lastPrefixIndex < aiRawResponse.length - ACTION_COMMAND_SUFFIX.length) {

                            val commandPart = aiRawResponse.substring(lastPrefixIndex)
                            mainAiTextMessage = aiRawResponse.substring(0, lastPrefixIndex).trim()

                            val commandContent = commandPart.removePrefix(ACTION_COMMAND_PREFIX).removeSuffix(ACTION_COMMAND_SUFFIX)
                            val parts = commandContent.split(ACTION_COMMAND_SEPARATOR, limit = 2)

                            if (parts.size == 2) {
                                val buttonText = parts[0].replace("_", " ") // Para o caso da IA usar _ no texto do botão
                                val actionTypeName = parts[1].trim() // .trim() aqui é importante
                                try {
                                    val actionType = ActionType.valueOf(actionTypeName)

                                    if (mainAiTextMessage.isNotEmpty()) {
                                        addRegularAiMessage(mainAiTextMessage)
                                    }

                                    val actionMessage = Message(
                                        text = "", // Texto principal da mensagem de ação pode ser vazio
                                        sender = Sender.AI,
                                        buttonText = buttonText,
                                        actionType = actionType
                                    )
                                    messagesList.add(actionMessage)
                                    messageAdapter.notifyItemInserted(messagesList.size - 1)
                                    commandProcessedSuccessfully = true

                                } catch (e: IllegalArgumentException) {
                                    Log.e("ChatFragment", "Tipo de ação desconhecido no comando: '$actionTypeName'", e)
                                }
                            } else {
                                Log.d("ChatFragment", "Comando ACAO malformado (partes insuficientes): $commandContent")
                            }
                        }
                    }

                    if (!commandProcessedSuccessfully) {
                        // Se não houve comando, ou o comando falhou ao ser processado, exibe a mensagem bruta (ou o que sobrou dela)
                        addRegularAiMessage(aiRawResponse)
                    }
                    scrollToBottom()

                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido na API Flask"
                    Log.e("ChatFragment", "Erro na API Flask: ${response.code()} - ${response.message()} - $errorBody")
                    addRegularAiMessage("Desculpe, ocorreu um erro com a API (${response.code()}).")
                    scrollToBottom()
                }
            }

            override fun onFailure(call: Call<FlaskApiResponseBody>, t: Throwable) {
                removeTypingIndicator()
                Log.e("ChatFragment", "Falha na chamada da API Flask: ${t.message}", t)
                addRegularAiMessage("Falha na conexão com o servidor. Verifique sua internet ou a URL do ngrok.")
                scrollToBottom()
            }
        })
    }

    private fun addRegularAiMessage(text: String) {
        if (text.isNotEmpty()) { // Evita adicionar mensagens completamente vazias
            val aiMessage = Message(text = text, sender = Sender.AI)
            messagesList.add(aiMessage)
            messageAdapter.notifyItemInserted(messagesList.size - 1)
        }
    }

    private fun removeTypingIndicator() {
        if (messagesList.isNotEmpty() && messagesList.last().text == "Digitando..." && messagesList.last().sender == Sender.AI) {
            val lastIndex = messagesList.size - 1
            messagesList.removeAt(lastIndex)
            messageAdapter.notifyItemRemoved(lastIndex)
        }
    }

    private fun scrollToBottom() {
        if (messageAdapter.itemCount > 0) {
            recyclerViewMessages.post { // Usar post para garantir que a rolagem ocorra após o layout
                recyclerViewMessages.smoothScrollToPosition(messageAdapter.itemCount - 1)
            }
        }
    }

    private fun navigateToHome() {
        Log.d("ChatFragment", "Navegando para HomeFragment...")
        try {
            // Opção 1: Usando Jetpack Navigation Component
            // Substitua 'R.id.action_chatActual_to_home' pelo ID da action definida no seu mobile_navigation.xml
            // que leva do ChatFragment para o HomeFragment.
            findNavController().navigate(R.id.action_chatActual_to_home)
            Log.d("ChatFragment", "Navegação via NavController (action_chatActual_to_home) iniciada.")

            // Opção 2: Usando FragmentManager manualmente (Comente a Opção 1 se usar esta)
            // Substitua 'R.id.nav_host_fragment' pelo ID real do container de Fragment na sua Activity.
            /*
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment())
                .addToBackStack(null)
                .commit()
            Log.d("ChatFragment", "Navegação via FragmentManager (replace) iniciada.")
            */

        } catch (e: Exception) {
            Log.e("ChatFragment", "Erro ao tentar navegar para Home: ${e.message}", e)
            Toast.makeText(requireContext(), "Não foi possível abrir a tela Home.", Toast.LENGTH_SHORT).show()
        }
    }
}