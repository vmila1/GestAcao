package com.pjt.gestacao.ui.onboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pjt.gestacao.MainActivity
import com.pjt.gestacao.R
import com.pjt.gestacao.ui.UserState
import com.pjt.gestacao.ui.UserViewModel
import com.pjt.gestacao.ui.theme.GestAcaoTheme
import kotlinx.coroutines.flow.collectLatest

// Modelo de dados para o grid
data class MonthData(val month: Int, val imageRes: Int)

class OnboardingActivity : ComponentActivity() {

    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onboardingViewModel.fetchUserLocation(this)
        } else {
            Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launchWhenStarted {
            onboardingViewModel.onboardingFinished.collectLatest {
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }
        }
        setContent {
            GestAcaoTheme {
                OnboardingFlow(
                    onboardingViewModel = onboardingViewModel,
                    userViewModel = userViewModel,
                    onRequestPermission = { checkLocationPermission() }
                )
            }
        }
    }

    private fun checkLocationPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) -> {
                onboardingViewModel.fetchUserLocation(this)
            }

            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}

// Componente principal para gerenciar a navegação
@Composable
fun OnboardingFlow(
    onboardingViewModel: OnboardingViewModel,
    userViewModel: UserViewModel,
    onRequestPermission: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userState by userViewModel.userState.observeAsState(initial = UserState.UNKNOWN)

    // Efeito que reage às mudanças de estado e realiza a navegação
    LaunchedEffect(userState) {
        when (userState) {
            UserState.LOGGED_IN -> {
                context.startActivity(Intent(context, MainActivity::class.java))
                (context as? OnboardingActivity)?.finish()
            }
            UserState.NEEDS_ONBOARDING -> {
                navController.navigate("select_month") {
                    popUpTo("welcome") { inclusive = true }
                }
            }
            UserState.AUTH_ERROR -> {
                Toast.makeText(context, "Erro de conexão. Tente novamente.", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }


    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            OnboardingWelcomeScreen(userState = userState, onEnterClick = { userViewModel.signInAndProceed() })
        }
        composable("select_month") {
            SelectMonthScreen(navController = navController, viewModel = onboardingViewModel)
        }
        composable("select_gender") {
            GenderSelectionScreen(navController = navController, viewModel = onboardingViewModel)
        }
        composable("select_location") {
            LocationScreen(
                navController = navController,
                viewModel = onboardingViewModel,
                onRequestPermission = onRequestPermission
            )
        }
    }
}

@Composable
fun OnboardingWelcomeScreen(userState: UserState, onEnterClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F6))
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        when (userState) {
            UserState.AUTHENTICATING -> {
                CircularProgressIndicator()
            }
            else -> {
                WelcomeScreenContent(onEnterClick = onEnterClick)
            }
        }
    }
}

@Composable
fun WelcomeScreenContent(onEnterClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1.5f))
        Image(
            painter = painterResource(id = R.drawable.logo_gestacao),
            contentDescription = "Logo GestAção+",
            modifier = Modifier.fillMaxWidth(0.8f).weight(2f)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bem-vinda ao",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9F0243),
                textAlign = TextAlign.Center
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF9F0243), fontWeight = FontWeight.Bold)) {
                        append("Gest")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFFFFABBD), fontWeight = FontWeight.Bold)) {
                        append("Ação")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF9F0243), fontWeight = FontWeight.Bold)) {
                        append("+")
                    }
                },
                fontSize = 56.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onEnterClick,
                modifier = Modifier.fillMaxWidth().fillMaxWidth(0.85f).height(60.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFABBD)),
                shape = RoundedCornerShape(17.dp)
            ) {
                Text(text = "Entrar", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.weight(1.5f))
    }
}


@Composable
fun SelectMonthScreen(navController: NavController, viewModel: OnboardingViewModel) {
    var selectedMonth by remember { mutableStateOf(viewModel.mes) }

    val months = listOf(
        MonthData(1, R.drawable.mes_1),
        MonthData(2, R.drawable.mes_2),
        MonthData(3, R.drawable.mes_3),
        MonthData(4, R.drawable.mes_4),
        MonthData(5, R.drawable.mes_5),
        MonthData(6, R.drawable.mes_6),
        MonthData(7, R.drawable.mes_7),
        MonthData(8, R.drawable.mes_8),
        MonthData(9, R.drawable.mes_9)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6F6))
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "Quantos meses de gestação você está?",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF434242),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Isso nos ajuda a oferecer conteúdos e dicas personalizadas",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            userScrollEnabled = false,
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
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    "< Voltar",
                    fontSize = 16.sp
                )
            }
            Button(
                onClick = {
                    viewModel.mes = selectedMonth
                    navController.navigate("select_gender")
                },
                enabled = selectedMonth != null
            ) { Text("Próximo") }
        }
    }
}

@Composable
fun MonthGridItem(monthData: MonthData, isSelected: Boolean, onItemSelected: (Int) -> Unit) {
    val borderColor = if (isSelected) Color(0xFFE91E63) else Color.Transparent
    Image(
        painter = painterResource(id = monthData.imageRes),
        contentDescription = "Mês ${monthData.month}",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .clickable { onItemSelected(monthData.month) }
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .border(3.dp, borderColor, CircleShape)
    )
}

@Composable
fun GenderSelectionScreen(navController: NavController, viewModel: OnboardingViewModel) {
    var selectedOption by remember { mutableStateOf(viewModel.genero) }

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
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "Qual o gênero do seu bebê?",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 22.sp,
            color = Color.Black
        )
        Text(
            "Isso nos ajuda a oferecer conteúdos e dicas personalizadas",
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
                onClick = { selectedOption = label })
        }
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    "< Voltar",
                    color = Color(0xFF9F0243)
                )
            }
            TextButton(
                onClick = {
                    viewModel.genero = selectedOption
                    navController.navigate("select_location")
                },
                enabled = selectedOption != null
            ) {
                Text(
                    "Próximo >",
                    color = if (selectedOption != null) Color(0xFFD00036) else Color.Gray
                )
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

@Composable
fun LocationScreen(navController: NavController, viewModel: OnboardingViewModel, onRequestPermission: () -> Unit) {
    val country by remember { mutableStateOf("Brasil") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF6F6))
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Qual sua localização?", style = MaterialTheme.typography.headlineMedium, color = Color.Black)
            Text(
                "Veja ONG’s próximas, postos de saúde e ações ativas próximas a você",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.DarkGray
            )
            Button(
                onClick = onRequestPermission,
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFABBD))
            ) {
                Text("Localizar automaticamente", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LocationTextField(value = viewModel.cep, onValueChange = { viewModel.cep = it }, label = "CEP")
            LocationTextField(value = country, onValueChange = {}, label = "País", enabled = false)
            LocationTextField(value = viewModel.estado, onValueChange = { viewModel.estado = it }, label = "Estado")
            LocationTextField(value = viewModel.cidade, onValueChange = { viewModel.cidade = it }, label = "Cidade")
            LocationTextField(value = viewModel.rua, onValueChange = { viewModel.rua = it }, label = "Rua")

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { navController.popBackStack() }, enabled = !viewModel.isLoading) {
                    Text("< Voltar", color = Color(0xFF9F0243), fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { viewModel.saveOnboardingData() }, enabled = !viewModel.isLoading) {
                    Text(
                        text = if (viewModel.cep.isBlank()) "Pular >" else "Finalizar!",
                        color = if (viewModel.cep.isBlank()) Color(0xFF9F0243) else Color(0xFFD00036),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (viewModel.isLoading) {
            Dialog(
                onDismissRequest = { /* Impede o fechamento pelo usuário */ },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun LocationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    )
}