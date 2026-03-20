package com.SzpontCompany.check.ui.theme


import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


fun buildColorScheme(accent: Color, darkTheme: Boolean) : ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = accent,
            secondary = Color(0xFF30302e),
            background = BackgroundDark,
            surface = SurfaceDark,
            onPrimary = Color.White,
            onBackground = Color(0xFFE8E8E8),
            onSurface = Color(0xFFE8E8E8),
            onSurfaceVariant = Color(0xFF8A8A8A),  // labele, placeholdery
            surfaceVariant = Color(0xFF2a2a2e),    // np. tło tab switcha
            outline = Color(0xFF3a3a3e),           // obramowania TextField
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = BackgroundLight,
            surface = SurfaceLight,
            onPrimary = Color.White,
            onBackground = Color(0xFF111112),
            onSurface = Color(0xFF111112),
            onSurfaceVariant = Color(0xFF6B6B6B),
            surfaceVariant = Color(0xFFE8E8E4),
            outline = Color(0xFFCCCCC8),
        )
    }
}

@Composable
fun CheckTheme(
    darkTheme: Boolean,
    accent: Color,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = buildColorScheme(accent, darkTheme),
        content = content
    )
}