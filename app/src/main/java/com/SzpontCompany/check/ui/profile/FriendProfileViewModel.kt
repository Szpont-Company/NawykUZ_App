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
                val userDoc = firestore.collection("users").document(friendUid).get().await()
                val user = userDoc.toObject(User::class.java)

                val habitsSnapshot = firestore.collection("users").document(friendUid).collection("habits").get().await()
                val habits = habitsSnapshot.documents.mapNotNull {
                    it.toObject(Habit::class.java)?.copy(id = it.id)
                }

                var totalPossible = 0
                var totalActual = 0
                val todayMs = System.currentTimeMillis()

                habits.forEach { habit ->
                    val actual = habit.completedDates.size
                    totalActual += actual

                    val daysSinceCreation = habit.createdAt?.let {
                        val diff = todayMs - it.time
                        (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                    } ?: actual.coerceAtLeast(1)

                    totalPossible += daysSinceCreation
                }

                val effectiveness = if (totalPossible > 0) {
                    ((totalActual.toFloat() / totalPossible) * 100).toInt().coerceAtMost(100)
                } else 0

                val coins = userDoc.getLong("coins")?.toInt() ?: 0

                val battlesSnapshot = firestore.collection("battles").whereEqualTo("winnerId", friendUid).get().await()
                val battlesWon = battlesSnapshot.size()

                val friendsSnapshot = firestore.collection("users").document(friendUid).collection("friends").get().await()
                val friendsCount = friendsSnapshot.size()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        habits = habits,
                        habitsCount = habits.size,
                        coins = coins,
                        battlesWon = battlesWon,
                        effectiveness = effectiveness,
                        friendsCount = friendsCount
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }
}