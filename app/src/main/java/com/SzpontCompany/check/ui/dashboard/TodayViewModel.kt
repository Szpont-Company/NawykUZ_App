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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val auth by lazy { FirebaseAuth.getInstance()}
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val _userData = MutableStateFlow<String?>(null)
    val userData: StateFlow<String?> = _userData

    init {
        fetchUserData()
    }

    fun fetchUserData() {
        if (_userData.value != null) return
        val user = auth.currentUser ?: return

        val uid = user.uid

        viewModelScope.launch {
            try {
                Log.e("TodayViewModel", "Fetching data for UID: $uid")
                val document = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                if(document.exists()) {
                    val name = document.getString("name") ?: "N/A"
                    _userData.value = name
                }
            } catch (e: Exception) {
                _userData.value = "Error fetching data: ${e.message}"
                Log.e("TodayViewModel", "Error fetching user data", e)
            }
        }
    }
}