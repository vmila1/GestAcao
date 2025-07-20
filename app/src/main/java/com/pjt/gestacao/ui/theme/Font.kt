package com.pjt.gestacao.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.pjt.gestacao.R

val MontserratFontFamily = FontFamily(
    // Alterado de montserrat_regular para 'montserrat', para usar o arquivo 'montserrat.ttf'
    Font(R.font.montserrat, FontWeight.Normal),

    // Alterado de montserrat_bold para 'roboto_bold', para usar o arquivo 'roboto_bold.ttf'
    Font(R.font.roboto_bold, FontWeight.Bold)
)