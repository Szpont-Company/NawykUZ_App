package com.SzpontCompany.check.ui.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class TopSectionUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val errorMessage: String? = null
)

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val auth by lazy { FirebaseAuth.getInstance()}
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val _uiState = MutableStateFlow(TopSectionUiState())
    val uiState: StateFlow<TopSectionUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    fun loadUserData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "No authenticated user found."
            )
            return
        }

        val uid = user.uid

        viewModelScope.launch {
            try {
                Log.e("TodayViewModel", "Fetching data for UID: $uid")
                val document = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                if (document.exists()) {
                    val fetchedUser = User(
                        uid = uid,
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        nickname = document.getString("nickname") ?: "",
                        isAdmin = document.getBoolean("isAdmin") ?: false
                    )
                    _uiState.value = TopSectionUiState(
                        isLoading = false,
                        user = fetchedUser
                    )
                }
            } catch (e: Exception) {
                _uiState.value = TopSectionUiState(
                    isLoading = false,
                    errorMessage = "Error fetching data: ${e.message}"
                )
                Log.e("TodayViewModel", "Error fetching user data", e)
            }
        }
    }
}