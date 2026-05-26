package com.SzpontCompany.check.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.data.habit.HabitRepository
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class StatsUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val habits: List<Habit> = emptyList(),
    val filteredHabits: List<Habit> = emptyList(),
    val coins: Int = 0,
    val totalCompletedHabits: Int = 0,
    val overallSuccessRate: Int = 0,
    val selectedTimeRangeIndex: Int = 0,
    val selectedHabitId: String? = null,

    val weeklyChartData: List<Pair<Int, Float>> = emptyList(),
    val weeklyAveragePercentage: Int = 0
)

class StatsViewModel(
    private val userRepository: UserRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val coinsFlow = userRepository.getUserCoins()

    private val _selectedTimeRange = MutableStateFlow(0)
    private val _selectedHabitId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StatsUiState> = combine(
        userRepository.userFlow,
        coinsFlow,
        habitRepository.getUserHabits(),
        _selectedTimeRange,
        _selectedHabitId
    ) { user, coins, allHabits, timeRangeIndex, selectedHabitId ->

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val cutoffDate = Calendar.getInstance().apply {
            when (timeRangeIndex) {
                0 -> add(Calendar.DAY_OF_YEAR, -7)
                1 -> add(Calendar.DAY_OF_YEAR, -30)
                2 -> add(Calendar.DAY_OF_YEAR, -90)
            }
        }.time
        val cutoffString = dateFormat.format(cutoffDate)

        val filteredHabits = if (selectedHabitId == null) {
            allHabits
        } else {
            allHabits.filter { it.id == selectedHabitId }
        }

        var totalPossible = 0
        var totalActual = 0
        val todayMs = System.currentTimeMillis()

        filteredHabits.forEach { habit ->
            val completedInWindow = habit.completedDates.count { dateStr ->
                if (timeRangeIndex == 3) true else dateStr >= cutoffString
            }
            totalActual += completedInWindow

            val daysSinceCreation = habit.createdAt?.let {
                val diff = todayMs - it.time
                (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            } ?: habit.completedDates.size.coerceAtLeast(1)

            val windowDays = when(timeRangeIndex) {
                0 -> 7
                1 -> 30
                2 -> 90
                else -> daysSinceCreation
            }

            val possibleForHabit = minOf(windowDays, daysSinceCreation)
            totalPossible += possibleForHabit
        }

        val successRate = if (totalPossible > 0) {
            ((totalActual.toFloat() / totalPossible.toFloat()) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }

        val chartData = mutableListOf<Pair<Int, Float>>()
        var totalSumForAverage = 0f

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(cal.time)

            val dayIndex = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }

            val completedCount = filteredHabits.count { it.completedDates.contains(dateStr) }
            val activeHabitsCount = filteredHabits.size

            val dailyValue = if (activeHabitsCount > 0) {
                (completedCount.toFloat() / activeHabitsCount).coerceIn(0f, 1f)
            } else 0f

            totalSumForAverage += dailyValue
            chartData.add(Pair(dayIndex, dailyValue))
        }

        val weeklyAveragePercentage = if (chartData.isNotEmpty()) {
            ((totalSumForAverage / 7f) * 100).toInt()
        } else 0

        StatsUiState(
            isLoading = false,
            user = user,
            habits = allHabits,
            filteredHabits = filteredHabits,
            coins = coins,
            totalCompletedHabits = totalActual,
            overallSuccessRate = successRate,
            selectedTimeRangeIndex = timeRangeIndex,
            selectedHabitId = selectedHabitId,
            weeklyChartData = chartData,
            weeklyAveragePercentage = weeklyAveragePercentage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(isLoading = true)
    )

    fun setTimeRange(index: Int) {
        _selectedTimeRange.value = index
    }

    fun setHabitFilter(habitId: String?) {
        _selectedHabitId.value = habitId
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                return StatsViewModel(
                    userRepository = UserRepository.getInstance(application),
                    habitRepository = HabitRepository()
                ) as T
            }
        }
    }
}