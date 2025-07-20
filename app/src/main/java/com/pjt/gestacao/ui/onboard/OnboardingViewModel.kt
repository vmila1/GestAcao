package com.pjt.gestacao.ui.onboard

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.pjt.gestacao.FirebaseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class OnboardingViewModel : ViewModel() {

    // --- Estados para guardar os dados coletados ---
    var mes by mutableStateOf<Int?>(null)
    var genero by mutableStateOf<String?>(null)
    // Coordenadas precisas
    var latitude by mutableStateOf(0.0)
    var longitude by mutableStateOf(0.0)
    // Campos de endereço para a UI
    var cep by mutableStateOf("")
    var estado by mutableStateOf("")
    var cidade by mutableStateOf("")
    var rua by mutableStateOf("")


    // --- Estados para controlar a UI ---
    var isLoading by mutableStateOf(false)
        private set

    // --- Eventos para a Activity (navegação) ---
    private val _onboardingFinished = MutableSharedFlow<Unit>()
    val onboardingFinished = _onboardingFinished.asSharedFlow()


    // --- Funções de Lógica ---
    fun fetchUserLocation(context: Context) {
        isLoading = true
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Salva as coordenadas precisas
                        latitude = location.latitude
                        longitude = location.longitude
                        // Inicia a conversão para endereço
                        getAddressFromLocation(location, context)
                    } else {
                        showError("Não foi possível obter as coordenadas.", context)
                    }
                }
                .addOnFailureListener {
                    showError("Falha ao obter localização.", context)
                }
        } catch (e: SecurityException) {
            showError("Permissão de localização negada.", context)
        }
    }

    private fun getAddressFromLocation(location: Location, context: Context) {
        val geocoder = Geocoder(context, Locale.getDefault())
        // A lógica de geocodificação foi movida para cá
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                updateAddressFields(addresses.firstOrNull())
            }
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    updateAddressFields(addresses?.firstOrNull())
                } catch (e: Exception) {
                    showError("Erro ao converter coordenadas.", context)
                }
            }
        }
    }

    private fun updateAddressFields(address: Address?) {
        if (address != null) {
            // Atualiza os estados que a UI está observando
            cep = address.postalCode ?: ""
            estado = address.adminArea ?: ""
            rua = address.thoroughfare ?: ""
            cidade = address.locality ?: address.subAdminArea ?: ""
        }
        isLoading = false
    }

    fun saveOnboardingData() {
        if (mes == null || genero == null) return
        isLoading = true

        FirebaseUtils.salvarDadosGestante(
            meses = mes!!,
            generoBebe = genero,
            latitude = latitude,
            longitude = longitude,
            onSuccess = {
                viewModelScope.launch {
                    _onboardingFinished.emit(Unit)
                }
                isLoading = false
            },
            onFailure = {
                isLoading = false
            }
        )
    }

    private fun showError(message: String, context: Context) {
        isLoading = false
        // Garante que o Toast seja executado na thread principal
        viewModelScope.launch(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}