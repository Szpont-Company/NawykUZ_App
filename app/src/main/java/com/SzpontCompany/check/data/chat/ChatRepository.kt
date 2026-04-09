package com.SzpontCompany.check.data.chat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUserId: String? get() = auth.currentUser?.uid

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    fun getMessages(friendId: String): Flow<List<ChatMessage>> = callbackFlow {
        val myId = currentUserId ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val chatId = getChatId(myId, friendId)
        val listener = firestore.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.map { doc ->
                        val isReadField = doc.getBoolean("isRead") ?: doc.getBoolean("read") ?: false
                        ChatMessage(
                            id = doc.getString("id") ?: doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            type = doc.getString("type") ?: "TEXT",
                            isRead = isReadField
                        )
                    }
                    trySend(messages)

                    // Mark received messages as read
                    val unreadUpdates = snapshot.documents.filter {
                        val isReadField = it.getBoolean("isRead") ?: it.getBoolean("read") ?: false
                        it.getString("senderId") != myId && !isReadField
                    }

                    if (unreadUpdates.isNotEmpty()) {
                        firestore.runBatch { batch ->
                            unreadUpdates.forEach { doc ->
                                batch.update(doc.reference, "isRead", true)
                            }
                        }
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(friendId: String, text: String, type: String = "TEXT") {
        val myId = currentUserId ?: return
        val chatId = getChatId(myId, friendId)
        val messageId = UUID.randomUUID().toString()
        val timestampLocal = System.currentTimeMillis()

        val msgData = mapOf(
            "id" to messageId,
            "senderId" to myId,
            "text" to text,
            "timestamp" to timestampLocal,
            "type" to type,
            "isRead" to false
        )

        try {
            firestore.collection("chats").document(chatId)
                .collection("messages").document(messageId)
                .set(msgData).await()

            // Update last message in chat document for list previews
            firestore.collection("chats").document(chatId).set(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to timestampLocal,
                    "participants" to listOf(myId, friendId)
                ), com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
