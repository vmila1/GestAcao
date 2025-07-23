package com.pjt.gestacao.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {

    /**
     * Busca locais próximos com base em uma coordenada e palavra-chave.
     */
    @GET("maps/api/place/nearbysearch/json")
    suspend fun findNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000, // Raio de busca um pouco maior
        @Query("keyword") keyword: String,
        @Query("key") apiKey: String
    ): PlacesResponse

    /**
     * Busca os detalhes de um local específico usando seu Place ID.
     * Essencial para obter informações ricas como telefone e horários completos.
     */
    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        // Define os campos que queremos para otimizar a chamada
        @Query("fields") fields: String = "name,opening_hours,formatted_phone_number,vicinity",
        @Query("key") apiKey: String
    ): PlaceDetailsResponse
}


// --- Classes de Dados para a Resposta da API ---

data class PlacesResponse(
    @SerializedName("results") val results: List<PlaceResult>
)

data class PlaceResult(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("opening_hours") val openingHours: OpeningHours?,
    // Adicionado para ajudar a identificar o tipo do local
    @SerializedName("types") val types: List<String> = emptyList()
)

data class PlaceDetailsResponse(
    @SerializedName("result") val result: PlaceDetailsResult
)

data class PlaceDetailsResult(
    @SerializedName("name") val name: String,
    @SerializedName("opening_hours") val openingHours: OpeningHours?,
    @SerializedName("formatted_phone_number") val formattedPhoneNumber: String?,
    @SerializedName("vicinity") val vicinity: String?
)

data class Geometry(
    @SerializedName("location") val location: ApiLocation
)

data class ApiLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

data class OpeningHours(
    @SerializedName("open_now") val openNow: Boolean?,
    // Adicionado para mostrar os horários da semana
    @SerializedName("weekday_text") val weekdayText: List<String>?
)