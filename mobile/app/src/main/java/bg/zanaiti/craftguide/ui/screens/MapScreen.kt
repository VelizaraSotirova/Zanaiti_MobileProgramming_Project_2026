package bg.zanaiti.craftguide.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import bg.zanaiti.craftguide.models.Craft
import bg.zanaiti.craftguide.ui.CraftViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    showOnlyCraftId: Long? = null,
    onCraftClick: (Craft) -> Unit,
    viewModel: CraftViewModel = viewModel()
) {
    val context = LocalContext.current
    val crafts by viewModel.crafts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedCraft by remember { mutableStateOf<Craft?>(null) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // ✅ Заявка за разрешение за локация
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Филтриране на занаятите
    val filteredCrafts = if (showOnlyCraftId != null) {
        crafts.filter { it.id == showOnlyCraftId }
    } else {
        crafts
    }

    // Вземане на локация, ако разрешението е дадено
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            getUserLocation(context) { location ->
                userLocation = GeoPoint(location.latitude, location.longitude)
            }
            delay(2000)
            if (userLocation == null) {
                userLocation = GeoPoint(42.6975, 23.3241) // София като fallback
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Карта на занаятите") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                !locationPermissionState.status.isGranted -> {
                    // 🟡 Екран за искане на разрешение
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "📍 За да видите занаяти в близост до вас, ни трябва достъп до локацията ви.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                locationPermissionState.launchPermissionRequest()
                            }
                        ) {
                            Text("Дайте достъп до локацията")
                        }
                    }
                }
                else -> {
                    // 🟢 Картата
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                Configuration.getInstance().load(
                                    ctx,
                                    ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
                                )
                                setTileSource(TileSourceFactory.MAPNIK)

                                val center = userLocation ?: GeoPoint(42.7, 25.5)
                                val zoom = if (userLocation != null) 12.0 else 7.0
                                controller.setZoom(zoom)
                                controller.setCenter(center)

                                filteredCrafts.forEach { craft ->
                                    val marker = Marker(this)
                                    marker.position = GeoPoint(craft.latitude, craft.longitude)
                                    marker.title = craft.translations["bg"]?.name ?: "Няма име"
                                    marker.setOnMarkerClickListener { _, _ ->
                                        selectedCraft = craft
                                        true
                                    }
                                    overlays.add(marker)
                                }

                                userLocation?.let { loc ->
                                    val myMarker = Marker(this)
                                    myMarker.position = loc
                                    myMarker.title = "Вие сте тук"
                                    overlays.add(myMarker)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Долен панел при избор на маркер
                    selectedCraft?.let { craft ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .clickable { onCraftClick(craft) },
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = craft.translations["bg"]?.name ?: "Няма име",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = craft.translations["bg"]?.description?.take(100) ?: "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "📍 Натисни за детайли",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getUserLocation(context: Context, onResult: (Location) -> Unit) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        location?.let { onResult(it) }
    }
}