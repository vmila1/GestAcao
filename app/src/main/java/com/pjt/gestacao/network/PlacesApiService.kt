package com.pjt.gestacao.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface do Retrofit para interagir com a Google Places API.
 */
interface PlacesApiService {

    /**
     * Busca locais próximos (hospitais, ONGs, etc.) com base em uma coordenada.
     *
     * @param location A localização central da busca, no formato "latitude,longitude".
     * @param radius O raio da busca em metros. O padrão é 5000 (5km).
     * @param keyword O termo de busca (ex: "hospital", "posto de saude", "ONG apoio gestante").
     * @param apiKey Sua chave da API do Google Maps.
     * @return Um objeto [PlacesResponse] contendo a lista de locais encontrados.
     */
    @GET("maps/api/place/nearbysearch/json")
    suspend fun findNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int = 5000,
        @Query("keyword") keyword: String,
        @Query("key") apiKey: String
    ): PlacesResponse
}

// --- Classes de Dados para Mapear a Resposta JSON da API ---

/**
 * Representa o objeto de nível superior da resposta da API de busca.
 */
data class PlacesResponse(
    @SerializedName("results") val results: List<PlaceResult>,
    @SerializedName("status") val status: String
)

/**
 * Representa um único local retornado na lista de resultados.
 */
data class PlaceResult(
    @SerializedName("place_id") val placeId: String,
    @SerializedName("name") val name: String,
    @SerializedName("geometry") val geometry: Geometry,
    @SerializedName("vicinity") val vicinity: String?, // Endereço simplificado
    @SerializedName("opening_hours") val openingHours: OpeningHours?
)

/**
 * Contém as informações de geolocalização.
 */
data class Geometry(
    @SerializedName("location") val location: ApiLocation
)

/**
 * Contém as coordenadas de latitude e longitude.
 */
data class ApiLocation(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)

/**
 * Contém a informação se o local está aberto no momento da busca.
 */
data class OpeningHours(
    @SerializedName("open_now") val openNow: Boolean?
)