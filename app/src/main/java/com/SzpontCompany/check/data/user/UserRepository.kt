package com.SzpontCompany.check.data.user

import android.util.Log
import android.content.Context
import com.SzpontCompany.check.config.FirebaseConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
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

    fun startUserObservation() {
        val uid = auth.currentUser?.uid ?: return
        if (userListener != null) return

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
                        lastGlobalStreakDate = snapshot.getString("lastGlobalStreakDate") ?: "",
                        weeklyProgress = (snapshot.get("weeklyProgress") as? List<*>)?.map { (it as? Number)?.toFloat() ?: 0f } ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
                        unlockedBadges = (snapshot.get("unlockedBadges") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
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
        val firebaseUser = auth.currentUser

        if(firebaseUser == null) {
            val cachedUid = cache.getLastUid()
            val cached = cachedUid?.let { cache.get(it) }

            if(cached != null) {
                if (_userFlow.value == null) {
                    _userFlow.value = cached
                }
                return cached
            } else {
                throw Exception("No user and no cache")
            }
        }

        val uid = firebaseUser.uid

        if(!forceRefresh) {
            val cached = cache.get(uid)
            if(cached != null && cache.isValid()) {
                Log.i("UserRepository", "Using cached user data for UID: $uid")
                if (userListener == null) {
                    _userFlow.value = cached
                }
                return cached
            }
        }

        Log.i("UserRepository", "Fetching user data from Firestore for UID: $uid")
        val document = firestore.collection("users")
            .document(uid)
            .get()
            .await()

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
            lastGlobalStreakDate = document.getString("lastGlobalStreakDate") ?: "",
            weeklyProgress = (document.get("weeklyProgress") as? List<*>)?.map { (it as? Number)?.toFloat() ?: 0f } ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
            unlockedBadges = (document.get("unlockedBadges") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        )

        cache.save(user)
        _userFlow.value = user

        return user
    }

    fun clearCache() {
        stopObservation()
        cache.clear()
        _userFlow.value = null
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
            "lastGlobalStreakDate" to "",
            "weeklyProgress" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
        firestore
            .collection("users")
            .document(uid)
            .set(userData, SetOptions.merge())
            .await()

        val updatedUser  = User(
            uid = uid,
            name = name,
            email = email,
            nickname = "",
            isAdmin = false,
            avatarEmoji = "",
            bgColor = "Mint",
            currentStreak = 0,
            bestStreak = 0,
            lastGlobalStreakDate = ""
        )
        cache.save(updatedUser)
        _userFlow.value = updatedUser
    }

    suspend fun isNicknameTaken(nickname: String): Boolean {
        if (nickname.isBlank()) return false
        val myUid = auth.currentUser?.uid
        val snapshot = firestore.collection("users")
            .whereEqualTo("nickname", nickname)
            .get()
            .await()

        return snapshot.documents.any { it.id != myUid }
    }

    suspend fun updateProfileViaFunctions(name: String, nickname: String, avatarEmoji: String, bgColor: String) {
        val data = hashMapOf(
            "name" to name,
            "nickname" to nickname,
            "avatarEmoji" to avatarEmoji,
            "bgColor" to bgColor
        )

        FirebaseConfig.functions.getHttpsCallable("updateUserProfile").call(data).await()

        val current = _userFlow.value
        if (current != null) {
            val updatedUser = current.copy(
                name = name,
                nickname = nickname,
                avatarEmoji = avatarEmoji,
                bgColor = bgColor
            )
            cache.save(updatedUser)
            _userFlow.value = updatedUser

            try {
                val myId = updatedUser.uid
                val myFriendsSnapshot = firestore.collection("users").document(myId).collection("friends").get().await()
                if (!myFriendsSnapshot.isEmpty) {
                    firestore.runBatch { batch ->
                        val friendData = mapOf(
                            "name" to name,
                            "avatarEmoji" to avatarEmoji,
                            "bgColor" to bgColor
                        )
                        for (doc in myFriendsSnapshot.documents) {
                            val friendId = doc.id
                            val theirFriendRef = firestore.collection("users").document(friendId).collection("friends").document(myId)
                            batch.update(theirFriendRef, friendData)
                        }
                    }.await()
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to sync profile update to friends", e)
            }
        }
    }

    suspend fun deleteUserAccount() {
        val user = auth.currentUser ?: throw Exception("Brak zalogowanego użytkownika")

        FirebaseConfig.functions.getHttpsCallable("deleteUserAccount").call().await()

        auth.signOut()

        clearCache()
        _userFlow.value = null
    }

    fun getUserCoins() : Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid

        if(uid == null) {
            trySend(0)
            close(Exception("No user"))
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, exception ->
                if(exception != null) {
                    return@addSnapshotListener
                }

                if(snapshot != null && snapshot.exists()) {
                    val coins = snapshot.getLong("coins")?.toInt() ?: 0
                    trySend(coins)
                } else {
                    trySend(0)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUserStreaks(uid: String, currentStreak: Int, bestStreak: Int, lastGlobalStreakDate: String) {
        firestore.collection("users").document(uid)
            .update(
                mapOf(
                    "currentStreak" to currentStreak,
                    "bestStreak" to bestStreak,
                    "lastGlobalStreakDate" to lastGlobalStreakDate
                )
            ).await()

        val currentUserState = _userFlow.value
        if (currentUserState != null && currentUserState.uid == uid) {
            val updatedUser = currentUserState.copy(
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                lastGlobalStreakDate = lastGlobalStreakDate
            )
            _userFlow.value = updatedUser
            cache.save(updatedUser)
        }
    }

    suspend fun claimBadge(uid: String, badgeId: String) {
        firestore.collection("users").document(uid)
            .update("unlockedBadges", FieldValue.arrayUnion(badgeId))
            .await()
    }
}