package com.pjt.gestacao.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.pjt.gestacao.R // Importe o R do seu projeto principal
import com.pjt.gestacao.ui.theme.GestAcaoTheme // Use o nome do seu tema principal

class InfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GestAcaoTheme {
                    TelaGestante()
                }
            }
        }
    }
}

@Composable
fun TelaGestante() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFFF7))
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.bebe),
            contentDescription = "Bebê",
            modifier = Modifier.size(100.dp)
        )
        Text(
            text = "7 meses",
            color = Color(0xFFD89A9A),
            fontWeight = FontWeight.Bold,
            fontSize = 17.38.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5DF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Oi, mamãe! Já consigo ouvir sua voz!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5DF))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Tamanho de",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.abacaxi),
                    contentDescription = "Abacaxi",
                    modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Um abacaxi grande!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5DF))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Marcos de desenvolvimento:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(12.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            DevelopmentTableRow("1 a 4", "1 mês", Color(0xFFEBEBEB))
                            DevelopmentTableRow("5 a 8", "2 meses", Color(0xFFEBEBEB))
                            DevelopmentTableRow("9 a 12", "3 meses", Color(0xFFEBEBEB))
                        }
                        Text("1o Trimestre", modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            DevelopmentTableRow("13 a 16", "4 meses", Color(0xFFE5E5E5))
                            DevelopmentTableRow("17 a 21", "5 meses", Color(0xFFE5E5E5))
                            DevelopmentTableRow("22 a 26", "6 meses", Color(0xFFE5E5E5))
                        }
                        Text("2o Trimestre", modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            DevelopmentTableRow("27 a 30", "7 meses", Color(0xFFDDEBD9))
                            DevelopmentTableRow("31 a 35", "8 meses", Color(0xFFDDEBD9))
                            DevelopmentTableRow("36 a 40", "9 meses", Color(0xFFDDEBD9))
                        }
                        Text("3o Trimestre", modifier = Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDDEBD9))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Como o bebê está esse mês ❤️",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Text("• Dorme e até pode sonhar.", style = MaterialTheme.typography.bodyMedium)
                Text("• Começa a reagir à luz e ao som, principalmente a sua voz.", style = MaterialTheme.typography.bodyMedium)
                Text("• Ele já consegue abrir os olhos.", style = MaterialTheme.typography.bodyMedium)
                Text("• O cérebro está crescendo bem rápido.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDDEBD9))
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Cuidados para mamãe 🤰",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Text("• Tente dormir de lado, é melhor pra você e pro bebê.", style = MaterialTheme.typography.bodyMedium)
                Text("• Coma alimentos simples e saudáveis, como feijão, ovo, frutas.", style = MaterialTheme.typography.bodyMedium)
                Text("• Fale com seu bebê — ele escuta e sente seu carinho.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun DevelopmentTableRow(semana: String, mes: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Semana",
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = semana,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = mes,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelSmall
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}