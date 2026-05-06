package com.SzpontCompany.check.ui.map

import android.app.Application
import android.content.Intent
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.map.LatLngPoint
import com.SzpontCompany.check.data.map.Route
import com.SzpontCompany.check.data.map.RouteRepository
import com.google.android.gms.maps.model.LatLng
import com.SzpontCompany.check.BuildConfig
import com.SzpontCompany.check.data.map.FriendLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.maps.android.PolyUtil
class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val isTracking = TrackingManager.isTracking
    val pathPoints = TrackingManager.pathPoints
    val stepsCount = TrackingManager.steps
    val routesHistory = repository.getRoutesHistory().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm.asStateFlow()
    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()
    private val _friendsLocations = MutableStateFlow<List<FriendLocation>>(emptyList())
    val friendsLocations: StateFlow<List<FriendLocation>> = _friendsLocations.asStateFlow()

    val calories: Int
        get() = (_distanceKm.value * 60).toInt()

    init {
        viewModelScope.launch {
            pathPoints.collect { points ->
                _distanceKm.value = calculateDistance(points)
            }
        }

        viewModelScope.launch {
            while (true) {
                if (isTracking.value) {
                    _durationMs.value = System.currentTimeMillis() - TrackingManager.startTime.value
                }
                delay(1000L)
            }
        }
        observeFriendsLocations()
    }

    fun toggleTracking(context: android.content.Context) {
        val intent = Intent(context, LocationTrackingService::class.java)
        if (isTracking.value) {
            intent.action = LocationTrackingService.ACTION_STOP
            context.startService(intent)
            saveCurrentRoute()
        } else {
            _distanceKm.value = 0.0
            _durationMs.value = 0L
            intent.action = LocationTrackingService.ACTION_START
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private fun saveCurrentRoute() {
        val points = pathPoints.value
        if (points.size < 2) {
            TrackingManager.clearData()
            return
        }

        val staticMapUrl = generateStaticMapUrl(points)

        val routeToSave = Route(
            points = points.map { LatLngPoint(it.latitude, it.longitude) },
            startTime = TrackingManager.startTime.value,
            endTime = System.currentTimeMillis(),
            distanceKm = _distanceKm.value,
            durationMs = _durationMs.value,
            calories = calories,
            steps = stepsCount.value,
            mapImageUrl = staticMapUrl
        )

        viewModelScope.launch {
            repository.saveRoute(routeToSave)
            TrackingManager.clearData()
            _distanceKm.value = 0.0
            _durationMs.value = 0L
        }
    }

    private fun generateStaticMapUrl(points: List<LatLng>): String {
        val encodedPath = PolyUtil.encode(points)

        val apiKey = BuildConfig.MAPS_API_KEY
        return "https://maps.googleapis.com/maps/api/staticmap?" +
                "size=600x300" +
                "&scale=2" +
                "&path=color:0xE24B4A|weight:5|enc:$encodedPath" +
                "&markers=size:tiny|color:green|${points.first().latitude},${points.first().longitude}" +
                "&markers=size:tiny|color:red|${points.last().latitude},${points.last().longitude}" +
                "&key=$apiKey"
    }

    private fun calculateDistance(points: List<LatLng>): Double {
        var totalDistance = 0f
        for (i in 0 until points.size - 1) {
            val result = FloatArray(1)
            Location.distanceBetween(
                points[i].latitude, points[i].longitude,
                points[i+1].latitude, points[i+1].longitude,
                result
            )
            totalDistance += result[0]
        }
        return (totalDistance / 1000.0)
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun observeFriendsLocations() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val friendIds = snapshot.get("friends") as? List<String> ?: emptyList()

                if (friendIds.isEmpty()) {
                    _friendsLocations.value = emptyList()
                    return@addSnapshotListener
                }

                firestore.collection("users")
                    .whereIn(FieldPath.documentId(), friendIds.take(10))
                    .addSnapshotListener { usersSnapshot, usersError ->
                        if (usersError != null || usersSnapshot == null) return@addSnapshotListener

                        val locations = usersSnapshot.documents.mapNotNull { doc ->
                            val lat = doc.getDouble("latitude")
                            val lng = doc.getDouble("longitude")
                            val lastSeen = doc.getLong("lastSeenMillis")

                            val showLocation = doc.getBoolean("showLocation") ?: true

                            if (lat != null && lng != null && lastSeen != null && showLocation) {
                                FriendLocation(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "Nieznany",
                                    emoji = doc.getString("avatarEmoji") ?: "👤",
                                    bgColorName = doc.getString("bgColor") ?: "Mint",
                                    latitude = lat,
                                    longitude = lng,
                                    lastSeenMillis = lastSeen
                                )
                            } else null
                        }
                        _friendsLocations.value = locations
                    }
            }
    }

    fun getTimeAgoString(lastSeenMillis: Long): Int {
        val diffMinutes = ((System.currentTimeMillis() - lastSeenMillis) / (1000 * 60)).toInt()
        return diffMinutes
    }
}