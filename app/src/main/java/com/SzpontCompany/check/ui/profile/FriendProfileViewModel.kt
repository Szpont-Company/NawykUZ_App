package com.SzpontCompany.check.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.data.user.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FriendProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val habits: List<Habit> = emptyList(),
    val habitsCount: Int = 0,
    val coins: Int = 0,
    val battlesWon: Int = 0,
    val effectiveness: Int = 0,
    val friendsCount: Int = 0,
    val error: String? = null
)

class FriendProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow(FriendProfileUiState())
    val uiState: StateFlow<FriendProfileUiState> = _uiState.asStateFlow()

    fun loadFriendProfile(friendUid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val document = firestore.collection("users").document(friendUid).get().await()

                if (!document.exists()) {
                    _uiState.update { it.copy(isLoading = false, error = "Użytkownik nie istnieje") }
                    return@launch
                }

                val friendUser = User(
                    uid = friendUid,
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    nickname = document.getString("nickname") ?: "",
                    isAdmin = document.getBoolean("isAdmin") ?: false,
                    avatarEmoji = document.getString("avatarEmoji") ?: "",
                    bgColor = document.getString("bgColor") ?: "Mint",
                    currentStreak = document.getLong("currentStreak")?.toInt() ?: 0,
                    bestStreak = document.getLong("bestStreak")?.toInt() ?: 0,
                    lastGlobalStreakDate = document.getString("lastGlobalStreakDate") ?: "",
                    xp = document.getLong("xp")?.toInt() ?: 0,
                    level = document.getLong("level")?.toInt() ?: 1,
                    weeklyProgress = (document.get("weeklyProgress") as? List<*>)?.map { (it as? Number)?.toFloat() ?: 0f }
                        ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
                )

                val habitsSnapshot = firestore.collection("users").document(friendUid).collection("habits").get().await()
                val habits = habitsSnapshot.documents.map { doc ->
                    Habit(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        icon = doc.getString("icon") ?: "🎯",
                        colorName = doc.getString("colorName") ?: "Mint",
                        frequency = doc.getString("frequency") ?: "",
                        selectedDays = (doc.get("selectedDays") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        timesPerWeek = doc.getLong("timesPerWeek")?.toInt() ?: 0,
                        dailyGoal = doc.getLong("dailyGoal")?.toInt() ?: 0,
                        unit = doc.getString("unit") ?: "",
                        dailyReminder = doc.getBoolean("dailyReminder") ?: false,
                        reminderTime = doc.getString("reminderTime") ?: "",
                        eveningReminder = doc.getBoolean("eveningReminder") ?: false,
                        eveningTime = doc.getString("eveningTime") ?: "",
                        difficulty = doc.getString("difficulty") ?: "",
                        isActive = doc.getBoolean("isActive") ?: true,
                        battleId = doc.getString("battleId"),
                        opponentName = doc.getString("opponentName"),
                        streak = doc.getLong("streak")?.toInt() ?: 0,
                        monthlyCompletionRate = doc.getLong("monthlyCompletionRate")?.toInt() ?: 0,
                        completedDates = (doc.get("completedDates") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        createdAt = doc.getDate("createdAt")
                    )
                }

                var totalPossible = 0
                var totalActual = 0
                val todayMs = System.currentTimeMillis()

                habits.forEach { habit ->
                    val actual = habit.completedDates.size
                    totalActual += actual
                    val daysSinceCreation = habit.createdAt?.let {
                        ((todayMs - it.time) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                    } ?: actual.coerceAtLeast(1)
                    totalPossible += daysSinceCreation
                }

                val effectiveness = if (totalPossible > 0) {
                    ((totalActual.toFloat() / totalPossible) * 100).toInt().coerceAtMost(100)
                } else 0

                val wonBattlesSnapshot = firestore.collection("battles").whereEqualTo("winnerId", friendUid).get().await()
                val friendsSnapshot = firestore.collection("users").document(friendUid).collection("friends").get().await()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = friendUser,
                        habits = habits,
                        habitsCount = habits.size,
                        coins = document.getLong("coins")?.toInt() ?: 0,
                        battlesWon = wonBattlesSnapshot.size(),
                        effectiveness = effectiveness,
                        friendsCount = friendsSnapshot.size()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}