package com.pjt.gestacao.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface do Retrofit para a Google Directions API.
 */
interface DirectionsApiService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String
    ): DirectionsResponse
}

// --- Classes de Dados para a Resposta da Directions API ---

data class DirectionsResponse(
    @SerializedName("routes") val routes: List<Route>
)

data class Route(
    @SerializedName("overview_polyline") val overviewPolyline: OverviewPolyline
)

data class OverviewPolyline(
    @SerializedName("points") val points: String
)