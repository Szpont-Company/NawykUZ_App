package com.SzpontCompany.check.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.habit.HabitRepository
import com.SzpontCompany.check.data.user.UserRepository
import kotlinx.coroutines.launch

class StepGoalViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepo = UserRepository.getInstance(application.applicationContext)
    private val habitRepo = HabitRepository()

    var isSaving by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun saveStepGoal(goal: Int) = viewModelScope.launch {
        isSaving = true
        error = null

        try {
            val safeGoal = goal.coerceIn(1000, 100000)

            userRepo.updateStepGoal(safeGoal)
            habitRepo.updateStepsGoal(safeGoal, application.applicationContext)
        } catch (e: Exception) {
            error = e.message
        } finally {
            isSaving = false
        }
    }
}