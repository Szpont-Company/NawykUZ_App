package com.SzpontCompany.check.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.data.user.UserRepository
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.data.habit.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val habits: List<Habit> = emptyList(),
    val errorMessage: String? = null
)

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository.getInstance(application.applicationContext)
    private val habitRepo = HabitRepository()

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        userRepo.startUserObservation()
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            launch {
                habitRepo.getUserHabits().collect { fetchedHabits ->
                    _uiState.value = _uiState.value.copy(habits = fetchedHabits)
                }
            }

            launch {
                userRepo.userFlow.collect { user ->
                    if (user != null) {
                        _uiState.value = _uiState.value.copy(user = user, isLoading = false)
                    }
                }
            }
        }
    }

    fun toggleHabitCompletion(habitId: String, isDone: Boolean) {
        val todayString = LocalDate.now().toString()
        val currentState = _uiState.value
        val currentHabits = currentState.habits
        val currentUser = currentState.user ?: return

        val habit = currentHabits.find { it.id == habitId } ?: return

        val wasPerfectDay = currentHabits.isNotEmpty() && currentHabits.all {
            it.completedDates.contains(todayString)
        }

        val newDates = if (isDone) (habit.completedDates + todayString).distinct()
        else habit.completedDates.filter { it != todayString }

        val newHabitStreak = if (isDone) habit.streak + 1 else maxOf(0, habit.streak - 1)

        val updatedHabits = currentHabits.map {
            if (it.id == habitId) it.copy(completedDates = newDates, streak = newHabitStreak) else it
        }

        val isPerfectDayNow = updatedHabits.isNotEmpty() && updatedHabits.all {
            it.completedDates.contains(todayString)
        }

        var newGlobalStreak = currentUser.currentStreak
        if (isPerfectDayNow && !wasPerfectDay) {
            newGlobalStreak += 1
        } else if (!isPerfectDayNow && wasPerfectDay) {
            newGlobalStreak = maxOf(0, newGlobalStreak - 1)
        }

        val newBestStreak = maxOf(currentUser.bestStreak, newGlobalStreak)

        _uiState.value = currentState.copy(
            habits = updatedHabits,
            user = currentUser.copy(currentStreak = newGlobalStreak, bestStreak = newBestStreak)
        )

        viewModelScope.launch {
            try {
                habitRepo.updateHabitCompletionAndStreak(habitId, newDates, newHabitStreak)

                if (newGlobalStreak != currentUser.currentStreak) {
                    userRepo.updateUserStreaks(currentUser.uid, newGlobalStreak, newBestStreak)
                }

                if (isDone) {
                    habitRepo.earnCoinsCloud("habit_done")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Błąd zapisu: ${e.message}")
            }
        }
    }
}