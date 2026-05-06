package com.SzpontCompany.check.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
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

        val SHOW_LOCATION_KEY = booleanPreferencesKey("app_show_location")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "Auto" }
    val accentColorFlow: Flow<String> = context.dataStore.data.map { it[ACCENT_COLOR_KEY] ?: "Mint" }
    val languageFlow: Flow<String> = context.dataStore.data.map { it[LANGUAGE_KEY] ?: "Polski" }
    val showLocationFlow: Flow<Boolean> = context.dataStore.data.map { it[SHOW_LOCATION_KEY] ?: true }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun saveAccentColor(colorName: String) {
        context.dataStore.edit { it[ACCENT_COLOR_KEY] = colorName }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = language }
    }

    suspend fun saveShowLocation(location: Boolean) {
        context.dataStore.edit { it[SHOW_LOCATION_KEY] = location }
    }
}