package com.SzpontCompany.check.ui.rewards

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.coins.CoinsRepository
import com.SzpontCompany.check.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RewardsViewModel(
    private val coinsRepository: CoinsRepository = CoinsRepository(),
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _uiMsg = MutableStateFlow<String?>(null)
    val uiMsg: StateFlow<String?> = _uiMsg

    val currentCoins: StateFlow<Int> = userRepository.getUserCoins()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun onAdWatched(rewardAmount: Int) {
        Log.d("ADS", "User earned reward: $rewardAmount points")
        viewModelScope.launch {
            _isLoading.value = true
            val result = coinsRepository.earnCoins("watch_ad")
            _isLoading.value = false

            result.onSuccess { added ->
                _uiMsg.value = "You earned $added coins!"
                Log.d("RewardsViewModel", "Successfully earned coins: $added")
            }.onFailure { e ->
                Log.e("RewardsViewModel", "Failed to earn coins: ${e.message}")
                _uiMsg.value = "Failed to earn coins. Please try again."
            }
        }
    }

    fun clearMessage() {
        _uiMsg.value = null
    }
}

class RewardsViewmodelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RewardsViewModel::class.java)) {

            val userRepository = UserRepository.getInstance(context)

            @Suppress("UNCHECKED_CAST")
            return RewardsViewModel(
                coinsRepository = CoinsRepository(),
                userRepository = userRepository
            ) as T

        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}