package com.SzpontCompany.check.ui.theme

import androidx.compose.ui.graphics.Color

// Preset colors

val Mint = Color(0xFF1d9e75)
val Indigo = Color(0xFF7F77DD)
val Coral = Color(0xFFD85A30)
val Sky = Color(0xFF378ADD)
val Rose = Color(0xFFD4537E)
val Cactus = Color(0xFF639922)
val Amber = Color(0xFFBA7517)
val Crimson = Color(0xFFE24B4A)

// Background colors

val BackgroundDark = Color(0xFF111112)
val SurfaceDark = Color(0xFF1a1a1e)

val BackgroundLight = Color(0xFFf4f4f0)
val SurfaceLight = Color(0xFFffffff)

fun getColorByName(name: String): Color {
    return when(name) {
        "Amber" -> Amber
        "Indigo" -> Indigo
        "Coral" -> Coral
        "Sky" -> Sky
        "Rose" -> Rose
        "Cactus" -> Cactus
        "Crimson" -> Crimson
        else -> Mint
    }
}

fun getColorName(color: Color): String {
    return when(color) {
        Amber -> "Amber"
        Indigo -> "Indigo"
        Coral -> "Coral"
        Sky -> "Sky"
        Rose -> "Rose"
        Cactus -> "Cactus"
        Crimson -> "Crimson"
        else -> "Mint"
    }
}
