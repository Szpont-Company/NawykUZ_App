package com.SzpontCompany.check.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.habit.HabitRepository
import com.SzpontCompany.check.data.steps.StepRepository
import com.SzpontCompany.check.data.user.UserRepository
import com.SzpontCompany.check.widgets.WidgetDataUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel dla ustawiania celu dziennego kroków.
 *
 * Zarządza:
 * - Zapisywaniem nowego celu kroków do bazy danych
 * - Aktualizacją celu w nawyku "Kroki"
 * - Aktualizacją widżetu kroków
 * - Walidacją wartości (1000 - 100000 kroków)
 * - Obsługą błędów podczas zapisu
 *
 * Synchronizuje cel z:
 * - UserRepository (profil użytkownika)
 * - HabitRepository (nawyk kroków)
 * - StepRepository (lokalne preferencje)
 * - Widżetami (aktualizacja wyświetlanego celu)
 *
 * @since 1.0
 */
class StepGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val userRepo = UserRepository.getInstance(context)
    private val habitRepo = HabitRepository()

    var isSaving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    val userHabitsFlow = habitRepo.getUserHabits()

    fun saveStepGoal(goal: Int) = viewModelScope.launch {
        isSaving = true
        error = null
        _saveSuccess.value = false

        try {
            val safeGoal = goal.coerceIn(1000, 100000)

            userRepo.updateStepGoal(safeGoal)
            habitRepo.updateStepsGoal(safeGoal, context)
            StepRepository(context).updateDailyGoal(safeGoal)
            WidgetDataUpdater().updateStepsWidgetData(context)

            _saveSuccess.value = true
        } catch (e: Exception) {
            error = e.message
        } finally {
            isSaving = false
        }
    }
}