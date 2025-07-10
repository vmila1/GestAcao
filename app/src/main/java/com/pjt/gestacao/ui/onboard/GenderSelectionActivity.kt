package com.pjt.gestacao.ui.onboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.pjt.gestacao.R
import com.pjt.gestacao.ui.theme.GestAcaoTheme

class GenderSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestAcaoTheme {
                GenderSelectionScreen(
                    onBack = { finish() },
                    onNext = {
                        startActivity(Intent(this, LocationActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun GenderSelectionScreen(onBack: () -> Unit, onNext: () -> Unit) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    val options = listOf(
        "Masculino" to Color(0xFFB6E2F2),
        "Feminino" to Color(0xFFE2A8BC),
        "Ainda não sei" to Color(0xFFFFF2B2),
        "Prefiro não informar" to Color(0xFFC5C5C5)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F6))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Qual o gênero do seu bebê?",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 22.sp,
            color = Color.Black
        )
        Text(
            text = "Isso nos ajuda a oferecer conteúdos e dicas personalizadas",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            lineHeight = 18.sp
        )

        options.forEach { (label, color) ->
            GenderOptionButton(
                text = label,
                color = color,
                isSelected = selectedOption == label,
                onClick = { selectedOption = label }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("< Voltar", color = Color(0xFF9F0243))
            }
            TextButton(
                onClick = { if (selectedOption != null) onNext() },
                enabled = selectedOption != null
            ) {
                Text("Próximo >", color = if (selectedOption != null) Color(0xFFD00036) else Color.Gray)
            }
        }
    }
}

@Composable
fun GenderOptionButton(text: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) Color(0xFFFFEB3B) else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(50.dp)
            .background(color, shape = RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 16.sp)
    }
}