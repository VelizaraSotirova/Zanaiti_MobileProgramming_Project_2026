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
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalGetImage::class)
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
    var statusText by remember { mutableStateOf("Насочете камерата към занаят...") }
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

    fun detectCraftByShape(width: Int, height: Int, aspectRatio: Float): String {
        return when {
            abs(aspectRatio - 1f) < 0.3f && width > 100 && height > 100 -> "Грънчарство"
            aspectRatio > 1.5f && width > 200 -> "Тъкане"
            width < 150 && height < 150 -> "Дърворезба"
            else -> "Занаят"
        }
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
                            if (!isAnalyzing && !isDetected) {
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

                                                    if (objectArea > screenArea * 0.15) {
                                                        val craftType = detectCraftByShape(width, height, aspectRatio)
                                                        detectionCount++

                                                        if (detectionCount >= 3) {
                                                            if (lastDetectedCraft == craftType) {
                                                                isDetected = true
                                                                statusText = "✅ Разпознат: $craftType!"
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
                                                        statusText = "📷 Приближете занаята"
                                                    }
                                                }
                                            } else {
                                                detectionCount = 0
                                                statusText = "🔍 Търся занаят..."
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
                Button(onClick = onBack) {
                    Text("Назад")
                }
            }

            // Временна карта при разпознаване
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
                        Text("Това е свързано с ${lastDetectedCraft ?: "занаят"}.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("⏳ Пренасочване след момент...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
//package bg.zanaiti.craftguide.ui.screens
//
//import android.Manifest
//import androidx.camera.core.*
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalLifecycleOwner
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import com.google.accompanist.permissions.*
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.objects.ObjectDetection
//import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
//import kotlin.math.abs
//import java.util.concurrent.Executors
//
//@androidx.annotation.OptIn(ExperimentalGetImage::class)
//@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalGetImage::class)
//@Composable
//fun ARScannerScreen(
//    onObjectDetected: (String) -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    var isDetected by remember { mutableStateOf(false) }
//    var isAnalyzing by remember { mutableStateOf(false) }
//    var statusText by remember { mutableStateOf("Насочете камерата към занаят...") }
//    var detectionCount by remember { mutableStateOf(0) }
//    var lastDetectedCraft by remember { mutableStateOf<String?>(null) }
//
//    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
//
//    if (!cameraPermissionState.status.isGranted) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text("📷 Нужен е достъп до камерата")
//            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
//                Text("Дайте разрешение")
//            }
//            Button(onClick = onBack) { Text("Назад") }
//        }
//        return
//    }
//
//    val options = ObjectDetectorOptions.Builder()
//        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
//        .build()
//    val objectDetector = ObjectDetection.getClient(options)
//
//    // Функция за определяне на занаят по форма на обекта
//    fun detectCraftByShape(width: Int, height: Int, aspectRatio: Float): String {
//        return when {
//            // Кръгъл/овален обект (гърне, ваза) – ширина и височина близки
//            abs(aspectRatio - 1f) < 0.3f && width > 100 && height > 100 -> "Грънчарство"
//            // Плосък и широк обект (текстил, килим)
//            aspectRatio > 1.5f && width > 200 -> "Тъкане"
//            // Малък, детайлен обект (резба)
//            width < 150 && height < 150 -> "Дърворезба"
//            // По подразбиране
//            else -> "Занаят"
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("AR Скенер") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) { Text("←") }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            AndroidView(
//                factory = { ctx ->
//                    val previewView = PreviewView(ctx)
//                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
//
//                    cameraProviderFuture.addListener({
//                        val cameraProvider = cameraProviderFuture.get()
//                        val preview = Preview.Builder().build()
//                        preview.setSurfaceProvider(previewView.surfaceProvider)
//
//                        val imageAnalysis = ImageAnalysis.Builder()
//                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                            .build()
//
//                        imageAnalysis.setAnalyzer(
//                            Executors.newSingleThreadExecutor()
//                        ) { imageProxy ->
//                            if (!isAnalyzing && !isDetected) {
//                                isAnalyzing = true
//                                val mediaImage = imageProxy.image
//                                if (mediaImage != null) {
//                                    val inputImage = InputImage.fromMediaImage(
//                                        mediaImage,
//                                        imageProxy.imageInfo.rotationDegrees
//                                    )
//                                    objectDetector.process(inputImage)
//                                        .addOnSuccessListener { objects ->
//                                            if (objects.isNotEmpty()) {
//                                                val largest = objects.maxByOrNull {
//                                                    it.boundingBox.width() * it.boundingBox.height()
//                                                }
//                                                largest?.let { obj ->
//                                                    val width = obj.boundingBox.width()
//                                                    val height = obj.boundingBox.height()
//                                                    val screenArea = imageProxy.width * imageProxy.height
//                                                    val objectArea = width * height
//                                                    val aspectRatio = width.toFloat() / height.toFloat()
//
//                                                    if (objectArea > screenArea * 0.15) {
//                                                        val craftType = detectCraftByShape(width, height, aspectRatio)
//                                                        detectionCount++
//
//                                                        if (detectionCount >= 3) {
//                                                            if (lastDetectedCraft == craftType) {
//                                                                isDetected = true
//                                                                statusText = "✅ Разпознат: $craftType!"
//                                                                onObjectDetected(craftType)
//                                                            }
//                                                        } else {
//                                                            lastDetectedCraft = craftType
//                                                            statusText = "🔍 Разпознавам... ($detectionCount/3)"
//                                                        }
//                                                    } else {
//                                                        detectionCount = 0
//                                                        statusText = "📷 Приближете занаята"
//                                                    }
//                                                }
//                                            } else {
//                                                detectionCount = 0
//                                                statusText = "🔍 Търся занаят..."
//                                            }
//                                        }
//                                        .addOnCompleteListener {
//                                            isAnalyzing = false
//                                            imageProxy.close()
//                                        }
//                                } else {
//                                    imageProxy.close()
//                                    isAnalyzing = false
//                                }
//                            } else {
//                                imageProxy.close()
//                            }
//                        }
//
//                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//                        cameraProvider.bindToLifecycle(
//                            lifecycleOwner,
//                            cameraSelector,
//                            preview,
//                            imageAnalysis
//                        )
//                    }, ContextCompat.getMainExecutor(context))
//
//                    previewView
//                },
//                modifier = Modifier.fillMaxSize()
//            )
//
//            // Долен панел
//            Column(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 40.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Surface(
//                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
//                    shape = MaterialTheme.shapes.medium
//                ) {
//                    Text(
//                        text = statusText,
//                        modifier = Modifier.padding(16.dp),
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//                Button(onClick = onBack) {
//                    Text("Назад")
//                }
//            }
//
//            if (isDetected) {
//                Card(
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    elevation = CardDefaults.cardElevation(8.dp)
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text("🎉 Разпознат занаят! 🎉", style = MaterialTheme.typography.titleLarge)
//                        Text("Това е свързано с $lastDetectedCraft.")
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Button(onClick = { onObjectDetected(lastDetectedCraft ?: "Занаят") }) {
//                            Text("Виж детайли")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}