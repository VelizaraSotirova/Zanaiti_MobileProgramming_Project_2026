package bg.zanaiti.craftguide.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.ui.CraftViewModel
import bg.zanaiti.craftguide.ui.LanguageViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    showOnlyCraftId: Long? = null,
    langViewModel: LanguageViewModel,
    onCraftClick: (Craft) -> Unit,
    viewModel: CraftViewModel = viewModel()
) {
    val context = LocalContext.current
    val crafts by viewModel.crafts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLanguage by langViewModel.currentLanguage.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedCraft by remember { mutableStateOf<Craft?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var tUserMarker by remember { mutableStateOf("Вие сте тук") }

    LaunchedEffect(currentLanguage) {
        tUserMarker = langViewModel.translate("Вие сте тук")
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            getUserLocation(context) { location ->
                userLocation = GeoPoint(location.latitude, location.longitude)
            }
        }
    }

    val filteredCrafts = if (showOnlyCraftId != null) {
        crafts.filter { it.id == showOnlyCraftId }
    } else {
        crafts
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            !locationPermissionState.status.isGranted -> {
                PermissionRequestUI(langViewModel) {
                    locationPermissionState.launchPermissionRequest()
                }
            }
            else -> {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapViewInstance = this
                            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(9.0)
                            controller.setCenter(userLocation ?: GeoPoint(42.6975, 23.3241))

                            filteredCrafts.forEach { craft ->
                                val marker = Marker(this)
                                marker.position = GeoPoint(craft.latitude, craft.longitude)
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                                scope.launch {
                                    marker.title = langViewModel.translate(craft.translations["bg"]?.name ?: "Занаят")
                                }

                                marker.setOnMarkerClickListener { _, _ ->
                                    selectedCraft = craft
                                    true
                                }
                                overlays.add(marker)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        userLocation?.let { loc ->
                            view.overlays.removeAll { it is Marker && it.title == tUserMarker }
                            val myMarker = Marker(view)
                            myMarker.position = loc
                            myMarker.title = tUserMarker
                            val redIcon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                            redIcon?.setTint(android.graphics.Color.RED)
                            myMarker.icon = redIcon
                            view.overlays.add(myMarker)
                        }
                        view.invalidate()
                    }
                )

                FloatingActionButton(
                    onClick = {
                        userLocation?.let {
                            mapViewInstance?.controller?.animateTo(it)
                            mapViewInstance?.controller?.setZoom(15.0)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (selectedCraft != null) 240.dp else 32.dp, end = 16.dp)
                ) {
                    Text("🎯")
                }

                selectedCraft?.let { craft ->
                    CraftInfoCard(
                        craft = craft,
                        langViewModel = langViewModel,
                        onDetailsClick = { onCraftClick(craft) },
                        onRouteClick = {
                            if (userLocation != null && mapViewInstance != null) {
                                scope.launch {
                                    val road = getRoad(context, userLocation!!, GeoPoint(craft.latitude, craft.longitude))
                                    if (road != null) {
                                        currentPolyline?.let { mapViewInstance?.overlays?.remove(it) }
                                        currentPolyline = RoadManager.buildRoadOverlay(road).apply {
                                            outlinePaint.color = android.graphics.Color.BLUE
                                            outlinePaint.strokeWidth = 12f
                                        }
                                        mapViewInstance?.overlays?.add(currentPolyline)
                                        mapViewInstance?.invalidate()
                                        mapViewInstance?.controller?.animateTo(userLocation)
                                    }
                                }
                            }
                        },
                        onClose = {
                            selectedCraft = null
                            currentPolyline?.let {
                                mapViewInstance?.overlays?.remove(it)
                                mapViewInstance?.invalidate()
                            }
                            currentPolyline = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestUI(langViewModel: LanguageViewModel, onRetry: () -> Unit) {
    val currentLanguage by langViewModel.currentLanguage.collectAsState()
    var tMsg by remember { mutableStateOf("📍 Трябва ни локация, за да видите занаятите наблизо.") }
    var tBtn by remember { mutableStateOf("Дай достъп") }

    LaunchedEffect(currentLanguage) {
        tMsg = langViewModel.translate("📍 Трябва ни локация, за да видите занаятите наблизо.")
        tBtn = langViewModel.translate("Дай достъп")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(tMsg, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(tBtn) }
    }
}

@Composable
fun BoxScope.CraftInfoCard(
    craft: Craft,
    langViewModel: LanguageViewModel,
    onDetailsClick: () -> Unit,
    onRouteClick: () -> Unit,
    onClose: () -> Unit
) {
    val currentLanguage by langViewModel.currentLanguage.collectAsState()

    var tName by remember { mutableStateOf(craft.translations["bg"]?.name ?: "") }
    var tDesc by remember { mutableStateOf(craft.translations["bg"]?.description?.take(80) ?: "") }
    var tDetailsBtn by remember { mutableStateOf("Детайли") }
    var tRouteBtn by remember { mutableStateOf("Заведи ме там") }

    LaunchedEffect(currentLanguage, craft) {
        tName = langViewModel.translate(craft.translations["bg"]?.name ?: "Занаят")
        tDesc = langViewModel.translate(craft.translations["bg"]?.description?.take(80) ?: "") + "..."
        tDetailsBtn = langViewModel.translate("Детайли")
        tRouteBtn = langViewModel.translate("Заведи ме там")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = tName, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Затвори")
                }
            }

            Text(text = tDesc, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onDetailsClick, modifier = Modifier.weight(1f)) {
                    Text(tDetailsBtn)
                }
                Button(
                    onClick = onRouteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(tRouteBtn)
                }
            }
        }
    }
}

private fun getUserLocation(context: Context, onResult: (Location) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        location?.let { onResult(it) }
    }
}

private suspend fun getRoad(context: Context, start: GeoPoint, end: GeoPoint): org.osmdroid.bonuspack.routing.Road? {
    return withContext(Dispatchers.IO) {
        val roadManager = OSRMRoadManager(context, "ZanaitiApp")
        roadManager.getRoad(arrayListOf(start, end))
    }
}