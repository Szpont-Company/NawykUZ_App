package com.SzpontCompany.check.data.user

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.functions
import com.google.firebase.functions.ktx.functions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import android.content.Context
import com.SzpontCompany.check.config.FirebaseConfig

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

    suspend fun updateUsername(name: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user")

        firestore.collection("users")
            .document(uid)
            .set(mapOf("name" to name), SetOptions.merge())
            .await()

        val current = _userFlow.value
        if(current != null) {
            val updatedUser = current.copy(name = name)
            cache.save(updatedUser)
            _userFlow.value = updatedUser
        }
    }

    suspend fun updateEmail(email: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("No user")

        firestore.collection("users")
            .document(uid)
            .set(mapOf("email" to email), SetOptions.merge())
            .await()

        val current = _userFlow.value
        if(current != null) {
            val updatedUser = current.copy(email = email)
            cache.save(updatedUser)
            _userFlow.value = updatedUser
        }
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
        }
    }

}