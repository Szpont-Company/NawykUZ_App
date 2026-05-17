package com.SzpontCompany.check.data.habit

import android.content.Context
import com.SzpontCompany.check.R
import com.SzpontCompany.check.config.FirebaseConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

class HabitRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val functions = FirebaseConfig.functions

    /**
     * Tworzy/aktualizuje specjalny nawyk kroków w subkolekcji `users/{uid}/habits`.
     * Zapis jest idempotentny (stałe ID dokumentu), a merge nie nadpisuje pól typu streak/completedDates.
     */
    suspend fun upsertStepsHabit(habit: Habit, habitId: String = STEPS_HABIT_ID) {
        val uid = auth.currentUser?.uid ?: throw Exception("Brak zalogowanego użytkownika")
        val habitsCol = firestore.collection("users").document(uid)
            .collection("habits")

        val documents = habitsCol.get().await().documents

        fun isStepsCandidate(name: String?, icon: String?, isStepsHabit: Boolean?, stepsHabit: Boolean?): Boolean {
            val n = (name ?: "").lowercase(Locale.getDefault())
            val i = icon ?: ""
            return isStepsHabit == true ||
                stepsHabit == true ||
                i.contains("🚶") ||
                n.contains("krok") ||
                n.contains("step")
        }

        val stepDocs = documents.filter { doc ->
            isStepsCandidate(
                doc.getString("name"),
                doc.getString("icon"),
                doc.getBoolean("isStepsHabit"),
                doc.getBoolean("stepsHabit")
            )
        }

        val stepsIdDoc = documents.firstOrNull { it.id == habitId }
        val flaggedDoc = stepDocs.firstOrNull {
            it.getBoolean("isStepsHabit") == true || it.getBoolean("stepsHabit") == true
        }

        val targetDoc = stepsIdDoc ?: flaggedDoc ?: stepDocs.firstOrNull()
        var targetRef = targetDoc?.reference ?: habitsCol.document(habitId)
        var targetId = targetDoc?.id ?: habitId

        if (stepsIdDoc == null && targetDoc != null && targetDoc.id != habitId) {
            val sourceData = (targetDoc.data ?: emptyMap()).toMutableMap()
            sourceData["id"] = habitId
            sourceData["isStepsHabit"] = true
            sourceData["stepsHabit"] = true

            habitsCol.document(habitId).set(sourceData, SetOptions.merge()).await()
            try {
                targetDoc.reference.delete().await()
            } catch (_: Exception) {
                // ignore
            }

            targetRef = habitsCol.document(habitId)
            targetId = habitId
        }

        val data = mapOf(
            "id" to targetId,
            "name" to habit.name,
            "icon" to habit.icon,
            "colorName" to habit.colorName,
            "frequency" to habit.frequency,
            "dailyGoal" to habit.dailyGoal,
            "unit" to habit.unit,
            "difficulty" to habit.difficulty,
            "isActive" to habit.isActive,
            "isStepsHabit" to true,
            "stepsHabit" to true
        )

        targetRef.set(data, SetOptions.merge()).await()

        documents
            .filter { it.id != targetId }
            .filter {
                isStepsCandidate(
                    it.getString("name"),
                    it.getString("icon"),
                    it.getBoolean("isStepsHabit"),
                    it.getBoolean("stepsHabit")
                )
            }
            .forEach { doc ->
                val battleId = doc.getString("battleId")
                if (battleId == null) {
                    try {
                        doc.reference.delete().await()
                    } catch (_: Exception) {
                        // ignore
                    }
                }
            }
    }

    suspend fun addHabit(habit: Habit): String {
        val uid = auth.currentUser?.uid ?: throw Exception("Brak zalogowanego użytkownika")
        val docRef = firestore.collection("users").document(uid).collection("habits").document()
        val habitWithId = habit.copy(id = docRef.id)

        docRef.set(habitWithId).await()
        return docRef.id
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

    suspend fun earnCoinsCloud(actionType: String, amount: Int? = null): Result<Int> {
        return try {
            val data = hashMapOf<String, Any>("actionType" to actionType)
            if (amount != null) {
                data["amount"] = amount
            }
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

    suspend fun updateHabitDailyNote(habitId: String, dateString: String, newNote: String) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid)
            .collection("habits").document(habitId)
            .update("dailyNotes.$dateString", newNote)
            .await()
    }

    suspend fun addHabitForUser(userId: String, habit: Habit) {
        val docRef = firestore.collection("users").document(userId).collection("habits").document()
        val habitWithId = habit.copy(id = docRef.id)

        docRef.set(habitWithId).await()
    }

    suspend fun markHabitDoneByBattleId(battleId: String, dateString: String, isDone: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val habitsRef = firestore.collection("users").document(uid).collection("habits")

        val querySnapshot = habitsRef.whereEqualTo("battleId", battleId).get().await()

        for (doc in querySnapshot.documents) {
            val habit = doc.toObject(Habit::class.java) ?: continue

            val newDates = if (isDone) {
                (habit.completedDates + dateString).distinct()
            } else {
                habit.completedDates.filter { it != dateString }
            }
            val newStreak = if (isDone) habit.streak + 1 else maxOf(0, habit.streak - 1)

            doc.reference.update(
                mapOf(
                    "completedDates" to newDates,
                    "streak" to newStreak
                )
            ).await()
        }
    }

    suspend fun updateStepsGoal(goal: Int, context: Context) {
        val safeGoal = goal.coerceIn(1000, 100000)

        val stepHabit = Habit(
            name = context.getString(R.string.habit_steps_name),
            icon = "🚶",
            colorName = "Mint",
            frequency = "Daily",
            dailyGoal = safeGoal,
            unit = context.getString(R.string.habit_steps_unit),
            difficulty = context.getString(R.string.habit_steps_difficulty),
            isActive = true,
            isStepsHabit = true
        )

        upsertStepsHabit(stepHabit)
    }

    private companion object {
        const val STEPS_HABIT_ID = "steps"
    }
}