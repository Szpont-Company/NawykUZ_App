package com.SzpontCompany.check.data.steps

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

val Context.dataStore by preferencesDataStore(name = "steps_prefs")

class StepRepository(private val context: Context) {

    companion object {
        val DAILY_STEPS_KEY = intPreferencesKey("daily_steps")
        val LAST_SENSOR_READING_KEY = intPreferencesKey("last_sensor_reading")
        val LAST_RECORDED_DATE_KEY = stringPreferencesKey("last_recorded_date")
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal")
    }

    val todayStepsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        val savedDate = preferences[LAST_RECORDED_DATE_KEY] ?: ""
        val today = LocalDate.now().toString()

        if (savedDate != today) 0 else (preferences[DAILY_STEPS_KEY] ?: 0)
    }

    suspend fun updateDailyGoal(newGoal: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_GOAL_KEY] = newGoal
        }
    }

    suspend fun processSensorSteps(newSensorSteps: Int) {
        if (newSensorSteps == 0) return

        val today = LocalDate.now().toString()

        context.dataStore.edit { prefs ->
            val savedDate = prefs[LAST_RECORDED_DATE_KEY] ?: ""
            var dailySteps = prefs[DAILY_STEPS_KEY] ?: 0
            val lastSensorReading = prefs[LAST_SENSOR_READING_KEY] ?: newSensorSteps

            if (dailySteps > 150000) {
                dailySteps = 0
            }

            if (savedDate != today) {
                dailySteps = 0
                prefs[LAST_RECORDED_DATE_KEY] = today
            }

            val stepsToAdd = if (newSensorSteps < lastSensorReading) {
                newSensorSteps
            } else {
                newSensorSteps - lastSensorReading
            }

            if (stepsToAdd > 20000) {
                prefs[LAST_SENSOR_READING_KEY] = newSensorSteps
                return@edit
            }

            dailySteps += stepsToAdd

            prefs[DAILY_STEPS_KEY] = dailySteps
            prefs[LAST_SENSOR_READING_KEY] = newSensorSteps
        }
    }
}