package com.pjt.gestacao.ui.mapa

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.GeoPoint
import com.pjt.gestacao.BuildConfig
import com.pjt.gestacao.FirebaseUtils
import com.pjt.gestacao.model.Place
import com.pjt.gestacao.network.DirectionsApiService
import com.pjt.gestacao.network.PlacesApiService
import com.pjt.gestacao.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val searchQuery: String = "",
    val selectedFilter: String = "Todos"
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
    private val apiKey = BuildConfig.Maps_key

    private var searchJob: Job? = null

    init {
        loadInitialLocationFromFirebase()
        fetchCurrentUserLocation()
    }

    private fun loadInitialLocationFromFirebase() {
        FirebaseUtils.buscarDadosGestante(
            onSuccess = { dados ->
                val geoPoint = dados?.get("ultimaLocalizacao") as? GeoPoint
                val firebaseLocation = geoPoint?.let { LatLng(it.latitude, it.longitude) }
                _uiState.update {
                    it.copy(
                        initialLocation = firebaseLocation ?: LatLng(-8.0476, -34.8770), // Fallback
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

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            searchNearbyPlaces()
        }
    }

    fun onFilterChanged(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        searchNearbyPlaces()
    }

    fun searchNearbyPlaces() {
        val currentState = _uiState.value
        val userLocation = currentState.userLocation
        if (userLocation == null) {
            return
        }

        val keyword = when (currentState.selectedFilter) {
            "Hospitais ou maternidades" -> "hospital maternidade"
            "Postos de Saúde" -> "posto de saude UBS"
            "ONGs de apoio a gestantes" -> "ONG de apoio para gestantes"
            else -> ""
        }

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

    fun onMarkerClicked(place: Place) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Loading, routePoints = emptyList()) }
            try {
                val detailsResponse = placesApiService.getPlaceDetails(place.id, apiKey = apiKey)
                val details = detailsResponse.result
                val detailedPlace = place.copy(
                    address = details.vicinity ?: "Endereço não informado",
                    phoneNumber = details.formattedPhoneNumber ?: "Telefone não informado",
                    operatingHours = details.openingHours?.weekdayText?.joinToString("\n") ?: "Horário não informado"
                )
                _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Success(detailedPlace)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Error("Não foi possível carregar os detalhes.")) }
            }
        }
    }

    fun dismissModal() {
        _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Idle, routePoints = emptyList()) }
    }

    private suspend fun findNearbyPlaces(location: LatLng, query: String) {
        val response = placesApiService.findNearbyPlaces(
            location = "${location.latitude},${location.longitude}",
            query = query,
            apiKey = apiKey
        )

        if (response.results.isEmpty()) {
            Toast.makeText(getApplication(), "Nenhum local encontrado para sua busca.", Toast.LENGTH_LONG).show()
        }

        val places = response.results.mapNotNull { result ->
            Place(
                id = result.placeId,
                name = result.name,
                type = _uiState.value.selectedFilter.takeIf { it != "Todos" } ?: result.types.firstOrNull() ?: "Local",
                latitude = result.geometry.location.lat,
                longitude = result.geometry.location.lng,
                address = result.vicinity
            )
        }
        _uiState.update { it.copy(nearbyPlaces = places) }
    }

    fun getDirectionsToPlace(place: Place) {
        viewModelScope.launch {
            traceRouteToPlace(place)
        }
    }

    private suspend fun traceRouteToPlace(destination: Place) {
        val origin = _uiState.value.userLocation ?: return
        try {
            val response = directionsApiService.getDirections(
                origin = "${origin.latitude},${origin.longitude}",
                destination = "${destination.latitude},${destination.longitude}",
                apiKey = apiKey
            )
            response.routes.firstOrNull()?.overviewPolyline?.points?.let {
                _uiState.update { ui -> ui.copy(routePoints = decodePolyline(it)) }
            }
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "Não foi possível traçar a rota.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            poly.add(LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
    }
}
