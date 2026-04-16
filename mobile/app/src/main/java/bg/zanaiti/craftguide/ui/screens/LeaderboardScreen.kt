package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import bg.zanaiti.craftguide.ui.LanguageViewModel
import bg.zanaiti.craftguide.ui.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    langViewModel: LanguageViewModel,
    onBack: () -> Unit
) {
    val viewModel: LeaderboardViewModel = viewModel()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    var tRetry by remember { mutableStateOf("") }
    var tEmpty by remember { mutableStateOf("") }
    var tPointsSuffix by remember { mutableStateOf("") }

    LaunchedEffect(currentLanguage) {
        tRetry = langViewModel.translate("Опитай отново")
        tEmpty = langViewModel.translate("Няма точки в класацията все още")
        tPointsSuffix = if (currentLanguage == "bg") "т." else "pts"
    }

    // Scaffold без topBar, защото ползваш общия от MainContainer
    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("❌ $error")
                        Button(onClick = { viewModel.refresh() }) { Text(tRetry) }
                    }
                }
                leaderboard.isEmpty() -> Text(tEmpty, modifier = Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(leaderboard) { index, entry ->
                            LeaderboardItem(
                                rank = index + 1,
                                username = entry.username,
                                points = entry.totalPoints,
                                pointsSuffix = tPointsSuffix
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, username: String, points: Int, pointsSuffix: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            // Това подрежда елементите в двата края
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Лява част: Ранг + Име
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f) // Заема свободното място, но бута точките вдясно
            ) {
                Text(
                    text = when(rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "$rank."
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Дясна част: Точки
            Text(
                text = "$points $pointsSuffix",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}