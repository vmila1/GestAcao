package com.pjt.gestacao.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // Import necessário para TimeUnit

object RetrofitClient {
    // Certifique-se de que esta é a sua URL ATIVA do ngrok
    private const val BASE_URL = "https://d31b-131-108-86-151.ngrok-free.app/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configurando o OkHttpClient com timeouts personalizados
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Timeout para estabelecer a conexão
        .readTimeout(30, TimeUnit.SECONDS)    // Timeout para ler a resposta do servidor
        .writeTimeout(30, TimeUnit.SECONDS)   // Timeout para enviar a requisição ao servidor
        .build()

    val instance: FlaskChatApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // Usa o httpClient com timeouts personalizados
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(FlaskChatApiService::class.java)
    }
}