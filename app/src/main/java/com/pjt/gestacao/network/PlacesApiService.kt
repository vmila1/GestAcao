package com.pjt.gestacao.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun findNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 10000, // Raio de busca aumentado
        @Query("keyword") keyword: String,
        @Query("key") apiKey: String
    ): PlacesResponse

    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "name,opening_hours,formatted_phone_number,vicinity", // Adicionado 'vicinity' para endereço
        @Query("key") apiKey: String
    ): PlaceDetailsResponse
}

data class PlacesResponse(@SerializedName("results") val results: List<PlaceResult>)

data class PlaceResult(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("opening_hours") val openingHours: OpeningHours?,
    @SerializedName("types") val types: List<String> = emptyList() // Adicionado para identificar o tipo do local
)

data class Geometry(@SerializedName("location") val location: ApiLocation)
data class ApiLocation(@SerializedName("lat") val lat: Double, @SerializedName("lng") val lng: Double)

data class PlaceDetailsResponse(@SerializedName("result") val result: PlaceDetailsResult)

data class PlaceDetailsResult(
    @SerializedName("name") val name: String,
    @SerializedName("opening_hours") val openingHours: OpeningHours?,
    @SerializedName("formatted_phone_number") val formattedPhoneNumber: String?,
    @SerializedName("vicinity") val vicinity: String? // Endereço/vizinhança
)

data class OpeningHours(
    @SerializedName("open_now") val openNow: Boolean?,
    @SerializedName("weekday_text") val weekdayText: List<String>?
)
