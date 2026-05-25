package com.SzpontCompany.check.data.steps

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Instancja DataStore dla preferencji kroków */
val Context.dataStore by preferencesDataStore(name = "steps_prefs")

/**
 * StepRepository - repozytorium zarządzające danymi kroków użytkownika.
 *
 * Odpowiada za:
 * - Przechowywanie liczby kroków na dzień w DataStore
 * - Przetwarzanie surowych danych z sensora krokomierza
 * - Resetowanie danych kroków przy nowym dniu
 * - Zarządzanie celem kroków na dzień
 *
 * Przechowywane wartości:
 * - daily_steps: Liczba kroków w bieżącym dniu
 * - last_sensor_reading: Ostatnia wartość z sensora (do obliczania różnicy)
 * - last_recorded_date: Data ostatniego zapisu
 * - daily_goal: Cel kroków na dzień
 *
 * @since 1.0
 * @author Szpont Company
 */
class StepRepository(private val context: Context) {

    companion object {
        /** Klucz dla liczby kroków dzisiaj */
        val DAILY_STEPS_KEY = intPreferencesKey("daily_steps")
        /** Klucz dla ostatniej wartości z sensora */
        val LAST_SENSOR_READING_KEY = intPreferencesKey("last_sensor_reading")
        /** Klucz dla daty ostatniego zapisu */
        val LAST_RECORDED_DATE_KEY = stringPreferencesKey("last_recorded_date")
        /** Klucz dla celu kroków */
        val DAILY_GOAL_KEY = intPreferencesKey("daily_goal")
    }

    /**
     * Flow emitujący liczbę kroków dzisiaj.
     *
     * Automatycznie resetuje się do 0 dla nowego dnia.
     * Obserwowanie tego flow pozwala na real-time aktualizacje UI.
     */
    val todayStepsFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        val savedDate = preferences[LAST_RECORDED_DATE_KEY] ?: ""
        val today = LocalDate.now().toString()

        if (savedDate != today) 0 else (preferences[DAILY_STEPS_KEY] ?: 0)
    }

    /**
     * Aktualizuje cel kroków na dzień.
     *
     * @param newGoal Nowy cel kroków
     */
    suspend fun updateDailyGoal(newGoal: Int) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_GOAL_KEY] = newGoal
        }
    }

    /**
     * Przetwarza nową wartość z sensora krokomierza.
     *
     * Logika:
     * 1. Sprawdza czy data jest dzisiejszym dniem, jeśli nie resetuje licznik
     * 2. Oblicza różnicę między bieżącą a ostatnią wartością sensora
     * 3. Odfiltruje anomalie (zmiana > 20000 kroków na raz)
     * 4. Zaokrężenia bardzo duże wartości (> 150000)
     *
     * Obsługuje reset sensora (gdy newSensorSteps < lastReading)
     * co zdarza się po restarcie urządzenia.
     *
     * @param newSensorSteps Nowa wartość z sensora (liczba kroków od ostatniego restartu)
     */
    suspend fun processSensorSteps(newSensorSteps: Int) {
        if (newSensorSteps == 0) return

        val today = LocalDate.now().toString()

        context.dataStore.edit { prefs ->
            val savedDate = prefs[LAST_RECORDED_DATE_KEY] ?: ""
            var dailySteps = prefs[DAILY_STEPS_KEY] ?: 0
            val lastSensorReading = prefs[LAST_SENSOR_READING_KEY] ?: newSensorSteps

            // Zabezpieczenie przed błędnymi wartościami
            if (dailySteps > 150000) {
                dailySteps = 0
            }

            // Reset licznika na nowy dzień
            if (savedDate != today) {
                dailySteps = 0
                prefs[LAST_RECORDED_DATE_KEY] = today
            }

            // Obliczenie kroków do dodania (obsługuje reset sensora)
            val stepsToAdd = if (newSensorSteps < lastSensorReading) {
                newSensorSteps
            } else {
                newSensorSteps - lastSensorReading
            }

            // Odfiltrowanie anomalii (np. błędy sensora)
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