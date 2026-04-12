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


// Secondary Preset Colors

val SecondaryMint = Color(0xFF25C599)
val SecondaryIndigo = Color(0xFF9F99E9)
val SecondaryCoral = Color(0xFFD2911E)
val SecondaryCrimson = Color(0xFFE96E6C)
val SecondarySky = Color(0xFF5AA3E8)
val SecondaryRose = Color(0xFFE0769C)
val SecondaryCactus = Color(0xFF7EC028)
val SecondaryAmber = Color(0xFFE87956)

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

fun getSecondaryColor(primary: Color): Color {
    return when(primary) {
        Amber -> SecondaryAmber
        Indigo -> SecondaryIndigo
        Coral -> SecondaryCoral
        Sky -> SecondarySky
        Rose -> SecondaryRose
        Cactus -> SecondaryCactus
        Crimson -> SecondaryCrimson
        else -> SecondaryMint
    }
}