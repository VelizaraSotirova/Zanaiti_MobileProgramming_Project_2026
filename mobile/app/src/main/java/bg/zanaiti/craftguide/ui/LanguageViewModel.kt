package bg.zanaiti.craftguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.zanaiti.craftguide.utils.TranslationManager
import com.google.mlkit.nl.translate.TranslateLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LanguageViewModel : ViewModel() {
    private val translationManager = TranslationManager()

    // Текущ избран език (по подразбиране български)
    private val _currentLanguage = MutableStateFlow(TranslateLanguage.BULGARIAN)
    val currentLanguage: StateFlow<String> = _currentLanguage

    // Индикатор дали в момента се тегли език
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    fun setLanguage(langCode: String) {
        viewModelScope.launch {
            if (langCode != TranslateLanguage.BULGARIAN) {
                _isDownloading.value = true
                // Тук просто инициираме сваляне, ако е нужно
                translationManager.translateText("test", targetLang = langCode)
                _isDownloading.value = false
            }
            _currentLanguage.value = langCode
        }
    }

    // Помощна функция, която ще викаме в екраните
    suspend fun translate(text: String): String {
        return translationManager.translateText(text, targetLang = _currentLanguage.value)
    }
}