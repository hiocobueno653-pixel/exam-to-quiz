package com.examtoquiz.network

import com.examtoquiz.data.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    @GET("api/stats")
    suspend fun getStats(): StatsResponse

    @GET("api/filters")
    suspend fun getFilters(): FiltersResponse

    @GET("api/questions")
    suspend fun getQuestions(
        @Query("page") page: Int = 1,
        @Query("subject") subject: String? = null,
        @Query("type") type: String? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("source") source: String? = null,
        @Query("search") search: String? = null
    ): QuestionListResponse

    @POST("api/questions")
    suspend fun createQuestion(@Body question: Question): Question

    @POST("api/questions/batch")
    suspend fun batchOperation(@Body request: BatchRequest): Map<String, Any>

    @POST("api/questions/import")
    suspend fun importQuestions(@Body request: ImportRequest): Map<String, Any>

    @Multipart
    @POST("api/ocr")
    suspend fun ocrFile(@Part file: MultipartBody.Part): OcrResponse

    @POST("api/ai/generate")
    suspend fun aiGenerate(@Body request: AiGenerateRequest): AiResponse

    @POST("api/ai/parse")
    suspend fun aiParse(@Body request: AiParseRequest): AiResponse

    @POST("api/ai/chat")
    suspend fun aiChat(@Body request: AiChatRequest): AiResponse

    @POST("api/quiz/generate")
    suspend fun generateQuiz(@Body request: QuizGenerateRequest): List<Question>

    @POST("api/quiz/grade")
    suspend fun gradeQuiz(@Body request: QuizGradeRequest): QuizResult

    companion object {
        // 改这个地址为你的 VPS 公网 IP
        private const val BASE_URL = "http://120.48.34.119:5000/"

        fun create(baseUrl: String = BASE_URL): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}