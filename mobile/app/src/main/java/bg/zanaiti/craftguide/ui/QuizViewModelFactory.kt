package bg.zanaiti.craftguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuizViewModelFactory(
    private val isLoggedIn: Boolean,
    private val userId: Long?,
    private val craftId: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(isLoggedIn, userId, craftId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}