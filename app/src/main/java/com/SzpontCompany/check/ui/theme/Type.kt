package com.SzpontCompany.check.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typografia Material 3 dla aplikacji Check.
 *
 * Definiuje style tekstowe używane w całej aplikacji.
 * Domyślnie używa czcionki systemowej z dostosowanymi rozmiarami i wagami.
 *
 * Dostępne style:
 * - bodyLarge: Główny tekst (16sp)
 * - titleLarge: Nagłówki główne (22sp)
 * - labelSmall: Etykiety (11sp)
 * - i inne standardowe style Material 3
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)