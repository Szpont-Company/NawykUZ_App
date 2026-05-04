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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RouteRepository()
    val isTracking = TrackingManager.isTracking
    val pathPoints = TrackingManager.pathPoints
    val stepsCount = TrackingManager.steps
    val routesHistory = repository.getRoutesHistory().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private val _distanceKm = MutableStateFlow(0.0)
    val distanceKm: StateFlow<Double> = _distanceKm.asStateFlow()
    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    val calories: Int
        get() = (_distanceKm.value * 60).toInt()

    init {
        // Liczenie dystansu na żywo
        viewModelScope.launch {
            pathPoints.collect { points ->
                _distanceKm.value = calculateDistance(points)
            }
        }

        // Licznik czasu
        viewModelScope.launch {
            while (true) {
                if (isTracking.value) {
                    _durationMs.value = System.currentTimeMillis() - TrackingManager.startTime.value
                }
                delay(1000L)
            }
        }
    }

    fun toggleTracking(context: android.content.Context) {
        val intent = Intent(context, LocationTrackingService::class.java)
        if (isTracking.value) {
            // Zatrzymujemy i Zapisujemy
            intent.action = LocationTrackingService.ACTION_STOP
            context.startService(intent)
            saveCurrentRoute()
        } else {
            // Startujemy
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
            return // Ignorujemy zbyt krótkie trasy
        }

        val routeToSave = Route(
            points = points.map { LatLngPoint(it.latitude, it.longitude) },
            startTime = TrackingManager.startTime.value,
            endTime = System.currentTimeMillis(),
            distanceKm = _distanceKm.value,
            durationMs = _durationMs.value,
            calories = calories,
            steps = stepsCount.value
        )

        viewModelScope.launch {
            repository.saveRoute(routeToSave)
            TrackingManager.clearData()
            _distanceKm.value = 0.0
            _durationMs.value = 0L
        }
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
}