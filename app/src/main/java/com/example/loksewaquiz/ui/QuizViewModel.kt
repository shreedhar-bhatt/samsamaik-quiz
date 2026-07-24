package com.example.loksewaquiz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loksewaquiz.data.QuizRepository
import com.example.loksewaquiz.data.model.QuestionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface QuizUiState {
    object Loading : QuizUiState
    data class Success(
        val quizTitle: String,
        val questions: List<QuestionDto>,
        val currentIndex: Int = 0,
        val selectedOption: Int? = null,
        val score: Int = 0,
        val isFinished: Boolean = false
    ) : QuizUiState
    data class Error(val message: String) : QuizUiState
}

class QuizViewModel(
    private val repository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadLatestQuiz()
    }

    fun loadLatestQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            repository.getLatestQuiz().fold(
                onSuccess = { quiz ->
                    _uiState.value = QuizUiState.Success(
                        quizTitle = quiz.title,
                        questions = quiz.questions
                    )
                },
                onFailure = { error ->
                    _uiState.value = QuizUiState.Error(
                        error.localizedMessage ?: "Failed to fetch quiz data"
                    )
                }
            )
        }
    }

    fun selectOption(optionIndex: Int) {
        val state = _uiState.value as? QuizUiState.Success ?: return
        if (state.selectedOption == null) {
            _uiState.value = state.copy(selectedOption = optionIndex)
        }
    }

    fun nextQuestion() {
        val state = _uiState.value as? QuizUiState.Success ?: return
        val isCorrect = state.selectedOption == state.questions[state.currentIndex].answerIndex
        val newScore = if (isCorrect) state.score + 1 else state.score
        
        if (state.currentIndex + 1 < state.questions.size) {
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                selectedOption = null,
                score = newScore
            )
        } else {
            _uiState.value = state.copy(
                score = newScore,
                isFinished = true
            )
        }
    }
}
