package com.SzpontCompany.check.widgets

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.SzpontCompany.check.data.habit.Habit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WidgetDataUpdater {
    suspend fun updateStepsWidgetData(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val habits = db.collection("users").document(uid)
            .collection("habits")
            .get()
            .await()

        val stepsHabit = habits.documents
            .mapNotNull { it.toObject(Habit::class.java) }
            .firstOrNull { it.icon == "🚶" } ?: return

        val today = java.time.LocalDate.now().toString()
        val currentSteps = if (stepsHabit.completedDates.contains(today)) stepsHabit.dailyGoal else 0

        // Aktualizuj stan Glance DataStore
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(StepsWidget::class.java)
        glanceIds.forEach { glanceId ->
            updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    this[widgetStepsKey] = currentSteps
                    this[widgetGoalKey] = stepsHabit.dailyGoal
                }
            }
            StepsWidget().update(context, glanceId)
        }
    }
}