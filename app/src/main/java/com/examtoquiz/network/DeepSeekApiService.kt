package com.examtoquiz.network

import retrofit2.*
import retrofit2.http.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class DeepSeekRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)

data class DeepSeekResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

class DeepSeekApiService {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${System.getenv("DEEPSEEK_API_KEY") ?: "YOUR_API_KEY"}")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.deepseek.com/v1/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(DeepSeekApi::class.java)

    suspend fun sendMessage(message: String): String {
        return withContext(Dispatchers.IO) {
            val request = DeepSeekRequest(
                messages = listOf(Message(role = "user", content = message))
            )
            
            try {
                val response = api.sendMessage(request)
                response.choices.firstOrNull()?.message?.content ?: "抱歉，无法获取回复"
            } catch (e: Exception) {
                "请求失败: ${e.message}"
            }
        }
    }
}

interface DeepSeekApi {
    @POST("chat/completions")
    suspend fun sendMessage(@Body request: DeepSeekRequest): DeepSeekResponse
}
