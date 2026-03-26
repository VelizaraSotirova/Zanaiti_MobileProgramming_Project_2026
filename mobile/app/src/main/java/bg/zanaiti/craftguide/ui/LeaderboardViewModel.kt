package bg.zanaiti.craftguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.zanaiti.craftguide.models.LeaderboardEntry
import bg.zanaiti.craftguide.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.apiService.getLeaderboard()
                _leaderboard.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "Грешка при зареждане на класацията"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadLeaderboard()
    }
}