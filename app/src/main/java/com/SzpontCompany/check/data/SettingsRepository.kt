package com.SzpontCompany.check.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val ACCENT_COLOR_KEY = stringPreferencesKey("app_accent_color")
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "Auto" }
    val accentColorFlow: Flow<String> = context.dataStore.data.map { it[ACCENT_COLOR_KEY] ?: "Mint" }
    val languageFlow: Flow<String> = context.dataStore.data.map { it[LANGUAGE_KEY] ?: "Polski" }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun saveAccentColor(colorName: String) {
        context.dataStore.edit { it[ACCENT_COLOR_KEY] = colorName }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = language }
    }
}