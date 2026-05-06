package com.SzpontCompany.check.data.map

data class LatLngPoint(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

data class Route(
    val id: String = "",
    val points: List<LatLngPoint> = emptyList(),
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val distanceKm: Double = 0.0,
    val durationMs: Long = 0L,
    val calories: Int = 0,
    val steps: Int = 0,
    val mapImageUrl: String = ""
)