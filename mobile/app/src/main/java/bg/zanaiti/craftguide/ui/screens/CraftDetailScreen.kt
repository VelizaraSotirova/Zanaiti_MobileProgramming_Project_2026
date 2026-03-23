package bg.zanaiti.craftguide.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import bg.zanaiti.craftguide.models.Craft
import coil.compose.AsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftDetailScreen(
    craft: Craft,
    onBack: () -> Unit,
    onShowOnMap: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val translation = craft.translations["bg"]!!
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isLeaving by remember { mutableStateOf(false) }

    // Зареждане на видеото
    LaunchedEffect(Unit) {
        if (!craft.animationUrl.isNullOrBlank() && exoPlayer == null) {
            val player = ExoPlayer.Builder(context).build()
            val mediaItem = MediaItem.fromUri(craft.animationUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            exoPlayer = player
        }
    }

    // Освобождаване при напускане
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(translation.name) },
                navigationIcon = {
                    IconButton(onClick = {
                        // Спираме видеото веднага
                        exoPlayer?.stop()
                        exoPlayer?.release()
                        exoPlayer = null
                        isLeaving = true
                        onBack()
                    }) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = craft.imageUrl,
                contentDescription = translation.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Text(translation.description, style = MaterialTheme.typography.bodyLarge)

            Text("📜 Исторически факти:", style = MaterialTheme.typography.titleMedium)
            Text(translation.historicalFacts, style = MaterialTheme.typography.bodyMedium)

            Text("⚒️ Процес на изработка:", style = MaterialTheme.typography.titleMedium)
            Text(translation.makingProcess, style = MaterialTheme.typography.bodyMedium)

            // Видео – само ако има player и не сме в процес на напускане
            if (exoPlayer != null && !craft.animationUrl.isNullOrBlank() && !isLeaving) {
                Text("🎬 Виж как се прави:", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onShowOnMap,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🗺️ Виж на картата")
            }

            Button(
                onClick = onStartQuiz,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("❓ Започни Quiz")
            }
        }
    }
}