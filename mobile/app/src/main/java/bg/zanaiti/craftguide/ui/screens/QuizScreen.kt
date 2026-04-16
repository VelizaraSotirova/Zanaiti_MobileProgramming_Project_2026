package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.ui.QuizViewModel
import bg.zanaiti.craftguide.ui.QuizViewModelFactory
import androidx.compose.runtime.collectAsState
import bg.zanaiti.craftguide.ui.LanguageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    craft: Craft,
    langViewModel: LanguageViewModel,
    onBack: () -> Unit,
    isLoggedIn: Boolean = false,
    userId: Long? = null
) {
    val viewModel: QuizViewModel = viewModel(
        factory = QuizViewModelFactory(isLoggedIn, userId, craft.id)
    )

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val quizCompleted by viewModel.quizCompleted.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            quizCompleted -> {
                QuizResultScreen(
                    score = score,
                    totalQuestions = questions.size,
                    correctAnswers = correctCount,
                    onBack = {
                        viewModel.reset()
                        onBack()
                    }
                )
            }
            questions.isNotEmpty() -> {
                val question = questions[currentIndex]
                val translation = question.translations["bg"]!!

                // Прогрес и точки
                Text(
                    text = "Въпрос ${currentIndex + 1} от ${questions.size}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "⭐ Точки: $score",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Въпрос
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = translation.questionText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Опции
                val options = listOf(
                    translation.optionA,
                    translation.optionB,
                    translation.optionC,
                    translation.optionD
                )

                options.forEachIndexed { index, option ->
                    Button(
                        onClick = { viewModel.selectOption(index) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedOption == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(option)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Бутон за потвърждение
                if (selectedOption != null && feedback == null) {
                    Button(
                        onClick = { viewModel.submitAnswer() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Потвърди")
                    }
                }

                // Съобщение за обратна връзка
                feedback?.let {
                    Text(
                        text = it,
                        color = if (it.contains("Bravo") || it.contains("Правилно"))
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(
    score: Int,
    totalQuestions: Int,
    correctAnswers: Int,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏆 Тест завършен! 🏆",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Верни отговори: $correctAnswers / $totalQuestions",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Спечелени точки: $score",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад към детайлите")
        }
    }
}