package com.SzpontCompany.check.data.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUserNotifications(): Flow<List<Notification>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(userId)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "Error fetching notifications", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notifications = snapshot.toObjects(Notification::class.java)
                    val notificationsWithIds = notifications.mapIndexed { index, notification ->
                        notification.copy(id = snapshot.documents[index].id)
                    }
                    trySend(notificationsWithIds)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun markAsRead(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("notifications").document(notificationId)
                .update("isRead", true).await()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error marking as read", e)
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .collection("notifications").document(notificationId)
                .delete().await()
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error deleting notification", e)
        }
    }
}