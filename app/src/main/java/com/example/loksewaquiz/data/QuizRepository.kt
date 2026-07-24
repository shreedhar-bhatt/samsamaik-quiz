package com.example.loksewaquiz.data

import com.example.loksewaquiz.data.model.QuizDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuizRepository {
    private val client = SupabaseProvider.client

    suspend fun getLatestQuiz(): Result<QuizDto> = withContext(Dispatchers.IO) {
        try {
            val quiz = client.postgrest["loksewa_quizzes"]
                .select {
                    order(column = "created_at", order = Order.DESCENDING)
                    limit(1)
                }
                .decodeSingle<QuizDto>()
            Result.success(quiz)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
