package com.pjt.gestacao.ui.onboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.pjt.gestacao.MainActivity
import com.pjt.gestacao.ui.theme.GestAcaoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class LocationActivity : ComponentActivity() {

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    // Variáveis de estado para os campos de endereço
    private var cep by mutableStateOf("")
    private var state by mutableStateOf("")
    private var city by mutableStateOf("")
    private var street by mutableStateOf("")

    // Variável de estado para controlar a exibição do loading
    private var isLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                fetchUserLocation()
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            GestAcaoTheme {
                LocationScreen(
                    cep = cep,
                    state = state,
                    city = city,
                    street = street,
                    isLoading = isLoading, // Passa o estado de loading para a UI
                    onCepChange = { cep = it },
                    onStateChange = { state = it },
                    onCityChange = { city = it },
                    onStreetChange = { street = it },
                    onNext = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onBack = { finish() },
                    onRequestLocation = { checkLocationPermission() }
                )
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchUserLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchUserLocation() {
        // Ativa o indicador de loading
        isLoading = true
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Usa getCurrentLocation com prioridade otimizada para mais velocidade
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    getAddressFromLocation(location)
                } else {
                    showErrorToast("Não foi possível obter as coordenadas.")
                }
            }
            .addOnFailureListener { e ->
                showErrorToast("Falha ao obter localização: ${e.message}")
            }
    }

    private fun getAddressFromLocation(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                updateAddressFields(addresses.firstOrNull())
            }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    updateAddressFields(addresses?.firstOrNull())
                } catch (e: Exception) {
                    showErrorToast("Erro ao converter coordenadas em endereço.")
                }
            }
        }
    }

    private fun updateAddressFields(address: Address?) {
        // Garante que o loading seja desativado ao final do processo
        isLoading = false
        if (address != null) {
            lifecycleScope.launch(Dispatchers.Main) {
                cep = address.postalCode ?: ""
                state = address.adminArea ?: ""
                street = address.thoroughfare ?: ""
                city = address.locality ?: address.subAdminArea ?: ""
            }
        } else {
            showErrorToast("Endereço não encontrado para esta localização.")
        }
    }

    private fun showErrorToast(message: String) {
        // Garante que o loading seja desativado em qualquer caso de erro
        isLoading = false
        lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(this@LocationActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun LocationScreen(
    cep: String,
    state: String,
    city: String,
    street: String,
    isLoading: Boolean, // Recebe o estado de loading
    onCepChange: (String) -> Unit,
    onStateChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onStreetChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onRequestLocation: () -> Unit
) {
    val country by remember { mutableStateOf("Brasil") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF6F6))
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
                onClick = onRequestLocation,
                enabled = !isLoading, // Botão é desabilitado durante o loading
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFABBD))
            ) {
                Text("Localizar automaticamente", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LocationTextField(value = cep, onValueChange = onCepChange, label = "CEP")
            LocationTextField(value = country, onValueChange = {}, label = "País", enabled = false)
            LocationTextField(value = state, onValueChange = onStateChange, label = "Estado")
            LocationTextField(value = city, onValueChange = onCityChange, label = "Cidade")
            LocationTextField(value = street, onValueChange = onStreetChange, label = "Rua")

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack, enabled = !isLoading) { // Também desabilita
                    Text("< Voltar", color = Color(0xFF9F0243), fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onNext, enabled = !isLoading) { // Também desabilita
                    Text(
                        text = if (cep.isBlank()) "Pular >" else "Finalizar!",
                        color = if (cep.isBlank()) Color(0xFF9F0243) else Color(0xFFD00036),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Exibe o Dialog de loading por cima de toda a tela se isLoading for true
        if (isLoading) {
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