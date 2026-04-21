package com.SzpontCompany.check.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TopSectionUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null
)

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository.getInstance(application.applicationContext)
    private val _uiState = MutableStateFlow(TopSectionUiState())
    val uiState: StateFlow<TopSectionUiState> = _uiState.asStateFlow()

    init {
        observeUser()
        loadUser()
    }

    fun observeUser() {
        viewModelScope.launch {
            repo.userFlow.collect { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = user
                )
            }
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                repo.getUser()

                _uiState.value = _uiState.value.copy(isLoading = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load user: ${e.message}"
                )
            }
        }
    }
}