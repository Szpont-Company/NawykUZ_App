package com.SzpontCompany.check.data.social

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FriendRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Szukanie użytkowników po nazwie (lub uchwycie)
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

                val name = doc.getString("name") ?: "Nieznany"
                val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                val avatar = doc.getString("avatarEmoji") ?: ""
                val bgColor = doc.getString("bgColor") ?: "Mint"

                // Tutaj można by jeszcze pobrać status (czy wysłano, czy znajomy), na razie mock
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

    // Wysyłanie zaproszenia
    suspend fun sendFriendRequest(receiverId: String, myName: String, myAvatar: String, myBgColor: String): Boolean {
        val myId = currentUserId ?: return false
        return try {
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
            false
        }
    }

    // Nasłuchiwanie na przychodzące zaproszenia
    fun getIncomingRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val myId = currentUserId ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("friend_requests")
            .whereEqualTo("receiverId", myId)
            .whereEqualTo("status", "PENDING")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
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

    // Akceptacja lub Odrzucenie
    suspend fun respondToRequest(requestId: String, accept: Boolean, senderId: String): Boolean {
        val myId = currentUserId ?: return false
        return try {
            if (accept) {
                // Dodajemy siebie nawzajem jako znajomych
                firestore.runBatch { batch ->
                    // Usuń invite
                    val requestRef = firestore.collection("friend_requests").document(requestId)
                    batch.delete(requestRef)

                    // Dodaj do moich
                    val myFriendRef = firestore.collection("users").document(myId).collection("friends").document(senderId)
                    batch.set(myFriendRef, mapOf("timestamp" to System.currentTimeMillis()))

                    // Dodaj do jego
                    val hisFriendRef = firestore.collection("users").document(senderId).collection("friends").document(myId)
                    batch.set(hisFriendRef, mapOf("timestamp" to System.currentTimeMillis()))
                }.await()
            } else {
                // Po prostu usuń dokument na stałe
                firestore.collection("friend_requests").document(requestId).delete().await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Pobranie moich znajomych (tutaj uproszczone zapytanie do listy dla widoku)
    fun getMyFriends(): Flow<List<Friend>> = callbackFlow {
        val myId = currentUserId ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(myId).collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val friendIds = snapshot.documents.map { it.id }
                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    // Żeby nie komplikować pobierania każdego z osobna w tym mocku, strzelamy `whereIn` jeśli < 10 osób
                    if (friendIds.size <= 10) {
                        firestore.collection("users").whereIn("uid", friendIds).get()
                            .addOnSuccessListener { friendsSnapshot ->
                                val friendsList = friendsSnapshot.documents.mapNotNull { doc ->
                                    val name = doc.getString("name") ?: "Nieznany"
                                    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
                                    Friend(
                                        uid = doc.id,
                                        name = name,
                                        initials = initials,
                                        xp = doc.getLong("xp")?.toInt() ?: 0,
                                        avatarEmoji = doc.getString("avatarEmoji") ?: "",
                                        bgColor = doc.getString("bgColor") ?: "Mint",
                                        online = false // to do zaimplementowania logiki online/offline realnie
                                    )
                                }
                                trySend(friendsList)
                            }
                    } else {
                        trySend(emptyList())
                    }
                }
            }
        awaitClose { listener.remove() }
    }
}
