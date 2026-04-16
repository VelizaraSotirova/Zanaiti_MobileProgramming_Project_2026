package bg.zanaiti.craftguide.utils

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import kotlinx.coroutines.tasks.await

class TranslationManager {

    private val modelManager = RemoteModelManager.getInstance()

    // Функция за превод на единичен стринг
    suspend fun translateText(
        text: String,
        sourceLang: String = TranslateLanguage.BULGARIAN,
        targetLang: String
    ): String {
        if (sourceLang == targetLang || text.isBlank()) return text

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

        val translator = Translation.getClient(options)

        return try {
            // Уверяваме се, че моделът е свален преди превод
            val conditions = DownloadConditions.Builder()
                .requireWifi() // Може да се промени, ако искаш и през мобилни данни
                .build()

            translator.downloadModelIfNeeded(conditions).await()
            val result = translator.translate(text).await()
            result
        } catch (e: Exception) {
            text // Връщаме оригинала при грешка
        } finally {
            translator.close()
        }
    }

    // Функция за проверка дали моделът е наличен
    suspend fun isModelDownloaded(langCode: String): Boolean {
        val model = TranslateRemoteModel.Builder(langCode).build()
        return modelManager.isModelDownloaded(model).await()
    }
}