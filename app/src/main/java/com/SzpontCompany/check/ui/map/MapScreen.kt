package com.SzpontCompany.check.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.data.map.Route
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.interaction.MutableInteractionSource

enum class MapTab { ROUTES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(MapTab.ROUTES) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(52.23, 21.01), 6f) // Startowy zoom
    }

    val pathPoints by viewModel.pathPoints.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val distanceKm by viewModel.distanceKm.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val routesHistory by viewModel.routesHistory.collectAsState()
    val scope = rememberCoroutineScope()
    val stepsCount by viewModel.stepsCount.collectAsState()

    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16f)
                            )
                        }
                    } else {
                        fusedLocationClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                            null
                        ).addOnSuccessListener { currentLocation ->
                            currentLocation?.let {
                                scope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }
    }

    LaunchedEffect(pathPoints) {
        if (isTracking && pathPoints.isNotEmpty()) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(pathPoints.last()))
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetPeekHeight = 160.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetDragHandle = { BottomSheetDefaults.DragHandle() },
        sheetContent = {
            MapSheetContent(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                isTracking = isTracking,
                distance = String.format(Locale.US, "%.2f", distanceKm),
                duration = viewModel.formatDuration(durationMs),
                calories = viewModel.calories.toString(),
                steps = stepsCount.toString(),
                onToggleTracking = {
                    if (locationPermissionGranted) {
                        viewModel.toggleTracking(context)
                    } else {
                        Toast.makeText(context, "Brak uprawnień do lokalizacji!", Toast.LENGTH_SHORT).show()
                    }
                },
                routesHistory = routesHistory,
                formatDuration = viewModel::formatDuration
            )
        },
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
                ) {
                    if (pathPoints.isNotEmpty()) {
                        Polyline(
                            points = pathPoints,
                            color = MaterialTheme.colorScheme.primary,
                            width = 12f
                        )
                    }
                }

                MapOverlays(
                    onSettingsClick = {},
                    onZoomIn = {
                        scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                    },
                    onZoomOut = {
                        scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                    },
                    onLocationClick = {
                        if (locationPermissionGranted) {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        // OPAKOWUJEMY W SCOPE.LAUNCH:
                                        scope.launch {
                                            cameraPositionState.animate(
                                                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f)
                                            )
                                        }
                                    }
                                }
                            } catch (e: SecurityException) {}
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun MapOverlays(
    onSettingsClick: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onLocationClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ZoomButton("+", onClick = onZoomIn)
            ZoomButton("−", onClick = onZoomOut)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onLocationClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocationOn, "GPS", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun MapSheetContent(
    selectedTab: MapTab,
    onTabSelected: (MapTab) -> Unit,
    isTracking: Boolean,
    distance: String,
    duration: String,
    calories: String,
    steps: String,
    onToggleTracking: () -> Unit,
    routesHistory: List<Route>,
    formatDuration: (Long) -> String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 450.dp)
            .padding(bottom = 16.dp)
    ) {

        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            StatCell(duration, "czas", true)
            Spacer(Modifier.weight(1f))
            StatCell(distance, "km")
            Spacer(Modifier.weight(1f))
            StatCell(steps, "kroków")
            Spacer(Modifier.weight(1f))
            StatCell(calories, "kcal")
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        if (isTracking) "Zapisywanie trasy..." else "Rozpoczęto śledzenie trasy!",
                        Toast.LENGTH_SHORT
                    ).show()
                    onToggleTracking()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color(0xFFE24B4A) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = if (isTracking) "Zakończ i Zapisz" else "Rozpocznij Trening",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Row(Modifier.padding(horizontal = 16.dp)) {
            MapTabItem("Moje Trasy", selectedTab == MapTab.ROUTES) { onTabSelected(MapTab.ROUTES) }
        }

        if (selectedTab == MapTab.ROUTES) {
            var selectedImageUrl by remember { mutableStateOf<String?>(null) }

            Spacer(modifier = Modifier.height(8.dp))
            if (routesHistory.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Brak zapisanych tras.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(routesHistory) { route ->
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        val dateStr = if (route.startTime > 0) dateFormat.format(Date(route.startTime)) else "Nieznana data"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                        ) {
                            Column {
                                if (route.mapImageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = route.mapImageUrl,
                                        contentDescription = "Mapa przebytej trasy",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { selectedImageUrl = route.mapImageUrl }
                                    )
                                }

                                Column(Modifier.padding(16.dp)) {
                                    Text(dateStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    Spacer(Modifier.height(8.dp))

                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Dystans", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${String.format(Locale.US, "%.2f", route.distanceKm)} km", fontSize=15.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Czas", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(formatDuration(route.durationMs), fontSize=15.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Kroki", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${route.steps}", fontSize=15.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // DODANO: Pełnoekranowy Dialog z powiększonym zdjęciem
            if (selectedImageUrl != null) {
                Dialog(
                    onDismissRequest = { selectedImageUrl = null },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false, // Pozwala na rozciągnięcie na 100% szerokości ekranu
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f)) // Przyciemnione, czarne tło
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null, // Brak efektu fali przy kliknięciu w puste tło
                                onClick = { selectedImageUrl = null } // Kliknięcie gdziekolwiek zamyka obrazek
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = selectedImageUrl,
                            contentDescription = "Powiększona mapa",
                            contentScale = ContentScale.Fit, // Zmienia na Fit, żeby zdjęcie zachowało proporcje
                            modifier = Modifier.fillMaxSize()
                        )

                        // Przycisk zamykania (X) w prawym górnym rogu
                        IconButton(
                            onClick = { selectedImageUrl = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding() // Omija notch/wycięcie na aparat
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zamknij",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MapChip(icon: @Composable () -> Unit, label: String) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon()
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ZoomButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surface).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatCell(value: String, label: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = if (highlight) 20.sp else 16.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MapTabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        if (selected) {
            Box(Modifier.height(2.dp).width(20.dp).background(MaterialTheme.colorScheme.primary))
        }
    }
}