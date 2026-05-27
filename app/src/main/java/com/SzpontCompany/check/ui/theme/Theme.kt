package com.SzpontCompany.check.ui.theme


import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tworzy schemat kolorów Material 3 na podstawie wybranego koloru akcentu i tematu.
 *
 * Schemat definiuje:
 * - Kolor główny (primary) - wybrany kolor accentu
 * - Kolory wtórne i powierzchni
 * - Kolory tekstu dla obu motywów (jasny/ciemny)
 * - Kolory dla elementów takich jak labele, obramowania, itp.
 *
 * @param accent Kolor akcentu - kolor główny dla interfejsu
 * @param darkTheme true dla motywu ciemnego, false dla jasnego
 * @return Schemat kolorów Material 3
 */
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
            secondary = Color(0xFFf4f4f0),
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

/**
 * Aplikuje theme Check na zawartość kompozy.
 *
 * Dostarcza MaterialTheme z konfiguracją:
 * - Schematu kolorów opartego na wybranym accentie i motywie
 * - Typografii Materiał 3
 * - Kształtów i innych stylów
 *
 * Powinna być używana na najwyższym poziomie hierarchii composables aplikacji.
 *
 * @param darkTheme true dla motywu ciemnego, false dla jasnego
 * @param accent Kolor akcentu do zastosowania w całej aplikacji
 * @param content Zawartość do styli zowania tematem
 */
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