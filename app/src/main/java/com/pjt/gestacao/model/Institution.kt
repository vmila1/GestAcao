package com.pjt.gestacao.model

data class Institution(
    val id: Int,
    val name: String,
    val type: String,
    val address: String,
    val image: Int,
    val latitude: Double,
    val longitude: Double,
    val horario: String,
    val atendimento: String,
    val contato: String,
    val campanhas: List<String>
)