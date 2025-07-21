package com.pjt.gestacao.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Objeto singleton para gerir a criação de instâncias do Retrofit para as diferentes APIs
 * utilizadas na aplicação (Flask e Google Maps).
 */
object RetrofitClient {

    // --- Configuração do Cliente HTTP (Compartilhada por todas as APIs) ---

    // Interceptor para logar as requisições e respostas no Logcat
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente OkHttp com timeouts personalizados para evitar falhas em redes lentas
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()


    // --- Configuração para a API do Chat (Flask via Ngrok) ---

    private const val FLASK_BASE_URL = "https://dad3-131-108-86-151.ngrok-free.app/"

    private val flaskRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(FLASK_BASE_URL)
            .client(httpClient) // Reutiliza o cliente HTTP configurado
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Instância do serviço para a API do Chat (Flask).
     * Manteve-se o nome 'instance' para compatibilidade com o código existente.
     */
    val instance: FlaskChatApiService by lazy {
        flaskRetrofit.create(FlaskChatApiService::class.java)
    }


    // --- Configuração para as APIs do Google Maps ---

    private const val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/"

    private val googleMapsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GOOGLE_MAPS_BASE_URL)
            .client(httpClient) // Reutiliza o mesmo cliente HTTP
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Instância do serviço para a Google Places API.
     * Use esta variável para buscar locais.
     */
    val placesInstance: PlacesApiService by lazy {
        googleMapsRetrofit.create(PlacesApiService::class.java)
    }

    /**
     * Instância do serviço para a Google Directions API.
     * Use esta variável para traçar rotas.
     */
    val directionsInstance: DirectionsApiService by lazy {
        googleMapsRetrofit.create(DirectionsApiService::class.java)
    }
}