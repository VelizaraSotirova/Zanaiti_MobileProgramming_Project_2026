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
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    val viewModel: QuizViewModel = viewModel(
        factory = QuizViewModelFactory(isLoggedIn, userId, craft.id, "bg")
    )

    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val score by viewModel.score.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val quizCompleted by viewModel.quizCompleted.collectAsState()
    val selectedOption by viewModel.selectedOption.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()

    // Държатели за преведените текстове (UI State)
    var tQuestionText by remember { mutableStateOf("") }
    var tOptions by remember { mutableStateOf(listOf("", "", "", "")) }
    var tConfirmBtn by remember { mutableStateOf("") }
    var tFeedbackMsg by remember { mutableStateOf("") }
    var tQuestionInfo by remember { mutableStateOf("") }
    var tScoreLabel by remember { mutableStateOf("") }

    // Основен Ефект: Превежда въпроса и опциите при смяна на индекс или език
    LaunchedEffect(questions, currentIndex, currentLanguage) {
        if (questions.isNotEmpty()) {
            val currentQuestion = questions[currentIndex]
            val base = currentQuestion.translations["bg"]!!

            // Превеждаме заглавната информация
            tQuestionInfo = langViewModel.translate("Въпрос ${currentIndex + 1} от ${questions.size}")
            tScoreLabel = langViewModel.translate("⭐ Точки: $score")
            tConfirmBtn = langViewModel.translate("Потвърди")

            // Превеждаме самия въпрос и неговите опции
            tQuestionText = langViewModel.translate(base.questionText)
            tOptions = listOf(
                langViewModel.translate(base.optionA),
                langViewModel.translate(base.optionB),
                langViewModel.translate(base.optionC),
                langViewModel.translate(base.optionD)
            )
        }
    }

    // Ефект за превод на обратната връзка (Браво/Грешка)
    LaunchedEffect(feedback, currentLanguage) {
        feedback?.let {
            tFeedbackMsg = langViewModel.translate(it)
        }
    }

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
                    langViewModel = langViewModel, // Подаваме го и тук за финалния екран
                    onBack = {
                        viewModel.reset()
                        onBack()
                    }
                )
            }
            questions.isNotEmpty() -> {
                // Прогрес и точки
                Text(text = tQuestionInfo, style = MaterialTheme.typography.titleMedium)
                Text(text = tScoreLabel, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(8.dp))

                // Карта с въпроса
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = tQuestionText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Опции за отговор
                tOptions.forEachIndexed { index, option ->
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
                        Text(tConfirmBtn)
                    }
                }

                // Съобщение за обратна връзка
                feedback?.let {
                    Text(
                        text = tFeedbackMsg,
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
    langViewModel: LanguageViewModel,
    onBack: () -> Unit
) {
    val currentLanguage by langViewModel.currentLanguage.collectAsState()
    var tFinished by remember { mutableStateOf("") }
    var tCorrect by remember { mutableStateOf("") }
    var tPoints by remember { mutableStateOf("") }
    var tBackBtn by remember { mutableStateOf("") }

    LaunchedEffect(currentLanguage) {
        tFinished = langViewModel.translate("🏆 Тест завършен! 🏆")
        tCorrect = langViewModel.translate("Верни отговори: $correctAnswers / $totalQuestions")
        tPoints = langViewModel.translate("Спечелени точки: $score")
        tBackBtn = langViewModel.translate("Назад към детайлите")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = tFinished, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = tCorrect, style = MaterialTheme.typography.titleLarge)
        Text(text = tPoints, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        ) {
            Text(tBackBtn)
        }
    }
}