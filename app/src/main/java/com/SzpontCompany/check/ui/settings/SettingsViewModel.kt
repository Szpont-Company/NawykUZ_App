package com.SzpontCompany.check.ui.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.settings.SettingsRepository
import com.SzpontCompany.check.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val userRepository : UserRepository
) : ViewModel() {

    val themeState = repository.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Auto"
    )

    val accentColorState = repository.accentColorFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = "Mint"
    )

    val showLocationState = repository.showLocationFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val stepGoalState = repository.stepGoalFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 8000
    )

    private val _notificationSubtitleState = MutableStateFlow(Triple(false, "EVERYDAY", "20:00"))
    val notificationSubtitleState = _notificationSubtitleState.asStateFlow()

    init {
        refreshNotificationSettings()
    }

    fun updateShowLocation(show: Boolean) {
        viewModelScope.launch { repository.saveShowLocation(show) }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("showLocation", show)
                .addOnFailureListener { e ->
                    Log.e("SettingsViewModel", "Błąd aktualizacji prywatności lokalizacji", e)
                }
        }
    }

    val languageState = repository.languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Polski"
    )

    val battleNotificationsState = repository.battleNotificationsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun updateTheme(newTheme: String) {
        viewModelScope.launch { repository.saveTheme(newTheme) }
    }

    fun updateAccentColor(newColorName: String) {
        viewModelScope.launch { repository.saveAccentColor(newColorName) }
    }

    fun updateLanguage(newLanguage: String) {
        viewModelScope.launch {
            repository.saveLanguage(newLanguage)

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val langCode = when (newLanguage) {
                    "Polski" -> "pl"
                    "English" -> "en"
                    else -> "en"
                }

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("language", langCode)
            }
        }
    }

    fun updateBattleNotifications(isEnabled: Boolean) {
        viewModelScope.launch {
            repository.saveBattleNotifications(isEnabled)

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("battleNotifications", isEnabled)
            }
        }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.deleteUserAccount()
                onComplete(true)
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Błąd usuwania konta", e)
                onComplete(false)
            }
        }
    }

    fun refreshNotificationSettings() {
        _notificationSubtitleState.value = repository.getNotificationPrefs()
    }

    fun updateStepGoal(newGoal: Int) {
        viewModelScope.launch { repository.saveStepGoal(newGoal) }
        //TODO: update step goal in Firestore if needed
//         val uid = FirebaseAuth.getInstance().currentUser?.uid
//         if (uid != null) {
//             FirebaseFirestore.getInstance().collection("users").document(uid)
//                 .update("stepGoal", newGoal)
//         }
    }
}

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            val settingsRepository = SettingsRepository(context)
            val userRepository = UserRepository.getInstance(context)
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}