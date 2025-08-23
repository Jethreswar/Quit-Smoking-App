package com.example.quitesmoking

import com.example.quitesmoking.chat.GPTMessage
import retrofit2.http.*
import retrofit2.Response

data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<GPTMessage>
)

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: GPTMessage
)

interface OpenAIService {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}
