package com.example.loksewaquiz.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    val id: String,
    val title: String,
    val questions: List<QuestionDto>,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class QuestionDto(
    val id: Int,
    val question: String,
    val options: List<String>,
    @SerialName("answer_index") val answerIndex: Int
)
