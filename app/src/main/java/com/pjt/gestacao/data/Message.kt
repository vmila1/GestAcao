package com.pjt.gestacao.data

data class Message(
    val text: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Sender {
    USER, AI
}