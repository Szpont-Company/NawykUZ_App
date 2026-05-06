package com.SzpontCompany.check.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.habit.HabitRepository
import com.SzpontCompany.check.data.social.ChallengeRepository
import com.SzpontCompany.check.data.social.FriendRepository
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository.getInstance(application.applicationContext)
    private val habitRepository = HabitRepository()
    private val friendRepository = FriendRepository()
    private val challengeRepository = ChallengeRepository(FirebaseFirestore.getInstance())

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val user: StateFlow<User?> = userRepository.userFlow

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
        observeStats()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                userRepository.getUser()

                userRepository.userFlow.collect { currentUser ->
                    if (currentUser != null) {
                        _uiState.update { it.copy(isLoading = false, user = currentUser) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Nie udało się pobrać profilu: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun observeStats() {
        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            habitRepository.getUserHabits().collect { habits ->
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
                    ((totalActual.toFloat() / totalPossible.toFloat()) * 100).toInt().coerceAtMost(100)
                } else {
                    0
                }

                _uiState.update { it.copy(
                    habitsCount = habits.size,
                    effectiveness = effectiveness
                )}
            }
        }

        viewModelScope.launch {
            userRepository.getUserCoins().collect { coins ->
                _uiState.update { it.copy(coins = coins) }
            }
        }

        viewModelScope.launch {
            challengeRepository.getActiveBattlesForUser(currentUserId).collect { battles ->
                val wonBattles = battles.count { it.winnerId == currentUserId }
                _uiState.update { it.copy(battlesWon = wonBattles) }
            }
        }

        viewModelScope.launch {
            friendRepository.getMyFriends().collect { friends ->
                _uiState.update { it.copy(friendsCount = friends.size) }
            }
        }
    }

    fun saveProfile(newName: String, newNickname: String, newAvatar: String, newBgColor: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                userRepository.updateProfileViaFunctions(newName, newNickname, newAvatar, newBgColor)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Błąd zapisu: ${e.localizedMessage}") }
            }
        }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null,
    val habitsCount: Int = 0,
    val coins: Int = 0,
    val battlesWon: Int = 0,
    val effectiveness: Int = 0,
    val friendsCount: Int = 0
)