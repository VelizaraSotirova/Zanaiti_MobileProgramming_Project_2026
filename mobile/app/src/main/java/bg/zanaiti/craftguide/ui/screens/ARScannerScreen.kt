package bg.zanaiti.craftguide.ui.screens

import android.Manifest
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ARScannerScreen(
    onObjectDetected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var isDetected by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Натиснете бутона за сканиране") }
    var detectionCount by remember { mutableStateOf(0) }
    var lastDetectedCraft by remember { mutableStateOf<String?>(null) }
    var showDetectionCard by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (!cameraPermissionState.status.isGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📷 Нужен е достъп до камерата")
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Дайте разрешение")
            }
            Button(onClick = onBack) { Text("Назад") }
        }
        return
    }

    val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .build()
    val objectDetector = ObjectDetection.getClient(options)

    fun detectCraftByShape(width: Int, height: Int, aspectRatio: Float): String? {
        val isRound = abs(aspectRatio - 1f) < 0.4f
        val isLarge = width > 150 && height > 150
        return if (isRound && isLarge) "Грънчарство" else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AR Скенер") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(
                            Executors.newSingleThreadExecutor()
                        ) { imageProxy ->
                            if (!isAnalyzing && isScanning && !isDetected) {
                                isAnalyzing = true
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    objectDetector.process(inputImage)
                                        .addOnSuccessListener { objects ->
                                            if (objects.isNotEmpty()) {
                                                val largest = objects.maxByOrNull {
                                                    it.boundingBox.width() * it.boundingBox.height()
                                                }
                                                largest?.let { obj ->
                                                    val width = obj.boundingBox.width()
                                                    val height = obj.boundingBox.height()
                                                    val screenArea = imageProxy.width * imageProxy.height
                                                    val objectArea = width * height
                                                    val aspectRatio = width.toFloat() / height.toFloat()

                                                    if (objectArea > screenArea * 0.25) {
                                                        val craftType = detectCraftByShape(width, height, aspectRatio)
                                                        if (craftType != null) {
                                                            detectionCount++
                                                            if (detectionCount >= 3) {
                                                                if (lastDetectedCraft == craftType) {
                                                                    isDetected = true
                                                                    isScanning = false
                                                                    statusText = "✅ Разпознато гювече!"
                                                                    lastDetectedCraft = craftType

                                                                    coroutineScope.launch {
                                                                        showDetectionCard = true
                                                                        delay(1500)
                                                                        showDetectionCard = false
                                                                        onObjectDetected(craftType)
                                                                    }
                                                                }
                                                            } else {
                                                                lastDetectedCraft = craftType
                                                                statusText = "🔍 Разпознавам... ($detectionCount/3)"
                                                            }
                                                        } else {
                                                            detectionCount = 0
                                                            statusText = "📷 Приближете гювечето"
                                                        }
                                                    } else {
                                                        detectionCount = 0
                                                        statusText = "📷 Приближете гювечето"
                                                    }
                                                }
                                            } else {
                                                detectionCount = 0
                                                statusText = "🔍 Търся гювече..."
                                            }
                                        }
                                        .addOnCompleteListener {
                                            isAnalyzing = false
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                    isAnalyzing = false
                                }
                            } else {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    }, ContextCompat.getMainExecutor(context))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Долен панел
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isScanning = true
                        detectionCount = 0
                        statusText = "Сканирам... Насочете към гювече"
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                ) {
                    Text("🔍 Сканирай")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Назад")
                }
            }

            if (showDetectionCard) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 Разпознат занаят! 🎉", style = MaterialTheme.typography.titleLarge)
                        Text("Това е свързано с грънчарството.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⏳ Пренасочване след момент...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}