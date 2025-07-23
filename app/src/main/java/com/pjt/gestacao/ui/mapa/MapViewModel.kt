package com.pjt.gestacao.ui.mapa

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.pjt.gestacao.R
import com.pjt.gestacao.FirebaseUtils
import com.pjt.gestacao.model.Place
import com.pjt.gestacao.network.DirectionsApiService
import com.pjt.gestacao.network.PlacesApiService
import com.pjt.gestacao.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado da UI para a tela do mapa
data class MapUiState(
    val isInitializing: Boolean = true,
    val isSearching: Boolean = false,
    val userLocation: LatLng? = null,
    val nearbyPlaces: List<Place> = emptyList(),
    val selectedPlaceDetails: LocationDetailsUiState = LocationDetailsUiState.Idle,
    val routePoints: List<LatLng> = emptyList(),
    val initialLocation: LatLng? = null,
    val searchQuery: String = "", // Estado para a barra de busca
    val selectedFilter: String = "Todos" // Estado para o filtro selecionado
)

// Estados para o painel de detalhes do local
sealed interface LocationDetailsUiState {
    object Idle : LocationDetailsUiState
    object Loading : LocationDetailsUiState
    data class Success(val place: Place) : LocationDetailsUiState
    data class Error(val message: String) : LocationDetailsUiState
}

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()

    private val placesApiService: PlacesApiService = RetrofitClient.placesInstance
    private val directionsApiService: DirectionsApiService = RetrofitClient.directionsInstance
    private val apiKey = getApplication<Application>().resources.getString(R.string.google_api_key)

    init {
        loadInitialLocationFromFirebase()
        fetchCurrentUserLocation()
    }

    // Carrega a localização inicial salva no Firebase
    private fun loadInitialLocationFromFirebase() {
        FirebaseUtils.buscarDadosGestante(
            onSuccess = { dados ->
                val geoPoint = dados?.get("ultimaLocalizacao") as? GeoPoint
                val firebaseLocation = geoPoint?.let { LatLng(it.latitude, it.longitude) }
                _uiState.update {
                    it.copy(
                        initialLocation = firebaseLocation ?: LatLng(-8.0476, -34.8770), // Fallback para Recife
                        isInitializing = false
                    )
                }
            },
            onFailure = {
                _uiState.update {
                    it.copy(
                        initialLocation = LatLng(-8.0476, -34.8770), // Fallback
                        isInitializing = false
                    )
                }
            }
        )
    }

    // Busca a localização atual do usuário
    fun fetchCurrentUserLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication())
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        _uiState.update { it.copy(userLocation = userLatLng) }
                    }
                }
        } catch (e: SecurityException) {
            _uiState.update { it.copy(isInitializing = false) }
        }
    }

    // Atualiza o estado da query de busca
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Atualiza o estado do filtro selecionado
    fun onFilterChanged(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // Busca locais próximos com base na query e no filtro
    fun searchNearbyPlaces() {
        val currentState = _uiState.value
        val userLocation = currentState.userLocation ?: run {
            Toast.makeText(getApplication(), "Localização do usuário ainda não encontrada.", Toast.LENGTH_SHORT).show()
            return
        }

        val keyword = when (currentState.selectedFilter) {
            "Hospitais ou maternidades" -> "hospital maternidade"
            "Postos de Saúde" -> "posto de saude UBS"
            "ONGs de apoio a gestantes" -> "ong apoio gestante"
            else -> "" // "Todos"
        }

        // Combina a query de texto com a palavra-chave do filtro
        val finalQuery = "${currentState.searchQuery} $keyword".trim()
        if (finalQuery.isBlank()) {
            _uiState.update { it.copy(nearbyPlaces = emptyList()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, nearbyPlaces = emptyList(), routePoints = emptyList()) }
            try {
                findNearbyPlaces(userLocation, finalQuery)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Erro ao buscar locais.", Toast.LENGTH_SHORT).show()
            } finally {
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }

    // Busca detalhes de um local quando o marcador é clicado
    fun onMarkerClicked(place: Place) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Loading) }
            try {
                val detailsResponse = placesApiService.getPlaceDetails(place.id, apiKey = apiKey)
                val details = detailsResponse.result
                val detailedPlace = place.copy(
                    operatingHours = details.openingHours?.weekdayText?.joinToString("\n") ?: "Horário não informado",
                )
                _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Success(detailedPlace)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Error("Não foi possível carregar os detalhes.")) }
            }
        }
    }

    // Fecha o painel de detalhes
    fun dismissModal() {
        _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Idle) }
    }

    // Função interna para chamar a API do Google Places
    private suspend fun findNearbyPlaces(location: LatLng, query: String) {
        val response = placesApiService.findNearbyPlaces(
            location = "${location.latitude},${location.longitude}",
            keyword = query,
            apiKey = apiKey
        )
        val places = response.results.mapNotNull { result ->
            Place(
                id = result.placeId,
                name = result.name,
                // Associa o tipo baseado no filtro atual para o ícone
                type = _uiState.value.selectedFilter.takeIf { it != "Todos" } ?: result.types.firstOrNull() ?: "Local",
                latitude = result.geometry.location.lat,
                longitude = result.geometry.location.lng,
                operatingHours = if (result.openingHours?.openNow == true) "Aberto agora" else "Verificar horário"
            )
        }
        _uiState.update { it.copy(nearbyPlaces = places) }
    }
}
