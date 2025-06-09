package com.pjt.gestacao.data

data class Message(
    val text: String, // Para mensagens de texto normais ou pode ser usado como um fallback/descrição para ações
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis(),
    val buttonText: String? = null, // Texto que aparecerá no botão
    val actionType: ActionType? = null // O tipo de ação que o botão deve executar
)

enum class Sender {
    USER, AI
}