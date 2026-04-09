package com.SzpontCompany.check.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository.getInstance(application.applicationContext)

    val user: StateFlow<User?> = userRepository.userFlow

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                userRepository.getUser()

                userRepository.userFlow.collect { currentUser ->
                    if (currentUser != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = currentUser
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Nie udało się pobrać profilu: ${e.localizedMessage}"
                )
            }
        }
    }

    fun saveProfile(newName: String, newNickname: String, newAvatar: String, newBgColor: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                userRepository.updateProfileViaFunctions(newName, newNickname, newAvatar, newBgColor)

                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Błąd zapisu: ${e.localizedMessage}"
                )
            }
        }
    }


}

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null
)
