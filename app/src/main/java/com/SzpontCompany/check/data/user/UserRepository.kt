package com.SzpontCompany.check.data.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import android.content.Context
import com.SzpontCompany.check.config.FirebaseConfig
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository private constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cache: UserCache
) {

    private var userListener: ListenerRegistration? = null

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(context: Context): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(
                    FirebaseAuth.getInstance(),
                    FirebaseFirestore.getInstance(),
                    UserCache(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }
    }

    private val _userFlow = MutableStateFlow<User?>(value = null)
    val userFlow: StateFlow<User?> = _userFlow.asStateFlow()

    // NOWOŚĆ: Automatyczne słuchanie zmian w profilu użytkownika
    fun startUserObservation() {
        val uid = auth.currentUser?.uid ?: return
        if (userListener != null) return // Już słuchamy

        userListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserRepository", "Błąd listenera: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = User(
                        uid = uid,
                        name = snapshot.getString("name") ?: "",
                        email = snapshot.getString("email") ?: "",
                        nickname = snapshot.getString("nickname") ?: "",
                        isAdmin = snapshot.getBoolean("isAdmin") ?: false,
                        avatarEmoji = snapshot.getString("avatarEmoji") ?: "",
                        bgColor = snapshot.getString("bgColor") ?: "Mint",
                        currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0,
                        bestStreak = snapshot.getLong("bestStreak")?.toInt() ?: 0,
                        weeklyProgress = (snapshot.get("weeklyProgress") as? List<*>)?.map { (it as? Number)?.toFloat() ?: 0f } ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
                    )
                    _userFlow.value = user
                    cache.save(user)
                }
            }
    }

    fun stopObservation() {
        userListener?.remove()
        userListener = null
    }

    suspend fun getUser(forceRefresh: Boolean = false): User {
        val uid = auth.currentUser?.uid ?: throw Exception("Brak UID")

        // Jeśli już mamy dane w Flow i nie wymuszamy odświeżenia, używamy ich
        val current = _userFlow.value
        if (!forceRefresh && current != null && current.uid == uid) {
            return current
        }

        val document = firestore.collection("users").document(uid).get().await()
        val user = User(
            uid = uid,
            name = document.getString("name") ?: "",
            email = document.getString("email") ?: "",
            nickname = document.getString("nickname") ?: "",
            isAdmin = document.getBoolean("isAdmin") ?: false,
            avatarEmoji = document.getString("avatarEmoji") ?: "",
            bgColor = document.getString("bgColor") ?: "Mint",
            currentStreak = document.getLong("currentStreak")?.toInt() ?: 0,
            bestStreak = document.getLong("bestStreak")?.toInt() ?: 0,
            weeklyProgress = (document.get("weeklyProgress") as? List<*>)?.map { (it as? Number)?.toFloat() ?: 0f } ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )

        _userFlow.value = user
        cache.save(user)
        return user
    }

    suspend fun updateUserStreaks(uid: String, currentStreak: Int, bestStreak: Int) {
        firestore.collection("users").document(uid)
            .update(
                mapOf(
                    "currentStreak" to currentStreak,
                    "bestStreak" to bestStreak
                )
            ).await()

        // Aktualizujemy Flow lokalnie, aby Dashboard natychmiast widział zmianę
        val current = _userFlow.value
        if (current != null && current.uid == uid) {
            val updated = current.copy(currentStreak = currentStreak, bestStreak = bestStreak)
            _userFlow.value = updated
            cache.save(updated)
        }
    }

    suspend fun saveUserData(uid: String, name: String, email: String) {
        val userData = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "nickname" to "",
            "isAdmin" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "currentStreak" to 0,
            "bestStreak" to 0,
            "weeklyProgress" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
        firestore.collection("users").document(uid).set(userData, SetOptions.merge()).await()
        val newUser = User(uid = uid, name = name, email = email)
        _userFlow.value = newUser
        cache.save(newUser)
    }

    suspend fun isNicknameTaken(nickname: String): Boolean {
        if (nickname.isBlank()) return false
        val myUid = auth.currentUser?.uid
        val snapshot = firestore.collection("users").whereEqualTo("nickname", nickname).get().await()
        return snapshot.documents.any { it.id != myUid }
    }

    suspend fun updateProfileViaFunctions(name: String, nickname: String, avatarEmoji: String, bgColor: String) {
        val data = hashMapOf("name" to name, "nickname" to nickname, "avatarEmoji" to avatarEmoji, "bgColor" to bgColor)
        FirebaseConfig.functions.getHttpsCallable("updateUserProfile").call(data).await()
    }

    fun getUserCoins() : Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val coins = snapshot?.getLong("coins")?.toInt() ?: 0
                trySend(coins)
            }
        awaitClose { listener.remove() }
    }

    fun clearCache() {
        stopObservation()
        cache.clear()
        _userFlow.value = null
    }
}