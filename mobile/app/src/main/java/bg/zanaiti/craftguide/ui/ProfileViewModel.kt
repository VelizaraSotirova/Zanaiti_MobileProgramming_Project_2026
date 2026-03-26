package bg.zanaiti.craftguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.zanaiti.craftguide.models.PointsHistoryItem
import bg.zanaiti.craftguide.models.UserProfile
import bg.zanaiti.craftguide.models.UserStats
import bg.zanaiti.craftguide.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userId: Long
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    private val _stats = MutableStateFlow<UserStats?>(null)
    val stats: StateFlow<UserStats?> = _stats

    private val _pointsHistory = MutableStateFlow<List<PointsHistoryItem>>(emptyList())
    val pointsHistory: StateFlow<List<PointsHistoryItem>> = _pointsHistory

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        if (userId > 0) {
            loadProfile()
            loadStats()
            loadPointsHistory()
        } else {
            _isLoading.value = false
            _error.value = "Невалиден потребителски профил"
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getMyProfile()
                _profile.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "Грешка при зареждане на профила"
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getUserStats(userId)
                _stats.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "Грешка при зареждане на статистика"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadPointsHistory() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getPointsHistory(userId)
                _pointsHistory.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "Грешка при зареждане на история"
            }
        }
    }

    fun refresh() {
        _isLoading.value = true
        loadProfile()
        loadStats()
        loadPointsHistory()
    }
}