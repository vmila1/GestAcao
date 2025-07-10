package com.pjt.gestacao.ui.onboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pjt.gestacao.MainActivity
import com.pjt.gestacao.R
import com.pjt.gestacao.ui.theme.GestAcaoTheme
import androidx.compose.ui.layout.ContentScale

// Modelo de dados para cada item do grid
data class MonthData(val month: Int, val imageRes: Int)

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GestAcaoTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "welcome_screen") {
                    composable("welcome_screen") {
                        OnboardingWelcomeScreen(navController = navController)
                    }
                    composable("select_month_screen") {
                        SelectMonthScreen(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingWelcomeScreen(navController: NavController) {
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
                    // Diz ao nosso "gerenciador de telas" para ir para a tela de seleção de mês
                    navController.navigate("select_month_screen")
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


// COLE ESTE CÓDIGO NO FINAL DO SEU ARQUIVO

@Composable
fun SelectMonthScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf<Int?>(null) }

    val months = listOf(
        MonthData(1, R.drawable.mes_1), MonthData(2, R.drawable.mes_2), MonthData(3, R.drawable.mes_3),
        MonthData(4, R.drawable.mes_4), MonthData(5, R.drawable.mes_5), MonthData(6, R.drawable.mes_6),
        MonthData(7, R.drawable.mes_7), MonthData(8, R.drawable.mes_8), MonthData(9, R.drawable.mes_9)
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
            text = "Quantos meses de gestação você está?",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF434242),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Isso nos ajuda a oferecer conteúdos e dicas personalizadas",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 1. ADICIONADO SPACER FLEXÍVEL PARA EMPURRAR O GRID PARA BAIXO
        Spacer(modifier = Modifier.weight(1f))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            // 2. REMOVIDO O MODIFIER.WEIGHT(1f) DAQUI
            // A grade agora terá seu tamanho natural, sem tentar preencher todo o espaço
            userScrollEnabled = false, // Desabilitamos o scroll da grade, pois a tela toda já scrolla
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(months) { monthData ->
                MonthGridItem(
                    monthData = monthData,
                    isSelected = selectedMonth == monthData.month,
                    onItemSelected = { selectedMonth = it }
                )
            }
        }

        // 3. ADICIONADO OUTRO SPACER FLEXÍVEL PARA CENTRALIZAR
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(text = "< Voltar", fontSize = 16.sp)
            }
            Button(
                onClick = {
                    val intent = Intent(context, GenderSelectionActivity::class.java)
                    context.startActivity(intent)
                    (context as? OnboardingActivity)?.finish()
                },
                enabled = selectedMonth != null
            ) {
                Text(text = "Próximo")
            }
        }
    }
}

@Composable
fun MonthGridItem(
    monthData: MonthData,
    isSelected: Boolean,
    onItemSelected: (Int) -> Unit
) {
    val borderColor = if (isSelected) Color(0xFFE91E63) else Color.Transparent

    // A Column foi removida pois agora só temos a Imagem.
    // O modificador clickable foi passado diretamente para a Imagem.
    Image(
        painter = painterResource(id = monthData.imageRes),
        contentDescription = "Mês ${monthData.month}",
        contentScale = ContentScale.Fit, // Fit para a imagem não cortar
        modifier = Modifier
            .clickable { onItemSelected(monthData.month) } // Ação de clique direto na imagem
            .fillMaxWidth()
            .aspectRatio(1f) // Garante que a imagem seja sempre um círculo perfeito
            .clip(CircleShape)
            .border(3.dp, borderColor, CircleShape)
    )
    // O SPACER E O TEXT FORAM COMPLETAMENTE REMOVIDOS
}