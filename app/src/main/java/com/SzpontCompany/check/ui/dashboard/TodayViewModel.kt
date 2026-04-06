package com.SzpontCompany.check.ui.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.data.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TopSectionUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null
)

class TodayViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(
        FirebaseAuth.getInstance(),
        FirebaseFirestore.getInstance(),
        com.SzpontCompany.check.data.user.UserCache(application.applicationContext)
    )
    private val _uiState = MutableStateFlow(TopSectionUiState())
    val uiState: StateFlow<TopSectionUiState> = _uiState.asStateFlow()

    init {
        observeUser()
        loadUser()
    }

    fun observeUser() {
        viewModelScope.launch {
            repo.userFlow.collect { user ->
                _uiState.value = TopSectionUiState(isLoading = false, user = user)
            }
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            try {
                _uiState.value = TopSectionUiState(isLoading = true)
                repo.getUser()
            } catch (e: Exception) {
                _uiState.value = TopSectionUiState(isLoading = false, errorMessage = "Failed to load user: ${e.message}")
            }
        }
    }
}