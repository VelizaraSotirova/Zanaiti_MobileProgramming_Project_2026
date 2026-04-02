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
    onCraftClick: (Craft) -> Unit,
    viewModel: CraftViewModel = viewModel()
) {
    val context = LocalContext.current
    val crafts by viewModel.crafts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedCraft by remember { mutableStateOf<Craft?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val filteredCrafts = if (showOnlyCraftId != null) {
        crafts.filter { it.id == showOnlyCraftId }
    } else {
        crafts
    }

    // Вземане на локация при старт
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            getUserLocation(context) { location ->
                userLocation = GeoPoint(location.latitude, location.longitude)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            !locationPermissionState.status.isGranted -> {
                PermissionRequestUI { locationPermissionState.launchPermissionRequest() }
            }
            else -> {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            mapView = this
                            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            controller.setZoom(9.0)
                            controller.setCenter(userLocation ?: GeoPoint(42.6975, 23.3241))

                            // Маркери за занаятите
                            filteredCrafts.forEach { craft ->
                                val marker = Marker(this)
                                marker.position = GeoPoint(craft.latitude, craft.longitude)
                                marker.title = craft.translations["bg"]?.name ?: "Занаят"

                                marker.setOnMarkerClickListener { _, _ ->
                                    selectedCraft = craft
                                    true
                                }
                                overlays.add(marker)
                            }

                            // Маркер за потребителя
                            userLocation?.let { loc ->
                                val myMarker = Marker(this)
                                myMarker.position = loc
                                myMarker.title = "Вие сте тук"
                                val redIcon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)?.mutate()
                                redIcon?.setTint(android.graphics.Color.RED)
                                myMarker.icon = redIcon
                                myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                overlays.add(myMarker)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Бутон "Центрирай ме"
                FloatingActionButton(
                    onClick = {
                        userLocation?.let {
                            mapView?.controller?.animateTo(it)
                            mapView?.controller?.setZoom(15.0)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (selectedCraft != null) 220.dp else 32.dp, end = 16.dp)
                ) {
                    Text("🎯")
                }

                // Инфо карта и бутон "Заведи ме" (с бутон за затваряне)
                selectedCraft?.let { craft ->
                    CraftInfoCard(
                        craft = craft,
                        onDetailsClick = { onCraftClick(craft) },
                        onRouteClick = {
                            if (userLocation != null && mapView != null) {
                                scope.launch {
                                    val road = getRoad(context, userLocation!!, GeoPoint(craft.latitude, craft.longitude))
                                    if (road != null) {
                                        currentPolyline?.let { mapView?.overlays?.remove(it) }
                                        currentPolyline = RoadManager.buildRoadOverlay(road).apply {
                                            outlinePaint.color = android.graphics.Color.BLUE
                                            outlinePaint.strokeWidth = 12f
                                        }
                                        mapView?.overlays?.add(currentPolyline)
                                        mapView?.invalidate()
                                        mapView?.controller?.animateTo(userLocation)
                                    }
                                }
                            }
                        },
                        onClose = { selectedCraft = null }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRequestUI(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📍 Трябва ни локация, за да видите занаятите наблизо.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Дай достъп") }
    }
}

@Composable
fun BoxScope.CraftInfoCard(
    craft: Craft,
    onDetailsClick: () -> Unit,
    onRouteClick: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Ред със заглавие и бутон за затваряне
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = craft.translations["bg"]?.name ?: "Няма име",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Затвори")
                }
            }

            Text(
                text = craft.translations["bg"]?.description?.take(80) + "...",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onDetailsClick, modifier = Modifier.weight(1f)) {
                    Text("Детайли")
                }
                Button(
                    onClick = onRouteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Заведи ме там")
                }
            }
        }
    }
}

// Помощни функции за локация и пътища
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