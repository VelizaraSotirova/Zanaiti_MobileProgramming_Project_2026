package bg.zanaiti.craftguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CraftViewModel : ViewModel() {

    private val _crafts = MutableStateFlow<List<Craft>>(emptyList())
    val crafts: StateFlow<List<Craft>> = _crafts

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadCrafts()
    }

    private fun loadCrafts() {
        viewModelScope.launch(Dispatchers.IO) { // Изпълнява се на фонова нишка
            _isLoading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.apiService.getAllCrafts()
                _crafts.value = response
            } catch (e: Exception) {
                _error.value = e.message ?: "Неизвестна грешка"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}