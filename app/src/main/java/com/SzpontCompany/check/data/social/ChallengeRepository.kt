package com.SzpontCompany.check.data.social

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChallengeRepository(private val db: FirebaseFirestore) {

    private val invitesCollection = db.collection("challenge_invites")

    fun getPendingInvitesForUser(userId: String): Flow<List<ChallengeInvite>> = callbackFlow {
        val listener = invitesCollection
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val invites = snapshot?.toObjects(ChallengeInvite::class.java) ?: emptyList()
                trySend(invites).isSuccess
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendInvite(invite: ChallengeInvite): Result<Unit> {
        return try {
            val docRef = invitesCollection.document()

            val inviteWithId = invite.copy(id = docRef.id)

            docRef.set(inviteWithId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInviteStatus(inviteId: String, newStatus: String): Result<Unit> {
        return try {
            invitesCollection.document(inviteId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}