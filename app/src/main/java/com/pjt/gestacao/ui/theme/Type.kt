package com.pjt.gestacao.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val textDefaultColor = Color(0xFF8A8A8A) // A última cor clara que definimos

val Typography = Typography(
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
    headlineMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        color = textDefaultColor,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Bold,
        color = textDefaultColor,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MontserratFontFamily,
        fontWeight = FontWeight.Normal,
        color = textDefaultColor,
        fontSize = 12.sp,
    )
)