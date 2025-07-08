package com.pjt.gestacao.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Data class para o corpo da REQUISIÇÃO que o Kotlin enviará
data class FlaskApiRequestBody(
    val user_id: String,
    val message: String
)

data class FlaskApiResponseBody(
    val response: String

)

// )

// Interface do serviço
interface FlaskChatApiService {
    @POST("chat") // O endpoint é apenas "/chat" relativo à BASE_URL
    fun sendMessageToFlask(@Body requestBody: FlaskApiRequestBody): Call<FlaskApiResponseBody>
}