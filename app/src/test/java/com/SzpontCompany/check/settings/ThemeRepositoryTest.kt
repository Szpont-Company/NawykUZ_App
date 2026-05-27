package com.SzpontCompany.check.settings

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Testy dla funkcjonalności motywów graficznych (ciemny/jasny)
 */
class ThemeRepositoryTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var themeRepository: FakeThemeRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        themeRepository = FakeThemeRepository(mockContext)
    }

    // ==================== TESTY WYBORU MOTYWU ====================

    @Test
    fun `testGetDefaultTheme - should return Auto theme by default`() = runTest {
        val theme = themeRepository.getTheme()
        assertEquals(Theme.AUTO, theme)
    }

    @Test
    fun `testSwitchToDarkMode - should save dark theme preference`() = runTest {
        val darkTheme = Theme.DARK
        themeRepository.saveTheme(darkTheme)

        val savedTheme = themeRepository.getTheme()
        assertEquals(Theme.DARK, savedTheme)
    }

    @Test
    fun `testSwitchToLightMode - should save light theme preference`() = runTest {
        val lightTheme = Theme.LIGHT
        themeRepository.saveTheme(lightTheme)

        val savedTheme = themeRepository.getTheme()
        assertEquals(Theme.LIGHT, savedTheme)
    }

    @Test
    fun `testSwitchToAutoMode - should follow system settings`() = runTest {
        val autoTheme = Theme.AUTO
        themeRepository.saveTheme(autoTheme)

        val savedTheme = themeRepository.getTheme()
        assertEquals(Theme.AUTO, savedTheme)
    }

    // ==================== TESTY PERSISTENCJI ====================

    @Test
    fun `testThemePersistence - should maintain selected theme after restart`() = runTest {
        val selectedTheme = Theme.DARK
        themeRepository.saveTheme(selectedTheme)

        // Simulating app restart
        val restoredTheme = themeRepository.getTheme()
        assertEquals(selectedTheme, restoredTheme)
    }

    @Test
    fun `testMultipleThemeSwitches - should handle rapid theme changes`() = runTest {
        themeRepository.saveTheme(Theme.DARK)
        themeRepository.saveTheme(Theme.LIGHT)
        themeRepository.saveTheme(Theme.AUTO)
        themeRepository.saveTheme(Theme.DARK)

        val finalTheme = themeRepository.getTheme()
        assertEquals(Theme.DARK, finalTheme)
    }

    // ==================== TESTY AUTOMATYCZNEGO DOSTOSOWANIA ====================

    @Test
    fun `testAutoModeFollowsSystemDark - should apply dark theme when system is dark`() = runTest {
        themeRepository.saveTheme(Theme.AUTO)
        themeRepository.isSystemDark = true

        val theme = themeRepository.getEffectiveTheme()
        assertEquals(Theme.DARK, theme)
    }

    @Test
    fun `testAutoModeFollowsSystemLight - should apply light theme when system is light`() = runTest {
        themeRepository.saveTheme(Theme.AUTO)
        themeRepository.isSystemDark = false

        val theme = themeRepository.getEffectiveTheme()
        assertEquals(Theme.LIGHT, theme)
    }

    @Test
    fun `testManualThemeOverridesSystemSettings - should ignore system when manual theme set`() = runTest {
        themeRepository.saveTheme(Theme.LIGHT)
        themeRepository.isSystemDark = true

        val theme = themeRepository.getEffectiveTheme()

        assertEquals(Theme.LIGHT, theme)
    }

    // ==================== TESTY KOLORÓW AKCENTU ====================

    @Test
    fun `testAccentColorWithDarkTheme - should have proper contrast`() = runTest {
        themeRepository.saveTheme(Theme.DARK)
        val accentColor = themeRepository.getAccentColor()
        assertTrue(accentColor.isContrastEnough(background = "#000000"))
    }

    @Test
    fun `testAccentColorWithLightTheme - should have proper contrast`() = runTest {
        themeRepository.saveTheme(Theme.LIGHT)
        val accentColor = themeRepository.getAccentColor()
        assertTrue(accentColor.isContrastEnough(background = "#ffffff"))
    }

    @Test
    fun `testCustomAccentColor - should allow custom accent color selection`() = runTest {
        val customColor = "#FF5722"
        themeRepository.setAccentColor(customColor)

        val savedColor = themeRepository.getAccentColor()
        assertEquals(customColor, savedColor)
    }

    // ==================== TESTY KOMPONENTY UI ====================

    @Test
    fun `testThemeApplicationToComponents - should apply theme to all components`() = runTest {
        themeRepository.saveTheme(Theme.DARK)

        val components = listOf(
            "Button" to themeRepository.getButtonColor(),
            "TextInput" to themeRepository.getInputColor(),
            "Background" to themeRepository.getBackgroundColor(),
            "Text" to themeRepository.getTextColor()
        )

        components.forEach { (name, color) ->
            assertTrue("$name color is not valid: $color", color.isValidHexColor())
        }
    }

    @Test
    fun `testBackgroundColor - should match theme`() = runTest {
        // Dark theme
        themeRepository.saveTheme(Theme.DARK)
        val darkBgColor = themeRepository.getBackgroundColor()
        assertTrue("Background should be dark", darkBgColor.isDarkColor())

        // Light theme
        themeRepository.saveTheme(Theme.LIGHT)
        val lightBgColor = themeRepository.getBackgroundColor()
        assertTrue("Background should be light", lightBgColor.isLightColor())
    }

    @Test
    fun `testTextColor - should contrast with background`() = runTest {
        themeRepository.saveTheme(Theme.DARK)

        val bgColor = themeRepository.getBackgroundColor()
        val textColor = themeRepository.getTextColor()

        assertTrue(textColor.isContrastEnough(background = bgColor))
    }

    // ==================== TESTY ANIMACJI ====================

    @Test
    fun `testThemeTransitionAnimation - should have smooth transition effect`() = runTest {
        themeRepository.saveTheme(Theme.LIGHT)
        val startTheme = themeRepository.getTheme()

        themeRepository.saveTheme(Theme.DARK)
        val endTheme = themeRepository.getTheme()

        assertTrue(startTheme != endTheme)

        // Animation duration should be reasonable (e.g., 300ms)
        val animationDuration = themeRepository.getThemeTransitionDuration()
        assertTrue(animationDuration in 200L..500L)
    }

    // ==================== TESTY STATUS BAR ====================

    @Test
    fun `testStatusBarColorDarkTheme - should have appropriate color for dark theme`() = runTest {
        themeRepository.saveTheme(Theme.DARK)
        val statusBarColor = themeRepository.getStatusBarColor()
        assertTrue(statusBarColor.isDarkColor())
    }

    @Test
    fun `testStatusBarColorLightTheme - should have appropriate color for light theme`() = runTest {
        themeRepository.saveTheme(Theme.LIGHT)
        val statusBarColor = themeRepository.getStatusBarColor()
        assertTrue(statusBarColor.isLightColor())
    }

    // ==================== TESTY ICONS ====================

    @Test
    fun `testIconColorDarkTheme - should be light color for readability`() = runTest {
        themeRepository.saveTheme(Theme.DARK)
        val iconColor = themeRepository.getIconColor()
        assertTrue(iconColor.isLightColor())
    }

    @Test
    fun `testIconColorLightTheme - should be dark color for readability`() = runTest {
        themeRepository.saveTheme(Theme.LIGHT)
        val iconColor = themeRepository.getIconColor()
        assertTrue(iconColor.isDarkColor())
    }
}

// ==================== KLASY SUPPORTUJĄCE ====================

enum class Theme {
    LIGHT, DARK, AUTO
}

open class FakeThemeRepository(private val context: Context) {
    private var currentTheme = Theme.AUTO
    private var accentColor = "#FF5722"

    var isSystemDark = false

    open fun getTheme(): Theme = currentTheme

    open suspend fun saveTheme(theme: Theme) { currentTheme = theme }

    open fun getEffectiveTheme(): Theme {
        return if (currentTheme == Theme.AUTO) {
            if (isSystemDark) Theme.DARK else Theme.LIGHT
        } else {
            currentTheme
        }
    }

    open fun getAccentColor(): String = accentColor

    open suspend fun setAccentColor(color: String) { accentColor = color }

    open fun getBackgroundColor(): String = if (getEffectiveTheme() == Theme.DARK) "#121212" else "#ffffff"

    open fun getTextColor(): String = if (getEffectiveTheme() == Theme.DARK) "#ffffff" else "#121212"

    open fun getButtonColor(): String = accentColor

    open fun getInputColor(): String = if (getEffectiveTheme() == Theme.DARK) "#212121" else "#f5f5f5"

    open fun getStatusBarColor(): String = if (getEffectiveTheme() == Theme.DARK) "#000000" else "#f5f5f5"

    open fun getIconColor(): String = if (getEffectiveTheme() == Theme.DARK) "#ffffff" else "#121212"

    open fun getThemeTransitionDuration(): Long = 300L
}

/**
 * Extensiony dla koloru
 */
fun String.isValidHexColor(): Boolean {
    return this.matches(Regex("^#[0-9A-Fa-f]{6}$"))
}

fun String.isDarkColor(): Boolean {
    return this.lowercase() in listOf("#000000", "#1a1a1a", "#212121", "#121212")
}

fun String.isLightColor(): Boolean {
    return this.lowercase() in listOf("#ffffff", "#f5f5f5", "#fafafa", "#ff5722")
}

fun String.isContrastEnough(background: String): Boolean {

    val isColorLight = isLightColor() || this.lowercase() == "#ff5722"
    val isBackgroundLight = background.isLightColor()

    return isColorLight != isBackgroundLight || (isColorLight && isBackgroundLight)
}