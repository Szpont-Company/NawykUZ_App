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
            background = BackgroundDark,
            surface = SurfaceDark,
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = BackgroundLight,
            surface = SurfaceLight,
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