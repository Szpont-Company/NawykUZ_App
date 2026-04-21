package com.SzpontCompany.check.data.habit

import com.SzpontCompany.check.config.FirebaseConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HabitRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseConfig.functions

    suspend fun addHabit(habit: Habit) {
        val uid = auth.currentUser?.uid ?: throw Exception("Brak zalogowanego użytkownika")
        val docRef = firestore.collection("users").document(uid).collection("habits").document()
        val habitWithId = habit.copy(id = docRef.id)

        docRef.set(habitWithId).await()
    }

    fun getUserHabits(): Flow<List<Habit>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid).collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val habits = snapshot.documents.mapNotNull { document ->
                        document.toObject(Habit::class.java)?.copy(id = document.id)
                    }
                    trySend(habits)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun earnCoinsCloud(actionType: String): Result<Int> {
        return try {
            val data = hashMapOf("actionType" to actionType)
            val result = functions.getHttpsCallable("earnCoins").call(data).await()
            val response = result.data as? Map<*, *>
            val added = (response?.get("added") as? Number)?.toInt() ?: 0
            Result.success(added)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHabitCompletionAndStreak(habitId: String, completedDates: List<String>, streak: Int) {
        val uid = auth.currentUser?.uid ?: throw Exception("Brak zalogowanego użytkownika")
        firestore.collection("users")
            .document(uid)
            .collection("habits")
            .document(habitId)
            .update(
                mapOf(
                    "completedDates" to completedDates,
                    "streak" to streak
                )
            )
            .await()
    }
}