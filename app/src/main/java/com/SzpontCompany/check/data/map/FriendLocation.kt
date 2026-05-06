package com.SzpontCompany.check.data.map

data class FriendLocation(
    val id: String,
    val name: String,
    val emoji: String,
    val bgColorName: String,
    val latitude: Double,
    val longitude: Double,
    val lastSeenMillis: Long
)