package com.SzpontCompany.check.data.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun updatePresence(isOnline: Boolean) {
        val myId = currentUserId ?: return
        try {
            firestore.collection("users").document(myId).update(
                mapOf(
                    "isOnline" to isOnline,
                    "lastActive" to System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
        }
    }

    suspend fun searchUsers(query: String): List<Friend> {
        if (query.isBlank()) return emptyList()

        return try {
            val result = firestore.collection("users")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(20)
                .get()
                .await()

            result.documents.mapNotNull { doc ->
                val uid = doc.id
                if (uid == currentUserId) return@mapNotNull null

                val name = doc.getString("name") ?: ""
                val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                val avatar = doc.getString("avatarEmoji") ?: ""
                val bgColor = doc.getString("bgColor") ?: "Mint"

                Friend(
                    uid = uid,
                    name = name,
                    initials = initials,
                    xp = 0,
                    avatarEmoji = avatar,
                    bgColor = bgColor,
                    status = ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendFriendRequest(receiverId: String): Boolean {
        val myId = currentUserId ?: return false
        return try {
            val myDoc = firestore.collection("users").document(myId).get().await()
            val myName = myDoc.getString("name") ?: ""
            val myAvatar = myDoc.getString("avatarEmoji") ?: "👤"
            val myBgColor = myDoc.getString("bgColor") ?: "Mint"

            val requestRef = firestore.collection("friend_requests").document()
            val request = FriendRequest(
                requestId = requestRef.id,
                senderId = myId,
                senderName = myName,
                senderAvatar = myAvatar,
                senderBgColor = myBgColor,
                receiverId = receiverId,
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )
            requestRef.set(request).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getIncomingRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val myId = currentUserId ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("friend_requests")
            .whereEqualTo("receiverId", myId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val requests = snapshot.documents.mapNotNull { it.toObject(FriendRequest::class.java) }
                    trySend(requests)
                }
            }

        awaitClose { listener.remove() }
    }

    fun getOutgoingRequests(): Flow<List<String>> = callbackFlow {
        val myId = currentUserId ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("friend_requests")
            .whereEqualTo("senderId", myId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    error?.printStackTrace()
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val receiverIds = snapshot.documents.mapNotNull { it.getString("receiverId") }
                trySend(receiverIds)
            }

        awaitClose { listener.remove() }
    }

    suspend fun respondToRequest(requestId: String, accept: Boolean, senderId: String): Boolean {
        val myId = currentUserId ?: return false
        return try {
            if (accept) {
                val myDoc = firestore.collection("users").document(myId).get().await()
                val hisDoc = firestore.collection("users").document(senderId).get().await()

                val myData = mapOf(
                    "uid" to myId,
                    "name" to (myDoc.getString("name") ?: ""),
                    "avatarEmoji" to (myDoc.getString("avatarEmoji") ?: ""),
                    "bgColor" to (myDoc.getString("bgColor") ?: "Mint"),
                    "timestamp" to System.currentTimeMillis()
                )

                val hisData = mapOf(
                    "uid" to senderId,
                    "name" to (hisDoc.getString("name") ?: ""),
                    "avatarEmoji" to (hisDoc.getString("avatarEmoji") ?: ""),
                    "bgColor" to (hisDoc.getString("bgColor") ?: "Mint"),
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.runBatch { batch ->
                    val requestRef = firestore.collection("friend_requests").document(requestId)
                    batch.update(requestRef, "status", "ACCEPTED")

                    val myFriendRef = firestore.collection("users").document(myId).collection("friends").document(senderId)
                    batch.set(myFriendRef, hisData)

                    val hisFriendRef = firestore.collection("users").document(senderId).collection("friends").document(myId)
                    batch.set(hisFriendRef, myData)
                }.await()
            } else {
                firestore.collection("friend_requests").document(requestId).delete().await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeFriend(friendId: String): Boolean {
        val myId = currentUserId ?: return false
        return try {
            firestore.runBatch { batch ->
                val myFriendRef = firestore.collection("users").document(myId).collection("friends").document(friendId)
                batch.delete(myFriendRef)

                val hisFriendRef = firestore.collection("users").document(friendId).collection("friends").document(myId)
                batch.delete(hisFriendRef)
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSuggestedFriends(): List<Friend> {
        val myId = currentUserId ?: return emptyList()
        return try {
            val result = firestore.collection("users").limit(15).get().await()
            result.documents.mapNotNull { doc ->
                if (doc.id == myId) return@mapNotNull null

                val name = doc.getString("name") ?: ""
                val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                Friend(
                    uid = doc.id,
                    name = name,
                    initials = initials,
                    xp = doc.getLong("xp")?.toInt() ?: 0,
                    avatarEmoji = doc.getString("avatarEmoji") ?: "",
                    bgColor = doc.getString("bgColor") ?: "Mint",
                    status = ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getMyFriends(): Flow<List<Friend>> = callbackFlow {
        val myId = currentUserId ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(myId).collection("friends")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val friendsList = snapshot.documents.mapNotNull { doc ->
                        val uid = doc.id
                        var name = doc.getString("name") ?: ""
                        var avatarEmoji = doc.getString("avatarEmoji") ?: ""
                        var bgColor = doc.getString("bgColor") ?: "Mint"
                        var xp = doc.getLong("xp")?.toInt() ?: 0
                        var isOnline = false

                        try {
                            val userDoc = firestore.collection("users").document(uid).get().await()
                            if (userDoc.exists()) {
                                isOnline = userDoc.getBoolean("isOnline") ?: false
                                userDoc.getString("name")?.let { name = it }
                                userDoc.getString("avatarEmoji")?.let { avatarEmoji = it }
                                userDoc.getString("bgColor")?.let { bgColor = it }
                                userDoc.getLong("xp")?.toInt()?.let { xp = it }
                            }
                        } catch (e: Exception) {}

                        val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")

                        Friend(
                            uid = uid,
                            name = name,
                            initials = initials,
                            xp = xp,
                            avatarEmoji = avatarEmoji,
                            bgColor = bgColor,
                            online = isOnline
                        )
                    }
                    trySend(friendsList)
                }
            }
        awaitClose { listener.remove() }
    }
}
