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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import bg.zanaiti.craftguide.models.Craft
import coil.compose.AsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView

@Composable
fun CraftDetailScreen(
    craft: Craft,
    onBack: () -> Unit,
    onShowOnMap: () -> Unit,
    onStartQuiz: () -> Unit
) {
    val translation = craft.translations["bg"]!!
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Използваме MutableState за плейъра
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // 1. Инициализация на ExoPlayer
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

    // 2. LIFECYCLE OBSERVER - оправя забавянето на видеото
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Когато потребителят натисне "Назад", Lifecycle преминава през ON_PAUSE
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                exoPlayer?.stop()
                exoPlayer?.release()
                exoPlayer = null
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.stop()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    // Основно съдържание (без Scaffold, защото той е в MainScreen)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Изображение на занаята
        AsyncImage(
            model = craft.imageUrl,
            contentDescription = translation.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Crop
        )

        // Описание
        Text(
            text = translation.description,
            style = MaterialTheme.typography.bodyLarge
        )

        // Исторически факти
        Text(text = "📜 Исторически факти:", style = MaterialTheme.typography.titleMedium)
        Text(text = translation.historicalFacts, style = MaterialTheme.typography.bodyMedium)

        // Процес на изработка
        Text(text = "⚒️ Процес на изработка:", style = MaterialTheme.typography.titleMedium)
        Text(text = translation.makingProcess, style = MaterialTheme.typography.bodyMedium)

        // Видео секция (само ако има зареден плейър)
        if (exoPlayer != null && !craft.animationUrl.isNullOrBlank()) {
            Text(text = "🎬 Виж как се прави:", style = MaterialTheme.typography.titleMedium)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            // Правим фона прозрачен, за да няма "черни проблясъци" при затваряне
                            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
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

        // Бутони за действие
        Button(
            onClick = onShowOnMap,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("🗺️ Виж на картата")
        }

        Button(
            onClick = onStartQuiz,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("❓ Започни Quiz")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}