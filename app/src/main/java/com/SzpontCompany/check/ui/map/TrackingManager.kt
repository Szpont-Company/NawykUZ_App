package com.SzpontCompany.check.ui.map

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TrackingManager {
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _pathPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints: StateFlow<List<LatLng>> = _pathPoints.asStateFlow()

    val startTime = MutableStateFlow(0L)

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    fun startTracking() {
        _isTracking.value = true
        _pathPoints.value = emptyList()
        startTime.value = System.currentTimeMillis()
    }

    fun stopTracking() {
        _isTracking.value = false
    }

    fun addPoint(point: LatLng) {
        if (_isTracking.value) {
            val currentList = _pathPoints.value.toMutableList()
            currentList.add(point)
            _pathPoints.value = currentList
        }
    }

    fun addStep() {
        if (_isTracking.value) {
            _steps.value += 1
        }
    }

    fun clearData() {
        _pathPoints.value = emptyList()
        startTime.value = 0L
    }
}