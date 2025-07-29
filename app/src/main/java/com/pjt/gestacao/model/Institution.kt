package com.pjt.gestacao.model

data class Institution(
    val id: Int,
    val name: String,
    val type: String,
    val address: String,
    val image: Int,
    val description: String,
    val phone: String,
    val site: String
)