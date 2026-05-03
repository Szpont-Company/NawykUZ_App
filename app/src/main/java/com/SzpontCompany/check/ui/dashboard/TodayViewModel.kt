package com.SzpontCompany.check.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.data.user.UserRepository
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.data.habit.HabitRepository
import com.SzpontCompany.check.data.social.ChallengeRepository
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
    private val challengeRepository = ChallengeRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance())

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        userRepo.startUserObservation()
        loadUser()
        observeHabits()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepo.userFlow.collect { user ->
                val progress = calculateWeeklyProgress(_uiState.value.habits)
                val userWithProgress = user?.copy(weeklyProgress = progress)
                _uiState.value = _uiState.value.copy(user = userWithProgress, isLoading = false)
            }
        }
    }

    private fun observeHabits() {
        viewModelScope.launch {
            habitRepo.getUserHabits().collect { fetchedHabits ->
                val progress = calculateWeeklyProgress(fetchedHabits)
                val userWithProgress = _uiState.value.user?.copy(weeklyProgress = progress)
                _uiState.value = _uiState.value.copy(habits = fetchedHabits, user = userWithProgress)
            }
        }
    }

    fun completeAction(action: String) {
        viewModelScope.launch {
            habitRepo.earnCoinsCloud(action).onFailure {
                _uiState.value = _uiState.value.copy(errorMessage = "Nie udało się dodać monet: ${it.message}")
            }
        }
    }

    private fun calculateWeeklyProgress(habits: List<Habit>): List<Float> {
        val today = LocalDate.now()
        val totalHabits = habits.size

        if (totalHabits == 0) return listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)

        return (6 downTo 0).map { daysAgo ->
            val dateString = today.minusDays(daysAgo.toLong()).toString()
            val completedCount = habits.count { it.completedDates.contains(dateString) }
            completedCount.toFloat() / totalHabits.toFloat()
        }
    }

    fun toggleHabitCompletion(habitId: String, isDone: Boolean) {
        val todayString = LocalDate.now().toString()
        val yesterdayString = LocalDate.now().minusDays(1).toString()

        val currentHabits = _uiState.value.habits
        val habit = currentHabits.find { it.id == habitId } ?: return

        val newDates = if (isDone) {
            (habit.completedDates + todayString).distinct()
        } else {
            habit.completedDates.filter { it != todayString }
        }
        val newHabitStreak = if (isDone) habit.streak + 1 else maxOf(0, habit.streak - 1)

        val updatedHabits = currentHabits.map {
            if (it.id == habitId) it.copy(completedDates = newDates, streak = newHabitStreak) else it
        }

        val allDoneToday = updatedHabits.isNotEmpty() && updatedHabits.all { it.completedDates.contains(todayString) }

        val currentUser = _uiState.value.user
        var updatedUser = currentUser

        if (currentUser != null) {
            var newGlobalStreak = currentUser.currentStreak
            var newLastDate = currentUser.lastGlobalStreakDate

            if (allDoneToday) {
                if (currentUser.lastGlobalStreakDate != todayString) {
                    newGlobalStreak += 1
                    newLastDate = todayString
                }
            } else {
                if (currentUser.lastGlobalStreakDate == todayString) {
                    newGlobalStreak = maxOf(0, newGlobalStreak - 1)
                    newLastDate = yesterdayString
                }
            }

            val newBestStreak = maxOf(currentUser.bestStreak, newGlobalStreak)
            val newProgress = calculateWeeklyProgress(updatedHabits)

            updatedUser = currentUser.copy(
                currentStreak = newGlobalStreak,
                bestStreak = newBestStreak,
                lastGlobalStreakDate = newLastDate,
                weeklyProgress = newProgress
            )
        }

        _uiState.value = _uiState.value.copy(
            habits = updatedHabits,
            user = updatedUser
        )

        viewModelScope.launch {
            try {
                if (habit.battleId != null && currentUser != null) {
                    challengeRepository.updateBattleProgress(
                        battleId = habit.battleId,
                        currentUserId = currentUser.uid,
                        isDone = isDone,
                        habitId = habitId,
                        newDates = newDates,
                        newHabitStreak = newHabitStreak
                    )
                } else {
                    habitRepo.updateHabitCompletionAndStreak(habitId, newDates, newHabitStreak)
                }

                if (updatedUser != null && currentUser != updatedUser) {
                    userRepo.updateUserStreaks(
                        updatedUser.uid,
                        updatedUser.currentStreak,
                        updatedUser.bestStreak,
                        updatedUser.lastGlobalStreakDate
                    )
                }

                if (isDone) completeAction("habit_done")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Błąd zapisu: ${e.message}")
            }
        }
    }

    fun updateHabitDailyNote(habitId: String, dateString: String, newNote: String) {
        val currentHabits = _uiState.value.habits
        val updatedHabits = currentHabits.map { habit ->
            if (habit.id == habitId) {
                val newNotesMap = habit.dailyNotes.toMutableMap()
                if (newNote.isBlank()) {
                    newNotesMap.remove(dateString)
                } else {
                    newNotesMap[dateString] = newNote
                }
                habit.copy(dailyNotes = newNotesMap)
            } else {
                habit
            }
        }

        _uiState.value = _uiState.value.copy(habits = updatedHabits)

        viewModelScope.launch {
            try {
                habitRepo.updateHabitDailyNote(habitId, dateString, newNote)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Błąd zapisu notatki: ${e.message}")
            }
        }
    }
}