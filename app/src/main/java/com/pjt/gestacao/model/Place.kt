package com.pjt.gestacao.model

/**
 * Representa um local de interesse no mapa, como um hospital, ONG ou posto de saúde.
 *
 * @property id O identificador único do local (ex: Google Place ID).
 * @property name O nome do local a ser exibido.
 * @property type A categoria do local (ex: "Hospital", "ONG", "Posto de Saúde").
 * @property latitude A coordenada de latitude do local.
 * @property longitude A coordenada de longitude do local.
 * @property operatingHours O horário de funcionamento (opcional, pode ser buscado depois).
 */
data class Place(
    val id: String,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val operatingHours: String? = null
)