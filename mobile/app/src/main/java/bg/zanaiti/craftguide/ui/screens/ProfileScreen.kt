package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg.zanaiti.craftguide.ui.LanguageViewModel
import bg.zanaiti.craftguide.ui.ProfileViewModel
import bg.zanaiti.craftguide.ui.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: Long,
    langViewModel: LanguageViewModel,
    onBack: () -> Unit
) {
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    var tProfileTitle by remember { mutableStateOf("") }
    var tNoUserMsg by remember { mutableStateOf("") }
    var tStatsTitle by remember { mutableStateOf("") }
    var tHistoryTitle by remember { mutableStateOf("") }
    var tPointsLabel by remember { mutableStateOf("") }
    var tVisitedLabel by remember { mutableStateOf("") }
    var tScoreLabel by remember { mutableStateOf("") }
    var tErrorMsg by remember { mutableStateOf("") }
    var tRetryBtn by remember { mutableStateOf("") }

    LaunchedEffect(currentLanguage) {
        tProfileTitle = langViewModel.translate("Профил")
        tNoUserMsg = langViewModel.translate("Моля, влезте в профила си, за да видите статистиката.")
        tStatsTitle = langViewModel.translate("📊 Статистика")
        tHistoryTitle = langViewModel.translate("📜 История на точките")
        tPointsLabel = langViewModel.translate("⭐ Точки")
        tVisitedLabel = langViewModel.translate("🏺 Посетени")
        tScoreLabel = langViewModel.translate("📈 Ср. Резултат")
        tErrorMsg = langViewModel.translate("❌ Грешка при зареждане")
        tRetryBtn = langViewModel.translate("Опитай отново")
    }

    if (userId <= 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(tNoUserMsg)
        }
        return
    }

    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(userId))
    val isLoading by viewModel.isLoading.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val pointsHistory by viewModel.pointsHistory.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(tProfileTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(tErrorMsg)
                    Button(onClick = { viewModel.refresh() }) { Text(tRetryBtn) }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(profile?.fullName ?: "", style = MaterialTheme.typography.headlineSmall)
                                Text("@${profile?.username ?: ""}", color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(tStatsTitle, style = MaterialTheme.typography.titleLarge)
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    StatItem(tPointsLabel, "${stats?.totalPoints ?: 0}")
                                    StatItem(tVisitedLabel, "${stats?.craftsVisited ?: 0}")
                                    StatItem(tScoreLabel, "${String.format("%.1f", stats?.averageScore ?: 0.0)}%")
                                }
                            }
                        }
                    }

                    item { Text(tHistoryTitle, style = MaterialTheme.typography.titleLarge) }

                    items(pointsHistory.take(15)) { item ->
                        var translatedSource by remember { mutableStateOf(item.source) }
                        var translatedDescription by remember { mutableStateOf(item.description) }

                        LaunchedEffect(currentLanguage) {
                            translatedSource = when (item.source) {
                                "QUIZ" -> langViewModel.translate("❓ Тест")
                                "VISIT" -> langViewModel.translate("📍 Посещение")
                                "ADMIN" -> langViewModel.translate("👑 Админ бонус")
                                else -> item.source
                            }
                            // Превеждаме и описанието ("Успешен тест...")
                            translatedDescription = langViewModel.translate(item.description)
                        }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(translatedSource, style = MaterialTheme.typography.titleSmall)
                                    Text(translatedDescription, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("+${item.points}", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}