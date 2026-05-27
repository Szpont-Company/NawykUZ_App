package com.SzpontCompany.check.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Moduł definiujący paletę kolorów aplikacji Check.
 *
 * Zawiera:
 * - Kolory główne (8 wariantów tematycznych)
 * - Kolory wtórne (dopełniające warianty główne)
 * - Kolory tła i powierzchni dla jasnego i ciemnego motywu
 * - Funkcje pomocnicze do mapowania kolorów
 *
 * Kolory są używane w całej aplikacji dla spójności wizualnej.
 */

// Preset colors
/**
 * Zielona, podstawowa barwa accentu aplikacji - główny kolor motywu.
 */
val Mint = Color(0xFF1d9e75)
/**
 * Indygo - wariant koloru accentu do wyboru przez użytkownika.
 */
val Indigo = Color(0xFF7F77DD)
/**
 * Koral - wariant koloru accentu do wyboru przez użytkownika.
 */
val Coral = Color(0xFFD85A30)
/**
 * Niebo - wariant koloru accentu do wyboru przez użytkownika.
 */
val Sky = Color(0xFF378ADD)
/**
 * Róża - wariant koloru accentu do wyboru przez użytkownika.
 */
val Rose = Color(0xFFD4537E)
/**
 * Kaktus - wariant koloru accentu do wyboru przez użytkownika.
 */
val Cactus = Color(0xFF639922)
/**
 * Bursztyn - wariant koloru accentu do wyboru przez użytkownika.
 */
val Amber = Color(0xFFBA7517)
/**
 * Karmin - wariant koloru accentu do wyboru przez użytkownika.
 */
val Crimson = Color(0xFFE24B4A)


/**
 * Wtórne kolory - wersje o zwiększonej jasności dla podkreślenia.
 */

val SecondaryMint = Color(0xFF25C599)
val SecondaryIndigo = Color(0xFF9F99E9)
val SecondaryCoral = Color(0xFFD2911E)
val SecondaryCrimson = Color(0xFFE96E6C)
val SecondarySky = Color(0xFF5AA3E8)
val SecondaryRose = Color(0xFFE0769C)
val SecondaryCactus = Color(0xFF7EC028)
val SecondaryAmber = Color(0xFFE87956)

/**
 * Kolory tła i powierzchni dla motywu ciemnego.
 */
val BackgroundDark = Color(0xFF111112)
val SurfaceDark = Color(0xFF1a1a1e)

/**
 * Kolory tła i powierzchni dla motywu jasnego.
 */
val BackgroundLight = Color(0xFFf4f4f0)
val SurfaceLight = Color(0xFFffffff)

/**
 * Mapuje nazwę koloru do obiektu Color.
 *
 * @param name Nazwa koloru (Amber, Indigo, Coral, Sky, Rose, Cactus, Crimson, Mint)
 * @return Odpowiadający obiekt Color, lub Mint jeśli nazwa jest nieznana
 */
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

/**
 * Mapuje obiekt Color na odpowiadającą mu nazwę.
 *
 * @param color Obiekt Color do mapowania
 * @return Nazwa koloru (Amber, Indigo, Coral, Sky, Rose, Cactus, Crimson, Mint)
 */
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

/**
 * Uzyskuje kolor wtórny dla danego koloru głównego.
 *
 * Kolory wtórne są jaśniejszymi wersjami koloru głównego,
 * używane do podkreślenia i wizualnego hierarchii.
 *
 * @param primary Kolor główny (primary color)
 * @return Odpowiadający kolor wtórny (secondary color)
 */
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