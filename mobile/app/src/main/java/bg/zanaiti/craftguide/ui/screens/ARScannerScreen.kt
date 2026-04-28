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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import bg.zanaiti.craftguide.ui.LanguageViewModel
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
    langViewModel: LanguageViewModel,
    onObjectDetected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    var isDetected by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    // Динамични текстове
    var statusText by remember { mutableStateOf("") }
    var tTitle by remember { mutableStateOf("") }
    var tScanBtn by remember { mutableStateOf("") }
    var tBack by remember { mutableStateOf("") }
    var tPermissionMsg by remember { mutableStateOf("") }
    var tPermissionBtn by remember { mutableStateOf("") }

    // Използваме LaunchedEffect за първоначален превод и при смяна на езика
    LaunchedEffect(currentLanguage) {
        tTitle = langViewModel.translate("AR Скенер")
        tScanBtn = langViewModel.translate("🔍 Сканирай")
        tBack = langViewModel.translate("Назад")
        tPermissionMsg = langViewModel.translate("📷 Нужен е достъп до камерата")
        tPermissionBtn = langViewModel.translate("Дайте разрешение")
        statusText = langViewModel.translate("Натиснете бутона за сканиране")
    }

    var detectionCount by remember { mutableStateOf(0) }
    var showDetectionCard by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    if (!cameraPermissionState.status.isGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(tPermissionMsg)
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text(tPermissionBtn)
            }
            Button(onClick = onBack) { Text(tBack) }
        }
        return
    }

    val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .build()
    val objectDetector = ObjectDetection.getClient(options)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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

                        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                            if (!isAnalyzing && isScanning && !isDetected) {
                                isAnalyzing = true
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    objectDetector.process(inputImage)
                                        .addOnSuccessListener { objects ->
                                            if (objects.isNotEmpty()) {
                                                val largest = objects.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                                                largest?.let { obj ->
                                                    val width = obj.boundingBox.width()
                                                    val height = obj.boundingBox.height()
                                                    val aspectRatio = width.toFloat() / height.toFloat()
                                                    if ((width * height) > (imageProxy.width * imageProxy.height * 0.25)) {
                                                        val craftType = if (abs(aspectRatio - 1f) < 0.4f && width > 150) "Грънчарство" else null
                                                        if (craftType != null) {
                                                            detectionCount++
                                                            if (detectionCount >= 3) {
                                                                isDetected = true
                                                                isScanning = false

                                                                // Тук използваме coroutineScope, защото сме в Listener (не-suspend среда)
                                                                coroutineScope.launch {
                                                                    statusText = langViewModel.translate("✅ Разпознато грънчарство!")
                                                                    showDetectionCard = true
                                                                    delay(1500)
                                                                    onObjectDetected(craftType)
                                                                }
                                                            } else {
                                                                coroutineScope.launch {
                                                                    val recognitionMsg = langViewModel.translate("🔍 Разпознавам...")
                                                                    statusText = "$recognitionMsg ($detectionCount/3)"
                                                                }
                                                            }
                                                        } else {
                                                            coroutineScope.launch {
                                                                statusText = langViewModel.translate("📷 Приближете изделието")
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    statusText = langViewModel.translate("🔍 Търся изделие...")
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { isAnalyzing = false; imageProxy.close() }
                                } else { imageProxy.close(); isAnalyzing = false }
                            } else { imageProxy.close() }
                        }
                        cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                    }, ContextCompat.getMainExecutor(context))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f), shape = MaterialTheme.shapes.medium) {
                    Text(text = statusText, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isScanning = true
                        detectionCount = 0
                        coroutineScope.launch {
                            statusText = langViewModel.translate("Сканирам...")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                ) {
                    Text(tScanBtn)
                }
                Button(onClick = onBack) { Text(tBack) }
            }

            if (showDetectionCard) {
                Card(modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tTitle, style = MaterialTheme.typography.titleLarge)
                        Text(statusText)
                    }
                }
            }
        }
    }
}