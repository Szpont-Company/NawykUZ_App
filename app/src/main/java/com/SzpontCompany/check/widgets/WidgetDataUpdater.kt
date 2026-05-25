package com.SzpontCompany.check.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.SzpontCompany.check.data.steps.StepRepository
import com.SzpontCompany.check.data.steps.dataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * WidgetDataUpdater - aktualizuje dane wyświetlane na widżetach aplikacji.
 *
 * Odpowiada za:
 * - Pobieranie bieżących danych kroków z DataStore
 * - Aktualizacja widżetu kroków z najnowszymi wartościami
 * - Reset danych dla nowego dnia
 *
 * Obsługuje widżet kroków (StepsWidget) wyświetlający:
 * - Aktualną liczbę kroków dzisiaj
 * - Cel kroków na dzień
 * - Procent wykonania
 *
 * @since 1.0
 * @author Szpont Company
 */
class WidgetDataUpdater {
    /**
     * Aktualizuje dane widżetu kroków ze świeżymi wartościami z DataStore.
     *
     * Logika:
     * 1. Pobiera preferencje kroków z DataStore
     * 2. Sprawdza czy data się nie zmieniła (reset kroków dla nowego dnia)
     * 3. Pobiera wszystkie zarejestrowane widżety StepsWidget
     * 4. Aktualizuje stan każdego widżetu
     * 5. Odświeża UI widżetu
     *
     * Wywoływane automatycznie za każdym razem gdy zmienia się liczba kroków.
     *
     * @param context Kontekst aplikacji
     */
    suspend fun updateStepsWidgetData(context: Context) {
        val prefs = context.dataStore.data.first()
        val today = LocalDate.now().toString()
        val savedDate = prefs[StepRepository.LAST_RECORDED_DATE_KEY] ?: ""
        val currentSteps = if (savedDate == today) prefs[StepRepository.DAILY_STEPS_KEY] ?: 0 else 0

        val dailyGoal = prefs[StepRepository.DAILY_GOAL_KEY] ?: 8000

        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(StepsWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[widgetStepsKey] = currentSteps
                    this[widgetGoalKey] = dailyGoal
                }
            }
            StepsWidget().update(context, glanceId)
        }
    }
}