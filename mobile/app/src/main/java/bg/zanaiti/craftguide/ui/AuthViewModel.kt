package bg.zanaiti.craftguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.zanaiti.craftguide.models.LoginRequest
import bg.zanaiti.craftguide.models.RegisterRequest
import bg.zanaiti.craftguide.network.RetrofitClient
import bg.zanaiti.craftguide.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _userId = MutableStateFlow<Long?>(null)
    val userId: StateFlow<Long?> = _userId

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    init {
        // Слушаме за промени в токена
        viewModelScope.launch {
            tokenManager.tokenFlow.collect { token ->
                _isAuthenticated.value = !token.isNullOrBlank()
            }
        }

        // Слушаме за промени в потребителското име
        viewModelScope.launch {
            tokenManager.usernameFlow.collect { name ->
                _username.value = name
                println("🔐 AuthViewModel: username updated to $name")
            }
        }

        // Слушаме за Id
        viewModelScope.launch {
            tokenManager.userIdFlow.collect { id ->
                _userId.value = id
                println("🆔 AuthViewModel: userId updated to $id")
            }
        }
    }

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(username, password))
                tokenManager.saveToken(response.token)
                tokenManager.saveUsername(response.username)
                tokenManager.saveUserId(response.id)
                _isAuthenticated.value = true
                _username.value = response.username
                _userId.value = response.id
                onSuccess()
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("401") == true -> "Невалидно потребителско име или парола"
                    e.message?.contains("400") == true -> "Моля, въведете всички полета"
                    else -> e.message ?: "Грешка при свързване със сървъра"
                }
                _error.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        fullName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.apiService.register(
                    RegisterRequest(username, email, password, fullName)
                )
                tokenManager.saveToken(response.token)
                tokenManager.saveUsername(response.username)
                _isAuthenticated.value = true
                //
                _username.value = response.username
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Грешка при регистрация"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            _isAuthenticated.value = false
            _username.value = null
        }
    }
}