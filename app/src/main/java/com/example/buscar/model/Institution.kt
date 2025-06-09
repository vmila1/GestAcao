package com.example.buscar.model

data class Institution(
    val id: Int,
    val name: String,
    val type: String,
    val distance: String,
    val logoResId: Int      // referência a drawable/mipmap
)