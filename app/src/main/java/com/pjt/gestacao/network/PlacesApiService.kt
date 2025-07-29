package com.pjt.gestacao.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/textsearch/json")
    suspend fun findNearbyPlaces(
        @Query("query") query: String,
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000,
        @Query("key") apiKey: String
    ): PlacesResponse

    @GET("maps/api/place/details/json")
    suspend fun getPlaceDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "name,opening_hours,formatted_phone_number,vicinity",
        @Query("key") apiKey: String
    ): PlaceDetailsResponse
}

data class PlacesResponse(@SerializedName("results") val results: List<PlaceResult>)

data class PlaceResult(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("opening_hours") val openingHours: OpeningHours?,
    @SerializedName("types") val types: List<String> = emptyList(),
    @SerializedName("vicinity") val vicinity: String?
)

data class Geometry(@SerializedName("location") val location: ApiLocation)
data class ApiLocation(@SerializedName("lat") val lat: Double, @SerializedName("lng") val lng: Double)

data class PlaceDetailsResponse(@SerializedName("result") val result: PlaceDetailsResult)

data class PlaceDetailsResult(
    @SerializedName("name") val name: String,
    @SerializedName("opening_hours") val openingHours: OpeningHours?,
    @SerializedName("formatted_phone_number") val formattedPhoneNumber: String?,
    @SerializedName("vicinity") val vicinity: String?
)

data class OpeningHours(
    @SerializedName("open_now") val openNow: Boolean?,
    @SerializedName("weekday_text") val weekdayText: List<String>?
)
