package com.pjt.gestacao.ui.mapa

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pjt.gestacao.BuildConfig
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.GeoPoint
import com.pjt.gestacao.FirebaseUtils
import com.pjt.gestacao.model.Place
import com.pjt.gestacao.network.DirectionsApiService
import com.pjt.gestacao.network.PlacesApiService
import com.pjt.gestacao.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    init {
        loadInitialLocationFromFirebase()
    }

    private fun loadInitialLocationFromFirebase() {
        FirebaseUtils.buscarDadosGestante(
            onSuccess = { dados ->
                val geoPoint = dados?.get("ultimaLocalizacao") as? GeoPoint
                val firebaseLocation = geoPoint?.let { LatLng(it.latitude, it.longitude) }
                _uiState.update { it.copy(initialLocation = firebaseLocation ?: LatLng(-8.0476, -34.8770)) }
                fetchCurrentUserLocation()
            },
            onFailure = {
                _uiState.update { it.copy(initialLocation = LatLng(-8.0476, -34.8770)) }
                fetchCurrentUserLocation()
            }
        )
    }

    internal fun fetchCurrentUserLocation() {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication())
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        _uiState.update { it.copy(userLocation = userLatLng, isInitializing = false) }
                        // Não fazemos mais busca automática aqui para evitar sobrecarga
                    } else {
                        _uiState.update { it.copy(isInitializing = false) }
                    }
                }.addOnFailureListener {
                    _uiState.update { it.copy(isInitializing = false) }
                }
        } catch (e: SecurityException) {
            _uiState.update { it.copy(isInitializing = false) }
        }
    }

    fun onFilterChanged(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter, routePoints = emptyList()) }
        searchNearbyPlaces()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchNearbyPlaces() {
        val currentState = _uiState.value
        val userLocation = currentState.userLocation ?: return

        val keyword = when (currentState.selectedFilter) {
            "Hospitais ou maternidades" -> "hospital OR maternidade OR pronto socorro"
            "Postos de Saúde" -> "posto de saude OR UBS OR UPA OR samu"
            "ONGs de apoio a gestantes" -> "ong apoio gestante"
            // O "Todos" agora é a junção de todos os termos importantes
            else -> "hospital OR maternidade OR pronto socorro OR posto de saude OR UBS OR UPA OR samu OR ong apoio gestante"
        }

        val finalQuery = "${currentState.searchQuery} $keyword".trim()

        if (finalQuery.isBlank()) {
            _uiState.update { it.copy(nearbyPlaces = emptyList(), isSearching = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, nearbyPlaces = emptyList()) }
            try {
                val response = placesApiService.findNearbyPlaces(
                    location = "${userLocation.latitude},${userLocation.longitude}",
                    keyword = finalQuery,
                    apiKey = apiKey
                )
                val places = response.results.mapNotNull { result ->
                    Place(
                        id = result.placeId, name = result.name,
                        type = result.types.firstOrNull() ?: "Local",
                        latitude = result.geometry.location.lat, longitude = result.geometry.location.lng,
                        operatingHours = if (result.openingHours?.openNow == true) "Aberto agora" else "Verificar horário"
                    )
                }

                // --- INÍCIO DA CORREÇÃO ---

                if (places.isNotEmpty()) {
                    // 1. Limita o número de resultados para evitar sobrecarga na UI
                    val limitedPlaces = places.take(20)

                    // 2. Encontra o local mais próximo dentro da lista limitada
                    val nearestPlace = findNearestPlace(userLocation, limitedPlaces)

                    // 3. Atualiza a UI com os marcadores limitados
                    _uiState.update { it.copy(nearbyPlaces = limitedPlaces) }

                    // 4. Se encontrou um local próximo, traça a rota até ele
                    nearestPlace?.let { traceRouteTo(it) }

                } else {
                    _uiState.update { it.copy(nearbyPlaces = emptyList()) }
                }

                // --- FIM DA CORREÇÃO ---

            } catch (e: Exception) {
                Log.e("MapViewModel", "Erro na busca: ${e.message}")
            } finally {
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }

    fun onMarkerClicked(place: Place) {
        _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Success(place)) }
    }

    fun traceRouteTo(destination: Place) {
        val origin = _uiState.value.userLocation ?: return
        val destinationLatLng = LatLng(destination.latitude, destination.longitude)
        viewModelScope.launch {
            try {
                val response = directionsApiService.getDirections(
                    origin = "${origin.latitude},${origin.longitude}",
                    destination = "${destinationLatLng.latitude},${destinationLatLng.longitude}",
                    apiKey = apiKey
                )
                val points = response.routes.firstOrNull()?.overviewPolyline?.points
                if (points != null) {
                    val decodedPath = decodePolyline(points)
                    _uiState.update { it.copy(routePoints = decodedPath) }
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Erro ao traçar rota: ${e.message}")
            }
        }
    }

    // ✅ NOVA FUNÇÃO AUXILIAR PARA ENCONTRAR O LOCAL MAIS PRÓXIMO
    private fun findNearestPlace(userLocation: LatLng, places: List<Place>): Place? {
        return places.minByOrNull { place ->
            val placeLocation = Location("place_location").apply {
                latitude = place.latitude
                longitude = place.longitude
            }
            val userLoc = Location("user_location").apply {
                latitude = userLocation.latitude
                longitude = userLocation.longitude
            }
            userLoc.distanceTo(placeLocation) // Retorna a distância em metros
        }
    }

    fun dismissModal() {
        _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Idle) }
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
            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }
}