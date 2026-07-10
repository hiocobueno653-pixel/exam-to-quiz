package com.examtoquiz.data

import com.google.gson.annotations.SerializedName

data class StatsResponse(
    val total: Int = 0,
    @SerializedName("today_count") val todayCount: Int = 0,
    val sources: Int = 0,
    @SerializedName("wrong_count") val wrongCount: Int = 0,
    @SerializedName("favorite_count") val favoriteCount: Int = 0,
    val subjects: List<SubjectStat> = emptyList(),
    @SerializedName("seven_days") val sevenDays: List<DayStat> = emptyList(),
    @SerializedName("daily_goal") val dailyGoal: DailyGoal = DailyGoal(),
    val recent: List<Question> = emptyList()
)

data class DayStat(
    val date: String = "",
    val count: Int = 0
)

data class DailyGoal(
    val target: Int = 20,
    val completed: Int = 0,
    val streak: Int = 0
)

data class SubjectStat(
    val name: String = "",
    val count: Int = 0
)

data class Question(
    val id: Int = 0,
    val subject: String = "",
    val type: String = "",
    val content: String = "",
    val options: List<Option>? = null,
    val answer: String = "",
    val analysis: String = "",
    val difficulty: String = "中等",
    val source: String = "",
    @SerializedName("is_favorite") val isFavorite: Boolean = false,
    @SerializedName("is_wrong") val isWrong: Boolean = false,
    @SerializedName("created_at") val createdAt: String = ""
)

data class Option(
    val label: String = "",
    val text: String = ""
)

data class FiltersResponse(
    val subjects: List<String> = emptyList(),
    val types: List<String> = emptyList(),
    val difficulties: List<String> = emptyList(),
    val sources: List<String> = emptyList()
)

data class QuestionListResponse(
    val questions: List<Question> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 1,
    val filters: FiltersResponse = FiltersResponse()
)

data class AiGenerateRequest(
    val topic: String,
    val subject: String = "通用",
    val count: Int = 5,
    val types: String = "单选,多选,判断"
)

data class AiParseRequest(
    val text: String
)

data class AiChatRequest(
    val message: String,
    val history: List<ChatMessage> = emptyList()
)

data class AiResponse(
    val questions: List<Question>? = null,
    val reply: String? = null
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class QuizGenerateRequest(
    val subjects: List<String>,
    val types: List<String>,
    val difficulties: List<String>,
    val count: Int = 10
)

data class QuizGradeRequest(
    val answers: Map<String, String>
)

data class QuizResult(
    val score: Int = 0,
    val correct: Int = 0,
    val total: Int = 0,
    val results: List<QuizItemResult> = emptyList()
)

data class QuizItemResult(
    val id: Int = 0,
    val type: String = "",
    val content: String = "",
    @SerializedName("correct_answer") val correctAnswer: String = "",
    @SerializedName("user_answer") val userAnswer: String = "",
    @SerializedName("is_correct") val isCorrect: Boolean = false,
    val analysis: String = ""
)

data class OcrResponse(
    val text: String = "",
    val questions: List<Question> = emptyList()
)

data class BatchRequest(
    val ids: List<Int>,
    val action: String
)

data class ImportRequest(
    val questions: List<Question>
)

data class ErrorResponse(
    val error: String = ""
)