package com.examtoquiz.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import com.examtoquiz.data.Question

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topic: String,
    val type: String,
    val difficulty: String,
    val content: String,
    val optionsJson: String, // JSON string for List<Option>
    val answer: String,
    val explanation: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        private val gson = Gson()
        fun fromQuestion(question: Question): QuestionEntity {
            return QuestionEntity(
                subject = question.subject,
                topic = question.topic,
                type = question.type,
                difficulty = question.difficulty,
                content = question.content,
                optionsJson = gson.toJson(question.options),
                answer = question.answer,
                explanation = question.explanation
            )
        }
    }
}
