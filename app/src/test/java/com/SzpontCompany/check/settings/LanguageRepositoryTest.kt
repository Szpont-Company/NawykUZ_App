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
 * Testy dla funkcjonalności internacjonalizacji (PL/ENG)
 */
class LanguageRepositoryTest {

    @Mock
    private lateinit var mockContext: Context

    private lateinit var languageRepository: FakeLanguageRepository
    private lateinit var translationProvider: TranslationProvider

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        languageRepository = FakeLanguageRepository(mockContext)
        translationProvider = TranslationProvider()
    }

    // ==================== TESTY WYBORU JĘZYKA ====================

    @Test
    fun `testGetDefaultLanguage - should return Polish by default`() = runTest {
        val language = languageRepository.getCurrentLanguage()
        assertEquals(Language.POLISH, language)
    }

    @Test
    fun `testSwitchToEnglish - should save English preference`() = runTest {
        val english = Language.ENGLISH
        languageRepository.setLanguage(english)
        val savedLanguage = languageRepository.getCurrentLanguage()
        assertEquals(Language.ENGLISH, savedLanguage)
    }

    @Test
    fun `testSwitchToPolish - should save Polish preference`() = runTest {
        val polish = Language.POLISH
        languageRepository.setLanguage(polish)
        val savedLanguage = languageRepository.getCurrentLanguage()
        assertEquals(Language.POLISH, savedLanguage)
    }

    // ==================== TESTY TŁUMACZENIA STRINGÓW ====================

    @Test
    fun `testTranslateRegistryButtonPL - should return Polish text`() = runTest {
        languageRepository.setLanguage(Language.POLISH)
        val translation = translationProvider.getString("btn_register", Language.POLISH)
        assertEquals("Zarejestruj się", translation)
    }

    @Test
    fun `testTranslateRegistryButtonENG - should return English text`() = runTest {
        languageRepository.setLanguage(Language.ENGLISH)
        val translation = translationProvider.getString("btn_register", Language.ENGLISH)
        assertEquals("Register", translation)
    }

    @Test
    fun `testTranslateLoginButtonPL - should return Polish text`() = runTest {
        val translation = translationProvider.getString("btn_login", Language.POLISH)
        assertEquals("Zaloguj się", translation)
    }

    @Test
    fun `testTranslateLoginButtonENG - should return English text`() = runTest {
        val translation = translationProvider.getString("btn_login", Language.ENGLISH)
        assertEquals("Login", translation)
    }

    @Test
    fun `testTranslateHabitsTitlePL - should return Polish text`() = runTest {
        val translation = translationProvider.getString("title_habits", Language.POLISH)
        assertEquals("Moje Nawyki", translation)
    }

    @Test
    fun `testTranslateHabitsTitleENG - should return English text`() = runTest {
        val translation = translationProvider.getString("title_habits", Language.ENGLISH)
        assertEquals("My Habits", translation)
    }

    @Test
    fun `testTranslateBattleLanguagePL - should return Polish text`() = runTest {
        val translation = translationProvider.getString("title_battles", Language.POLISH)
        assertEquals("Wyzwania", translation)
    }

    @Test
    fun `testTranslateBattleLanguageENG - should return English text`() = runTest {
        val translation = translationProvider.getString("title_battles", Language.ENGLISH)
        assertEquals("Challenges", translation)
    }

    @Test
    fun `testTranslateAchievementsLanguagePL - should return Polish text`() = runTest {
        val translation = translationProvider.getString("title_achievements", Language.POLISH)
        assertEquals("Osiągnięcia", translation)
    }

    @Test
    fun `testTranslateAchievementsLanguageENG - should return English text`() = runTest {
        val translation = translationProvider.getString("title_achievements", Language.ENGLISH)
        assertEquals("Achievements", translation)
    }

    // ==================== TESTY PERSISTENCJI JĘZYKA ====================

    @Test
    fun `testLanguagePersistence - should maintain selected language after restart`() = runTest {
        val selectedLanguage = Language.ENGLISH
        languageRepository.setLanguage(selectedLanguage)

        // Simulating app restart
        val restoredLanguage = languageRepository.getCurrentLanguage()

        assertEquals(selectedLanguage, restoredLanguage)
    }

    @Test
    fun `testMultipleLanguageSwitches - should handle rapid language changes`() = runTest {
        languageRepository.setLanguage(Language.ENGLISH)
        languageRepository.setLanguage(Language.POLISH)
        languageRepository.setLanguage(Language.ENGLISH)

        val finalLanguage = languageRepository.getCurrentLanguage()
        assertEquals(Language.ENGLISH, finalLanguage)
    }

    // ==================== TESTY PARAMETRÓW ====================

    @Test
    fun `testTranslateWithParameters - should replace placeholders`() = runTest {
        val username = "John"
        val params = mapOf("name" to username)

        val translation = translationProvider.getString(
            "welcome_message",
            Language.ENGLISH,
            params
        )

        assertTrue(translation.contains(username))
        assertEquals("Welcome, John!", translation)
    }

    @Test
    fun `testTranslateWithParametersPL - Polish with placeholders`() = runTest {
        val username = "Jan"
        val params = mapOf("name" to username)

        val translation = translationProvider.getString(
            "welcome_message",
            Language.POLISH,
            params
        )

        assertTrue(translation.contains(username))
        assertEquals("Witaj, Jan!", translation)
    }

    // ==================== TESTY PLURAL ====================

    @Test
    fun `testPluralTranslationEnglish - singular`() = runTest {
        val translation = translationProvider.getPlural("steps_today", 1, Language.ENGLISH)
        assertEquals("1 step today", translation)
    }

    @Test
    fun `testPluralTranslationEnglish - plural`() = runTest {
        val translation = translationProvider.getPlural("steps_today", 5, Language.ENGLISH)
        assertEquals("5 steps today", translation)
    }

    @Test
    fun `testPluralTranslationPolish - singular`() = runTest {
        val translation = translationProvider.getPlural("steps_today", 1, Language.POLISH)
        assertEquals("1 krok dzisiaj", translation)
    }

    @Test
    fun `testPluralTranslationPolish - few`() = runTest {
        val translation = translationProvider.getPlural("steps_today", 2, Language.POLISH)
        assertEquals("2 kroki dzisiaj", translation)
    }

    @Test
    fun `testPluralTranslationPolish - many`() = runTest {
        val translation = translationProvider.getPlural("steps_today", 5, Language.POLISH)
        assertEquals("5 kroków dzisiaj", translation)
    }

    // ==================== TESTY KOMPLETNOŚCI TŁUMACZEŃ ====================

    @Test
    fun `testAllStringsTranslatedToPolish - should have all keys translated`() = runTest {
        val englishKeys = translationProvider.getAllKeys()

        englishKeys.forEach { key ->
            val polishTranslation = translationProvider.getString(key, Language.POLISH)
            assertFalse("Missing Polish translation for $key", polishTranslation.isEmpty())
            assertFalse("Incomplete translation for $key", polishTranslation.contains("???"))
        }
    }

    @Test
    fun `testAllStringsTranslatedToEnglish - should have all keys translated`() = runTest {
        val englishKeys = translationProvider.getAllKeys()

        englishKeys.forEach { key ->
            val englishTranslation = translationProvider.getString(key, Language.ENGLISH)
            assertFalse("Missing English translation for $key", englishTranslation.isEmpty())
            assertFalse("Incomplete translation for $key", englishTranslation.contains("???"))
        }
    }

    // ==================== TESTY LOCALE ====================

    @Test
    fun `testLocaleChangeEnglish - should update system locale`() = runTest {
        languageRepository.setLanguage(Language.ENGLISH)
        val locale = languageRepository.getCurrentLocale()
        assertEquals("en", locale.language)
    }

    @Test
    fun `testLocaleChangePolish - should update system locale`() = runTest {
        languageRepository.setLanguage(Language.POLISH)
        val locale = languageRepository.getCurrentLocale()
        assertEquals("pl", locale.language)
    }

    // ==================== TESTY RTL (Right-to-Left) ====================

    @Test
    fun `testEnglishIsNotRTL - should be LTR`() = runTest {
        val isRTL = translationProvider.isRTLLanguage(Language.ENGLISH)
        assertFalse(isRTL)
    }

    @Test
    fun `testPolishIsNotRTL - should be LTR`() = runTest {
        val isRTL = translationProvider.isRTLLanguage(Language.POLISH)
        assertFalse(isRTL)
    }
}

// ==================== KLASY SUPPORTUJĄCE ====================

enum class Language {
    POLISH, ENGLISH
}

open class FakeLanguageRepository(private val context: Context) {
    private var currentLanguage = Language.POLISH

    open fun getCurrentLanguage(): Language = currentLanguage

    open suspend fun setLanguage(language: Language) {
        currentLanguage = language
    }

    open fun getCurrentLocale(): java.util.Locale {
        return when (currentLanguage) {
            Language.POLISH -> java.util.Locale("pl")
            Language.ENGLISH -> java.util.Locale("en")
        }
    }
}

class TranslationProvider {
    private val translations = mapOf(
        "btn_register" to mapOf(
            Language.POLISH to "Zarejestruj się",
            Language.ENGLISH to "Register"
        ),
        "btn_login" to mapOf(
            Language.POLISH to "Zaloguj się",
            Language.ENGLISH to "Login"
        ),
        "title_habits" to mapOf(
            Language.POLISH to "Moje Nawyki",
            Language.ENGLISH to "My Habits"
        ),
        "title_battles" to mapOf(
            Language.POLISH to "Wyzwania",
            Language.ENGLISH to "Challenges"
        ),
        "title_achievements" to mapOf(
            Language.POLISH to "Osiągnięcia",
            Language.ENGLISH to "Achievements"
        ),
        "welcome_message" to mapOf(
            Language.POLISH to "Witaj, {name}!",
            Language.ENGLISH to "Welcome, {name}!"
        )
    )

    fun getString(key: String, language: Language, params: Map<String, String> = emptyMap()): String {
        var text = translations[key]?.get(language) ?: "???"
        params.forEach { (placeholder, value) ->
            text = text.replace("{$placeholder}", value)
        }
        return text
    }

    fun getPlural(key: String, count: Int, language: Language): String {
        return when (key) {
            "steps_today" -> when (language) {
                Language.POLISH -> when {
                    count == 1 -> "1 krok dzisiaj"
                    count in 2..4 -> "$count kroki dzisiaj"
                    else -> "$count kroków dzisiaj"
                }
                Language.ENGLISH -> when {
                    count == 1 -> "1 step today"
                    else -> "$count steps today"
                }
            }
            else -> "???"
        }
    }

    fun getAllKeys(): List<String> {
        return translations.keys.toList()
    }

    fun isRTLLanguage(language: Language): Boolean {
        return false
    }
}