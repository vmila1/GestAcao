package com.pjt.gestacao.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import androidx.core.view.isGone
import androidx.core.view.isVisible

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
    private val userIdForFlaskApi = "AndroidAppUser"

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startVoiceRecognition()
            } else {
                Toast.makeText(requireContext(), "Permissão de áudio negada.", Toast.LENGTH_LONG).show()
            }
        }

    companion object {
        // Formato esperado do comando da IA para gerar um botão de ação no chat.
        // Exemplo: [ACAO_Ir para Início_NAVIGATE_TO_HOME]
        private const val ACTION_COMMAND_PREFIX = "[ACAO_"
        private const val ACTION_COMMAND_SEPARATOR = "_"
        private const val ACTION_COMMAND_SUFFIX = "]"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Binding das views
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // O SpeechRecognizer é inicializado aqui para garantir que o contexto do fragmento já está disponível.
        setupSpeechRecognizer()
    }

    private fun setupSpeechRecognizer() {
        if (!isAdded || context == null) return

        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Log.e("ChatFragment", "Reconhecimento de fala não disponível neste dispositivo.")
            Toast.makeText(requireContext(), "Reconhecimento de fala não disponível.", Toast.LENGTH_LONG).show()
            micButton.isEnabled = false
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                micButton.setImageResource(R.drawable.ic_mic_active)
                Toast.makeText(requireContext(), "Ouvindo...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                micButton.setImageResource(R.drawable.ic_mic_custom)
            }

            override fun onError(error: Int) {
                val errorMessage = getSpeechErrorMessage(error)
                Log.e("ChatFragment", "SpeechRecognizer Erro: $errorMessage (código: $error)")
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                micButton.setImageResource(R.drawable.ic_mic_custom)
            }

            override fun onResults(results: Bundle?) {
                micButton.setImageResource(R.drawable.ic_mic_custom)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val recognizedText = matches[0]
                    inputMessage.setText(recognizedText)
                    inputMessage.setSelection(recognizedText.length)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messagesList) { actionType ->
            handleChatAction(actionType)
        }
        recyclerViewMessages.adapter = messageAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun handleChatAction(actionType: ActionType) {
        when (actionType) {
            ActionType.NAVIGATE_TO_HOME -> navigateToHome()
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val messageText = inputMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                addUserMessageAndSendToFlask(messageText)
            } else {
                Toast.makeText(requireContext(), "Por favor, digite uma mensagem", Toast.LENGTH_SHORT).show()
            }
        }

        micButton.setOnClickListener {
            checkAndRequestAudioPermission()
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

    private fun checkAndRequestAudioPermission() {
        if (!isAdded || context == null) return
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceRecognition()
            }
            // Explica ao usuário por que a permissão é necessária, caso ele já tenha negado uma vez.
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Permissão Necessária")
                    .setMessage("Para usar a entrada por voz, precisamos da sua permissão para acessar o microfone.")
                    .setPositiveButton("Ok") { _, _ ->
                        requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            else -> {
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startVoiceRecognition() {
        if (!isAdded || context == null) return
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            try {
                speechRecognizer.startListening(speechRecognizerIntent)
            } catch (e: Exception) {
                Log.e("ChatFragment", "Erro ao iniciar SpeechRecognizer: ${e.message}", e)
            }
        } else {
            Toast.makeText(requireContext(), "Reconhecimento de fala não disponível.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getSpeechErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Erro na gravação do áudio."
            SpeechRecognizer.ERROR_CLIENT -> "Erro no cliente de reconhecimento."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Sem permissão para usar o microfone."
            SpeechRecognizer.ERROR_NETWORK -> "Erro de rede para reconhecimento de voz."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tempo de rede esgotado para reconhecimento."
            SpeechRecognizer.ERROR_NO_MATCH -> "Não entendi o que você disse. Tente novamente."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Serviço de reconhecimento de voz ocupado."
            SpeechRecognizer.ERROR_SERVER -> "Erro no servidor de reconhecimento de voz."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nenhuma fala detectada. Por favor, tente falar mais perto do microfone."
            else -> "Erro desconhecido no reconhecimento de voz."
        }
    }

    private fun isNetworkAvailable(): Boolean {
        if (!isAdded || context == null) return false
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun hideInitialSuggestions() {
        if (initialSuggestionsLayout.isVisible) {
            initialSuggestionsLayout.visibility = View.GONE
            if (recyclerViewMessages.isGone) {
                recyclerViewMessages.visibility = View.VISIBLE
            }
        }
    }

    private fun addUserMessageAndSendToFlask(text: String) {
        if (!isNetworkAvailable()) {
            showOfflineDialog()
            return
        }
        inputMessage.text.clear()
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

        try {
            RetrofitClient.instance.sendMessageToFlask(requestBody).enqueue(object : Callback<FlaskApiResponseBody> {
                override fun onResponse(call: Call<FlaskApiResponseBody>, response: Response<FlaskApiResponseBody>) {
                    removeTypingIndicator()
                    if (response.isSuccessful) {
                        val flaskApiResponse = response.body()
                        val aiRawResponse = flaskApiResponse?.response?.trim()

                        var commandProcessedSuccessfully = false
                        if (aiRawResponse.isNullOrEmpty()) {
                            addRegularAiMessage("A assistente teve um problema ao gerar a resposta. Tente reformular sua pergunta.")
                        } else {
                            var mainAiTextMessage = aiRawResponse
                            // Verifica se a resposta contém um comando de ação no final
                            if (aiRawResponse.endsWith(ACTION_COMMAND_SUFFIX)) {
                                val lastPrefixIndex = aiRawResponse.lastIndexOf(ACTION_COMMAND_PREFIX)
                                if (lastPrefixIndex != -1) {
                                    val commandPart = aiRawResponse.substring(lastPrefixIndex)
                                    mainAiTextMessage = aiRawResponse.substring(0, lastPrefixIndex).trim()
                                    val commandContent = commandPart.removePrefix(ACTION_COMMAND_PREFIX).removeSuffix(ACTION_COMMAND_SUFFIX)
                                    val parts = commandContent.split(ACTION_COMMAND_SEPARATOR, limit = 2)

                                    if (parts.size == 2) {
                                        val buttonText = parts[0].replace("_", " ")
                                        val actionTypeName = parts[1].trim()
                                        try {
                                            val actionType = ActionType.valueOf(actionTypeName)
                                            if (mainAiTextMessage.isNotEmpty()) { addRegularAiMessage(mainAiTextMessage) }
                                            val actionMessage = Message(text = "", sender = Sender.AI, buttonText = buttonText, actionType = actionType)
                                            messagesList.add(actionMessage)
                                            messageAdapter.notifyItemInserted(messagesList.size - 1)
                                            commandProcessedSuccessfully = true
                                        } catch (e: IllegalArgumentException) {
                                            Log.e("ChatFragment", "Tipo de ação desconhecido no comando: '$actionTypeName'", e)
                                        }
                                    }
                                }
                            }
                            if (!commandProcessedSuccessfully) {
                                addRegularAiMessage(mainAiTextMessage)
                            }
                        }
                    } else { // Trata erros de HTTP como 4xx, 5xx
                        val errorMessage = when(response.code()) {
                            503 -> "A assistente está temporariamente indisponível. Estamos trabalhando para resolver."
                            else -> "A assistente virtual está indisponível no momento. Tente novamente mais tarde."
                        }
                        addRegularAiMessage(errorMessage)
                    }
                    scrollToBottom()
                }

                override fun onFailure(call: Call<FlaskApiResponseBody>, t: Throwable) {
                    removeTypingIndicator()
                    Log.e("ChatFragment", "Falha de rede na chamada da API: ${t.message}", t)
                    addRegularAiMessage("Não foi possível conectar com a assistente. Verifique sua conexão e tente novamente.")
                    scrollToBottom()
                }
            })
        } catch (e: Exception) { // Captura outros erros antes da chamada ser despachada
            Log.e("ChatFragment", "Erro ao tentar enviar mensagem: ${e.message}", e)
            Toast.makeText(requireContext(), "Erro ao enviar sua mensagem. Tente novamente.", Toast.LENGTH_LONG).show()
            removeTypingIndicator()
        }
    }

    private fun showOfflineDialog() {
        if (!isAdded || context == null) { return }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sem Conexão")
            .setMessage("Você está offline. Algumas funcionalidades, como a assistente virtual, podem não funcionar corretamente.")
            .setPositiveButton("Entendido") { dialog, _ -> dialog.dismiss() }
            .setIcon(R.drawable.ic_no_internet)
            .show()
    }

    private fun addRegularAiMessage(text: String) {
        if (text.isNotEmpty()) {
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
            // Usar post garante que a rolagem aconteça depois que o RecyclerView atualizar o layout
            recyclerViewMessages.post {
                recyclerViewMessages.smoothScrollToPosition(messageAdapter.itemCount - 1)
            }
        }
    }

    private fun navigateToHome() {
        try {
            findNavController().navigate(R.id.action_chatActual_to_home)
        } catch (e: Exception) {
            Log.e("ChatFragment", "Erro ao tentar navegar para Home: ${e.message}", e)
            Toast.makeText(requireContext(), "Erro ao acessar a funcionalidade. Tente novamente.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.stopListening()
            speechRecognizer.destroy()
        }
    }
}