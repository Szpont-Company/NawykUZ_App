package com.SzpontCompany.check.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.intPreferencesKey

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val ACCENT_COLOR_KEY = stringPreferencesKey("app_accent_color")
        val LANGUAGE_KEY = stringPreferencesKey("app_language")

        val SHOW_LOCATION_KEY = booleanPreferencesKey("app_show_location")
        val BATTLE_NOTIFICATIONS_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("battle_notifications")
        val STEP_GOAL_KEY = intPreferencesKey("app_step_goal")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "Auto" }
    val accentColorFlow: Flow<String> = context.dataStore.data.map { it[ACCENT_COLOR_KEY] ?: "Mint" }
    val languageFlow: Flow<String> = context.dataStore.data.map { it[LANGUAGE_KEY] ?: "Polski" }
    val battleNotificationsFlow: Flow<Boolean> = context.dataStore.data.map { it[BATTLE_NOTIFICATIONS_KEY] ?: true }
    val showLocationFlow: Flow<Boolean> = context.dataStore.data.map { it[SHOW_LOCATION_KEY] ?: true }
    val stepGoalFlow: Flow<Int> = context.dataStore.data.map { it[STEP_GOAL_KEY] ?: 8000 }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun saveAccentColor(colorName: String) {
        context.dataStore.edit { it[ACCENT_COLOR_KEY] = colorName }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = language }
    }

    suspend fun saveBattleNotifications(isEnabled: Boolean) {
        context.dataStore.edit { it[BATTLE_NOTIFICATIONS_KEY] = isEnabled }
    }

    suspend fun saveShowLocation(location: Boolean) {
        context.dataStore.edit { it[SHOW_LOCATION_KEY] = location }
    }
    suspend fun saveStepGoal(goal: Int) {
        context.dataStore.edit { it[STEP_GOAL_KEY] = goal }
    }

    fun getNotificationPrefs(): Triple<Boolean, String, String> {
        val prefs = context.getSharedPreferences("check_notifications", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("mainReminders", false)
        val hour = prefs.getInt("savedHour", 20)
        val minute = prefs.getInt("savedMinute", 0)
        val frequency = prefs.getString("selectedFrequency", "EVERYDAY") ?: "EVERYDAY"

        val timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)

        return Triple(isEnabled, frequency, timeStr)
    }
}