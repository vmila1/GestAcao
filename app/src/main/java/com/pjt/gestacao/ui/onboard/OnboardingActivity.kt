package com.pjt.gestacao.ui.onboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pjt.gestacao.MainActivity
import com.pjt.gestacao.R
import com.pjt.gestacao.ui.theme.GestAcaoTheme

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestAcaoTheme {
                OnboardingScreen()
            }
        }
    }
}

@Composable
fun OnboardingScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F6))
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        // Removemos o 'verticalArrangement' para que os pesos controlem o espaço
    ) {
        // 1. Spacer flexível no topo para empurrar o conteúdo para baixo
        Spacer(modifier = Modifier.weight(1.5f))

        // 2. Logo com peso, forçando-a a ocupar um espaço vertical grande
        Image(
            painter = painterResource(id = R.drawable.logo_gestacao),
            contentDescription = "Logo GestAção+",
            modifier = Modifier
                .fillMaxWidth(0.8f) // Mantém a largura em 80%
                .weight(2f)       // <<-- ESTA É A MUDANÇA PRINCIPAL
            //      Dá à imagem 2x mais espaço vertical que os Spacers
        )

        // 3. Agrupamos o texto e o botão para que fiquem juntos
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bem-vinda ao",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF9F0243),
                textAlign = TextAlign.Center
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF9F0243), fontWeight = FontWeight.Bold)) {
                        append("Gest")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFFFABBD), fontWeight = FontWeight.Bold)) {
                        append("Ação+")
                    }
                },
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                    (context as? OnboardingActivity)?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxWidth(0.85f)
                    .height(60.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFABBD)),
                shape = RoundedCornerShape(17.dp)
            ) {
                Text(
                    text = "Entrar",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Spacer flexível na parte de baixo
        Spacer(modifier = Modifier.weight(1.5f))
    }
}