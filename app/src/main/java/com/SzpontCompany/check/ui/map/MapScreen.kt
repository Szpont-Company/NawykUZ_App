package com.SzpontCompany.check.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.SzpontCompany.check.R
import com.SzpontCompany.check.data.map.FriendLocation
import com.SzpontCompany.check.data.map.Route
import com.SzpontCompany.check.ui.theme.getColorByName
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.SzpontCompany.check.ui.map.createAvatarMarker

enum class MapTab { ROUTES, FRIENDS }

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(MapTab.ROUTES) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(52.23, 21.01), 6f)
    }

    val pathPoints by viewModel.pathPoints.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val distanceKm by viewModel.distanceKm.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val routesHistory by viewModel.routesHistory.collectAsState()
    val friendsLocations by viewModel.friendsLocations.collectAsState() // Pobieranie danych o znajomych
    val stepsCount by viewModel.stepsCount.collectAsState()
    val scope = rememberCoroutineScope()

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

                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid != null) {
                            FirebaseFirestore.getInstance().collection("users").document(uid)
                                .update(
                                    mapOf(
                                        "latitude" to location.latitude,
                                        "longitude" to location.longitude,
                                        "lastSeenMillis" to System.currentTimeMillis()
                                    )
                                )
                        }

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
                        Toast.makeText(context,
                            context.getString(R.string.error_location_permissions), Toast.LENGTH_SHORT).show()
                    }
                },
                routesHistory = routesHistory,
                friendsLocations = friendsLocations,
                formatDuration = viewModel::formatDuration,
                onFriendClick = { friend ->
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(friend.latitude, friend.longitude), 14f)
                        )
                        scaffoldState.bottomSheetState.partialExpand()
                    }
                },
                getTimeAgoString = viewModel::getTimeAgoString
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

                    if (selectedTab == MapTab.FRIENDS) {
                        friendsLocations.forEach { friend ->
                            val avatarIcon = remember(friend.id, friend.name, friend.emoji, friend.bgColorName) {
                                createAvatarMarker(
                                    context = context,
                                    name = friend.name,
                                    emoji = friend.emoji,
                                    bgColorName = friend.bgColorName
                                )
                            }

                            Marker(
                                state = MarkerState(position = LatLng(friend.latitude, friend.longitude)),
                                title = friend.name,
                                icon = avatarIcon,
                                snippet = "Check."
                            )
                        }
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

@SuppressLint("LocalContextGetResourceValueCall")
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
    friendsLocations: List<FriendLocation>,
    formatDuration: (Long) -> String,
    onFriendClick: (FriendLocation) -> Unit,
    getTimeAgoString: (Long) -> Int
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 450.dp)
            .padding(bottom = 16.dp)
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            StatCell(duration, stringResource(R.string.map_time), true)
            Spacer(Modifier.weight(1f))
            StatCell(distance, "km")
            Spacer(Modifier.weight(1f))
            StatCell(steps, stringResource(R.string.map_steps))
            Spacer(Modifier.weight(1f))
            StatCell(calories, "kcal")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        if (isTracking) context.getString(R.string.map_saving) else context.getString(
                            R.string.map_tracking
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                    onToggleTracking()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracking) Color(0xFFE24B4A) else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (isTracking) stringResource(R.string.map_finish) else stringResource(R.string.map_begin_training),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Row(Modifier.padding(horizontal = 16.dp)) {
            MapTabItem(stringResource(R.string.map_my_routes), selectedTab == MapTab.ROUTES) { onTabSelected(MapTab.ROUTES) }
            MapTabItem(stringResource(R.string.map_friends), selectedTab == MapTab.FRIENDS) { onTabSelected(MapTab.FRIENDS) }
        }

        if (selectedTab == MapTab.ROUTES) {
            var selectedImageUrl by remember { mutableStateOf<String?>(null) }
            Spacer(modifier = Modifier.height(8.dp))
            if (routesHistory.isEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.map_no_routes), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(routesHistory) { route ->
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        val dateStr = if (route.startTime > 0) dateFormat.format(Date(route.startTime)) else context.getString(
                            R.string.map_unknown_date
                        )
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
                                        contentDescription = stringResource(R.string.map_routemap),
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
                                            Text(stringResource(R.string.route_distance), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${String.format(Locale.US, "%.2f", route.distanceKm)} km", fontSize=15.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(stringResource(R.string.route_time), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(formatDuration(route.durationMs), fontSize=15.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(stringResource(R.string.route_steps), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${route.steps}", fontSize=15.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedImageUrl != null) {
                Dialog(
                    onDismissRequest = { selectedImageUrl = null },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { selectedImageUrl = null }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = selectedImageUrl,
                            contentDescription = stringResource(R.string.larger_map),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { selectedImageUrl = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close_btn),
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        if (selectedTab == MapTab.FRIENDS) {
            Spacer(modifier = Modifier.height(8.dp))
            if (friendsLocations.isEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.map_friends_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(friendsLocations) { friend ->
                        FriendLocationCard(
                            friend = friend,
                            minutesAgo = getTimeAgoString(friend.lastSeenMillis),
                            onClick = { onFriendClick(friend) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendLocationCard(
    friend: FriendLocation,
    minutesAgo: Int,
    onClick: () -> Unit
) {
    val timeAgoText = when {
        minutesAgo < 1 -> stringResource(R.string.map_friend_just_now)
        minutesAgo < 60 -> stringResource(R.string.map_friend_mins_ago, minutesAgo)
        else -> stringResource(R.string.map_friend_hours_ago, minutesAgo / 60)
    }

    // 1. Obliczanie inicjałów z nazwy (identycznie jak w innych częściach aplikacji)
    val initials = remember(friend.name) {
        friend.name.trim().split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
    }

    // 2. Sprawdzenie, czy użytkownik ma ustawione własne emoji
    val isEmojiValid = friend.emoji.isNotEmpty() && friend.emoji != "👤"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 3. Kontener awatara z dynamiczną zawartością
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(getColorByName(friend.bgColorName)) // Tło w kolorze użytkownika
                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isEmojiValid) {
                // Wyświetlamy emoji, jeśli jest ustawione
                Text(text = friend.emoji, fontSize = 24.sp)
            } else {
                // Wyświetlamy białe, pogrubione inicjały w przeciwnym razie
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = friend.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.map_friend_last_seen, timeAgoText),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = stringResource(R.string.show_on_map),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ZoomButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun StatCell(value: String, label: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = if (highlight) 20.sp else 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MapTabItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
        if (selected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(Modifier
                .height(3.dp)
                .width(20.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(MaterialTheme.colorScheme.primary))
        }
    }
}