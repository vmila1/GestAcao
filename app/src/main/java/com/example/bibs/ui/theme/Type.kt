package com.example.bibs.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ########## COR ALTERADA AQUI PARA UM CINZA FINAL MAIS CLARO ##########
private val textDefaultColor = Color(0xFF6D6D6D
)

// Substitua o conteúdo do seu Type.kt por este
val Typography = Typography(
    // Estilo para corpo de texto normal
    bodyLarge = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        color = textDefaultColor,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        color = textDefaultColor,
        fontSize = 14.sp
    ),
    // Estilo para títulos principais (será Bold)
    headlineMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        color = textDefaultColor,
        fontSize = 20.sp
    ),
    // Estilo para títulos de cards (será Bold)
    titleMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        color = textDefaultColor,
        fontSize = 16.sp
    ),
    // Estilo para textos pequenos, como na tabela
    labelSmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        color = textDefaultColor,
        fontSize = 12.sp,
    )
)