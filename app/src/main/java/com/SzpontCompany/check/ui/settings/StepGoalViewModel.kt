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

class StepGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val userRepo = UserRepository.getInstance(context)
    private val habitRepo = HabitRepository()

    var isSaving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    // Status pomyślnego zapisu, aby cofnąć użytkownika na poprzedni ekran
    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    // Pozwala na pobranie obecnego celu z bazy nawyków
    val userHabitsFlow = habitRepo.getUserHabits()

    fun saveStepGoal(goal: Int) = viewModelScope.launch {
        isSaving = true
        error = null
        _saveSuccess.value = false

        try {
            val safeGoal = goal.coerceIn(1000, 100000)

            // Aktualizacja wszędzie tam, gdzie aplikacja polega na celu kroków
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