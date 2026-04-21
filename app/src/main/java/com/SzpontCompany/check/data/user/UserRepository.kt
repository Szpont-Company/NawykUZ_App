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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserRepository private constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cache: UserCache
) {
    
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


    suspend fun getUser(forceRefresh: Boolean = false): User {
        val firebaseUser = auth.currentUser

        if(firebaseUser == null) {
            val cachedUid = cache.getLastUid()
            val cached = cachedUid?.let { cache.get(it) }

            if(cached != null) {
                _userFlow.value = cached
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
                _userFlow.value = cached
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
            bgColor = document.getString("bgColor") ?: "Mint"
        )

        cache.save(user)
        _userFlow.value = user

        return user
    }
    fun clearCache() {
        cache.clear()
    }

    suspend fun saveUserData(uid: String, name: String, email: String) {
        val userData = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "nickname" to "",
            "isAdmin" to false,
            "createdAt" to FieldValue.serverTimestamp()
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
            bgColor = "Mint"
        )
        cache.save(updatedUser)
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
}