package com.SzpontCompany.check.data.habit

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HabitRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addHabit(habit: Habit) {
        val uid = auth.currentUser?.uid ?: throw Exception("Brak zalogowanego użytkownika")

        val docRef = firestore.collection("users").document(uid).collection("habits").document()

        val habitWithId = habit.copy(id = docRef.id)

        docRef.set(habitWithId).await()
    }
}