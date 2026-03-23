package bg.zanaiti.craftguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.zanaiti.craftguide.models.AnswerCheckRequest
import bg.zanaiti.craftguide.models.QuizQuestion
import bg.zanaiti.craftguide.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizViewModel(
    private val isLoggedIn: Boolean = false,
    private val userId: Long? = null,
    private val craftId: Long
) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val questions: StateFlow<List<QuizQuestion>> = _questions

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted

    private val _selectedOption = MutableStateFlow<Int?>(null)
    val selectedOption: StateFlow<Int?> = _selectedOption

    private val _feedback = MutableStateFlow<String?>(null)
    val feedback: StateFlow<String?> = _feedback

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getQuestionsForCraft(craftId, "bg")
                _questions.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectOption(index: Int) {
        _selectedOption.value = index
    }

    fun submitAnswer() {
        val question = _questions.value.getOrNull(_currentIndex.value) ?: return
        val selected = _selectedOption.value ?: return

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.checkAnswer(
                    question.id,
                    selectedOption = selected
                )
                _feedback.value = response.message

                if (response.correct) {
                    _score.value += question.pointsReward
                    _correctCount.value += 1

                    // ✅ АКО Е ЛОГНАТ, ПРАЩАМЕ ТОЧКИТЕ КЪМ БЕКЕНДА
                    if (isLoggedIn && userId != null) {
                        sendPointsToBackend(craftId, _correctCount.value)
                    }
                }

                // Изчакваме 1.5 секунди, за да покажем съобщението
                kotlinx.coroutines.delay(1500)
                _feedback.value = null
                _selectedOption.value = null

                // Преминаваме към следващия въпрос
                if (_currentIndex.value + 1 < _questions.value.size) {
                    _currentIndex.value += 1
                } else {
                    _quizCompleted.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun sendPointsToBackend(craftId: Long, correctAnswersCount: Int) {
        try {
            val response = RetrofitClient.apiService.completeQuiz(
                userId = userId!!,
                craftId = craftId,
                correctAnswersCount = correctAnswersCount,
                lang = "bg"
            )
            if (response.isSuccessful) {
                println("✅ Точките са записани успешно!")
            } else {
                println("Грешка при запис на точки: ${response.code()}")
            }
        } catch (e: Exception) {
            println("Грешка при запис на точки: ${e.message}")
            e.printStackTrace()
        }
    }

    fun reset() {
        _currentIndex.value = 0
        _score.value = 0
        _correctCount.value = 0
        _selectedOption.value = null
        _feedback.value = null
        _quizCompleted.value = false
    }
}