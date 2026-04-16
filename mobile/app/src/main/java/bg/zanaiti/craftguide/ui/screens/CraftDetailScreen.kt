package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.ui.LanguageViewModel
import coil.compose.AsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

@Composable
fun CraftDetailScreen(
    craft: Craft,
    langViewModel: LanguageViewModel,
    onBack: () -> Unit,
    onShowOnMap: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val currentLanguage by langViewModel.currentLanguage.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Базови текстове от обекта
    val baseName = craft.translations["bg"]?.name ?: ""
    val baseDesc = craft.translations["bg"]?.description ?: ""
    val baseHistory = craft.translations["bg"]?.historicalFacts ?: ""
    val baseProcess = craft.translations["bg"]?.makingProcess ?: ""

    // Състояния за преведените текстове
    var tName by remember { mutableStateOf(baseName) }
    var tDesc by remember { mutableStateOf(baseDesc) }
    var tHistoryLabel by remember { mutableStateOf("📜 Исторически факти:") }
    var tHistoryText by remember { mutableStateOf(baseHistory) }
    var tProcessLabel by remember { mutableStateOf("⚒️ Процес на изработка:") }
    var tProcessText by remember { mutableStateOf(baseProcess) }
    var tVideoLabel by remember { mutableStateOf("🎬 Виж как се прави:") }
    var tMapBtn by remember { mutableStateOf("🗺️ Виж на картата") }
    var tQuizBtn by remember { mutableStateOf("❓ Започни Quiz") }

    LaunchedEffect(currentLanguage) {
        tName = langViewModel.translate(baseName)
        tDesc = langViewModel.translate(baseDesc)
        tHistoryText = langViewModel.translate(baseHistory)
        tProcessText = langViewModel.translate(baseProcess)

        tHistoryLabel = "📜 " + langViewModel.translate("Исторически факти:")
        tProcessLabel = "⚒️ " + langViewModel.translate("Процес на изработка:")
        tVideoLabel = "🎬 " + langViewModel.translate("Виж как се прави:")
        tMapBtn = "🗺️ " + langViewModel.translate("Виж на картата")
        tQuizBtn = "❓ " + langViewModel.translate("Започни Quiz")
    }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(craft.animationUrl) {
        if (!craft.animationUrl.isNullOrBlank()) {
            val player = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(craft.animationUrl))
                prepare()
                playWhenReady = true
            }
            exoPlayer = player
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                exoPlayer?.stop()
                exoPlayer?.release()
                exoPlayer = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.release()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = craft.imageUrl,
            contentDescription = tName,
            modifier = Modifier.fillMaxWidth().height(220.dp),
            contentScale = ContentScale.Crop
        )

        Text(text = tDesc, style = MaterialTheme.typography.bodyLarge)

        Text(text = tHistoryLabel, style = MaterialTheme.typography.titleMedium)
        Text(text = tHistoryText, style = MaterialTheme.typography.bodyMedium)

        Text(text = tProcessLabel, style = MaterialTheme.typography.titleMedium)
        Text(text = tProcessText, style = MaterialTheme.typography.bodyMedium)

        if (exoPlayer != null) {
            Text(text = tVideoLabel, style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                AndroidView(factory = { ctx ->
                    PlayerView(ctx).apply { player = exoPlayer }
                }, modifier = Modifier.fillMaxSize())
            }
        }

        Button(onClick = onShowOnMap, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text(tMapBtn)
        }

        Button(onClick = onStartQuiz, modifier = Modifier.fillMaxWidth()) {
            Text(tQuizBtn)
        }
    }
}