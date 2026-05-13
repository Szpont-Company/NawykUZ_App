package com.SzpontCompany.check.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.badges.Badge
import com.SzpontCompany.check.data.badges.BadgeProvider
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.data.user.UserRepository
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.data.habit.HabitRepository
import com.SzpontCompany.check.data.social.ChallengeRepository
import com.SzpontCompany.check.widgets.updateHabitWidgetData
import com.SzpontCompany.check.data.steps.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val habits: List<Habit> = emptyList(),
    val errorMessage: String? = null,
    val todaySteps: Int = 0
)

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo = UserRepository.getInstance(application.applicationContext)
    val user = userRepo.userFlow
    private val habitRepo = HabitRepository()
    private val challengeRepository = ChallengeRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance())

    private val stepRepository = StepRepository(application.applicationContext)

    private val _badgeToUnlock = MutableStateFlow<Badge?>(null)
    val badgeToUnlock: StateFlow<Badge?> = _badgeToUnlock.asStateFlow()
    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        userRepo.startUserObservation()
        loadUser()
        observeHabits()
        observeSteps()
    }

    private fun observeSteps() {
        viewModelScope.launch {
            stepRepository.todayStepsFlow.collect { steps ->
                _uiState.value = _uiState.value.copy(todaySteps = steps)
            }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepo.userFlow.collect { user ->
                if (user != null) {
                    val todayString = LocalDate.now().toString()
                    val yesterdayString = LocalDate.now().minusDays(1).toString()
                    var activeUser = user

                    if (user.currentStreak > 0 && user.lastGlobalStreakDate != todayString && user.lastGlobalStreakDate != yesterdayString) {
                        activeUser = user.copy(currentStreak = 0)
                        viewModelScope.launch {
                            try {
                                userRepo.updateUserStreaks(
                                    uid = user.uid,
                                    currentStreak = 0,
                                    bestStreak = user.bestStreak,
                                    lastGlobalStreakDate = user.lastGlobalStreakDate
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    try {
                        val c = getApplication<Application>().applicationContext
                        updateHabitWidgetData(c, activeUser.currentStreak, activeUser.bestStreak)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val progress = calculateWeeklyProgress(_uiState.value.habits)
                    val userWithProgress = activeUser.copy(weeklyProgress = progress)
                    _uiState.value = _uiState.value.copy(user = userWithProgress, isLoading = false)
                    viewModelScope.launch {
                        try {
                            val battles = challengeRepository.getWonBattlesCount(userWithProgress.uid).first()
                            checkForNewBadges(userWithProgress, battles)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(user = null, isLoading = false)
                }
            }
        }
    }

    private fun observeHabits() {
        viewModelScope.launch {
            habitRepo.getUserHabits().collect { fetchedHabits ->
                val todayString = LocalDate.now().toString()
                val yesterdayString = LocalDate.now().minusDays(1).toString()

                val processedHabits = fetchedHabits.map { habit ->
                    if (habit.streak > 0 && !habit.completedDates.contains(todayString) && !habit.completedDates.contains(yesterdayString)) {
                        viewModelScope.launch {
                            try {
                                habitRepo.updateHabitCompletionAndStreak(habit.id, habit.completedDates, 0)
                            } catch (e: Exception) {}
                        }
                        habit.copy(streak = 0)
                    } else habit
                }

                val progress = calculateWeeklyProgress(processedHabits)
                val userWithProgress = _uiState.value.user?.copy(weeklyProgress = progress)
                _uiState.value = _uiState.value.copy(habits = processedHabits, user = userWithProgress)
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
        if (habits.isEmpty()) return listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        return (6 downTo 0).map { daysAgo ->
            val dateString = today.minusDays(daysAgo.toLong()).toString()
            habits.count { it.completedDates.contains(dateString) }.toFloat() / habits.size.toFloat()
        }
    }

    fun toggleHabitCompletion(habitId: String, isDone: Boolean) {
        val todayString = LocalDate.now().toString()
        val yesterdayString = LocalDate.now().minusDays(1).toString()
        val habit = _uiState.value.habits.find { it.id == habitId } ?: return

        val wasDoneAlready = habit.completedDates.contains(todayString)

        val newDates = if (isDone) (habit.completedDates + todayString).distinct() else habit.completedDates.filter { it != todayString }
        val newHabitStreak = if (isDone) habit.streak + 1 else maxOf(0, habit.streak - 1)

        val updatedHabits = _uiState.value.habits.map { if (it.id == habitId) it.copy(completedDates = newDates, streak = newHabitStreak) else it }
        val allDoneToday = updatedHabits.isNotEmpty() && updatedHabits.all { it.completedDates.contains(todayString) }
        val currentUser = _uiState.value.user
        var updatedUser = currentUser

        val multiplier = currentUser?.getRewardMultiplier(todayString) ?: 1.0f
        val xpToAward = (15 * multiplier).toInt()
        val coinsToAward = (5 * multiplier).toInt()

        if (currentUser != null) {
            updatedUser = currentUser.calculateNewStreak(allDoneToday, todayString, yesterdayString).copy(weeklyProgress = calculateWeeklyProgress(updatedHabits))
        }

        _uiState.value = _uiState.value.copy(habits = updatedHabits, user = updatedUser)

        viewModelScope.launch {
            try {
                if (habit.battleId != null && currentUser != null) {
                    challengeRepository.updateBattleProgress(habit.battleId, currentUser.uid, isDone, habitId, newDates, newHabitStreak)
                } else habitRepo.updateHabitCompletionAndStreak(habitId, newDates, newHabitStreak)

                if (updatedUser != null && currentUser != updatedUser) {
                    userRepo.updateUserStreaks(updatedUser.uid, updatedUser.currentStreak, updatedUser.bestStreak, updatedUser.lastGlobalStreakDate)
                    userRepo.updateUserStreaks(
                        updatedUser.uid,
                        updatedUser.currentStreak,
                        updatedUser.bestStreak,
                        updatedUser.lastGlobalStreakDate
                    )

                    val context = getApplication<Application>().applicationContext
                    updateHabitWidgetData(context, updatedUser.currentStreak, updatedUser.bestStreak)
                }

                if (isDone && !wasDoneAlready) {
                    currentUser?.let { userRepo.addReward(it.uid, xpToAward, coinsToAward) }
                    completeAction("habit_done")
                } else if (!isDone && wasDoneAlready) {
                    currentUser?.let { userRepo.addReward(it.uid, -xpToAward, -coinsToAward) }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Błąd: ${e.message}")
            }
        }
    }

    fun updateHabitDailyNote(habitId: String, dateString: String, newNote: String) {
        val updatedHabits = _uiState.value.habits.map { habit ->
            if (habit.id == habitId) {
                val newNotes = habit.dailyNotes.toMutableMap()
                if (newNote.isBlank()) newNotes.remove(dateString) else newNotes[dateString] = newNote
                habit.copy(dailyNotes = newNotes)
            } else habit
        }
        _uiState.value = _uiState.value.copy(habits = updatedHabits)
        viewModelScope.launch { habitRepo.updateHabitDailyNote(habitId, dateString, newNote) }
    }

    fun checkForNewBadges(user: User, battlesWon: Int) {
        val earnedBadges = BadgeProvider.evaluateBadges(
            bestStreak = user.bestStreak,
            battlesWon = battlesWon
        ).filter { it.isUnlocked }

        val newBadge = earnedBadges.firstOrNull { badge ->
            !user.unlockedBadges.contains(badge.id)
        }

        if (newBadge != null) {
            _badgeToUnlock.value = newBadge
        }
    }

    fun claimBadgeReward(badge: Badge) {
        val currentUser = userRepo.userFlow.value ?: return
        viewModelScope.launch {
            _badgeToUnlock.value = null

            try {
                userRepo.claimBadge(currentUser.uid, badge.id)
                habitRepo.earnCoinsCloud("badge_unlocked", 50)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}