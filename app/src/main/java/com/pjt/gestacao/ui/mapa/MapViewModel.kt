package com.pjt.gestacao.ui.mapa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.LatLng
import com.pjt.gestacao.R
import com.pjt.gestacao.model.Place
import com.pjt.gestacao.network.DirectionsApiService
import com.pjt.gestacao.network.PlacesApiService
import com.pjt.gestacao.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Define todos os dados que a UI do Mapa precisa para se desenhar.
 */
data class MapUiState(
    val isInitializing: Boolean = true,
    val userLocation: LatLng? = null,
    val nearbyPlaces: List<Place> = emptyList(),
    val selectedPlaceDetails: LocationDetailsUiState = LocationDetailsUiState.Idle,
    val routePoints: List<LatLng> = emptyList(),
    val initialLocation: LatLng = LatLng(-8.0238, -34.9567)
)

/**
 * Define os estados do modal de detalhes do local.
 */
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

    // Obtém a chave da API a partir dos recursos de string (injetada pelo plugin)
    private val apiKey = getApplication<Application>().resources.getString(R.string.google_maps_key)

    fun fetchCurrentUserLocation() {
        val context = getApplication<Application>().applicationContext
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        _uiState.update { it.copy(userLocation = userLatLng, isInitializing = false) }
                        viewModelScope.launch {
                            findNearbyPlaces(userLatLng, "hospital")
                        }
                    }
                }
        } catch (e: SecurityException) {
            _uiState.update { it.copy(isInitializing = false) }
        }
    }

    /**
     * Busca locais de um certo tipo e traça a rota para o mais próximo.
     */
    fun findAndRouteToNearest(placeType: String) {
        val userLocation = _uiState.value.userLocation ?: return

        val keyword = when (placeType) {
            "Hospitais" -> "hospital"
            "Postos de Saúde" -> "posto de saude"
            "ONGs de apoio a gestantes" -> "ong apoio gestante"
            else -> ""
        }

        viewModelScope.launch {
            try {
                // Busca os locais
                val places = findNearbyPlaces(userLocation, keyword)
                if (places.isNotEmpty()) {
                    // Encontra o mais próximo (neste exemplo, o primeiro da lista)
                    val nearestPlace = places.first()
                    // Traça a rota
                    traceRouteToPlace(nearestPlace)
                }
            } catch (e: Exception) {
                // Lidar com erro
            }
        }
    }

    /**
     * Chamado quando o usuário clica em um pino no mapa.
     */
    fun onMarkerClicked(place: Place) {
        _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Success(place)) }
    }

    /**
     * Fecha o modal de detalhes.
     */
    fun dismissModal() {
        _uiState.update { it.copy(selectedPlaceDetails = LocationDetailsUiState.Idle) }
    }

    private suspend fun findNearbyPlaces(location: LatLng, keyword: String): List<Place> {
        val response = placesApiService.findNearbyPlaces(
            location = "${location.latitude},${location.longitude}",
            keyword = keyword,
            apiKey = apiKey
        )
        val places = response.results.mapNotNull { result ->
            Place(
                id = result.placeId,
                name = result.name,
                type = keyword,
                latitude = result.geometry.location.lat,
                longitude = result.geometry.location.lng,
                operatingHours = if (result.openingHours?.openNow == true) "Aberto agora" else "Verificar horário"
            )
        }
        _uiState.update { it.copy(nearbyPlaces = it.nearbyPlaces + places) }
        return places
    }

    private suspend fun traceRouteToPlace(destination: Place) {
        val origin = _uiState.value.userLocation ?: return
        val destinationLatLng = LatLng(destination.latitude, destination.longitude)

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
            // Lidar com erro de rota
        }
    }

    /**
     * Função auxiliar para decodificar a polyline da API do Google Directions.
     */
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