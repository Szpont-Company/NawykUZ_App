package com.SzpontCompany.check.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.SzpontCompany.check.data.steps.StepRepository
import com.SzpontCompany.check.data.steps.dataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class WidgetDataUpdater {
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