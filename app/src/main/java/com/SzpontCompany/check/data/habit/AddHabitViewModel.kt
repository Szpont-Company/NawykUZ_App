package com.SzpontCompany.check.ui.addhabit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.data.habit.HabitRepository
import kotlinx.coroutines.launch

class AddHabitViewModel : ViewModel() {
    private val repository = HabitRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun saveHabit(habit: Habit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Wracamy do bezpośredniego zapisu z Twojego pierwotnego kodu!
                repository.addHabit(habit)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
            } finally {
                isLoading = false
            }
        }
    }
}