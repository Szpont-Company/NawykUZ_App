package com.SzpontCompany.check.data.social

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChallengeRepository(private val db: FirebaseFirestore) {

    private val invitesCollection = db.collection("challenge_invites")
    private val battlesCollection = db.collection("battles")

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

    suspend fun createBattle(battle: Battle): Result<String> {
        return try {
            val docRef = battlesCollection.document()
            val battleWithId = battle.copy(id = docRef.id)
            docRef.set(battleWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getActiveBattlesForUser(userId: String): Flow<List<Battle>> = callbackFlow {
        val listener = battlesCollection
            .whereArrayContains("participants", userId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val battles = snapshot?.toObjects(Battle::class.java) ?: emptyList()
                trySend(battles).isSuccess
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateBattleProgress(battleId: String, currentUserId: String, isDone: Boolean): Result<Unit> {
        return try {
            val battleRef = battlesCollection.document(battleId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(battleRef)
                val battle = snapshot.toObject(Battle::class.java) ?: throw Exception("Battle not found")

                val isPlayer1 = battle.player1Id == currentUserId
                val totalDays = battle.totalDays

                if (isPlayer1) {
                    if (isDone && battle.player1CompletedToday) return@runTransaction
                    if (!isDone && !battle.player1CompletedToday) return@runTransaction

                    val newDays = if (isDone) battle.player1Days + 1 else maxOf(0, battle.player1Days - 1)
                    val opponentNewHp = 100 - ((newDays * 100) / totalDays)

                    transaction.update(battleRef, mapOf(
                        "player1Days" to newDays,
                        "player2Hp" to opponentNewHp,
                        "player1CompletedToday" to isDone
                    ))
                } else {
                    if (isDone && battle.player2CompletedToday) return@runTransaction
                    if (!isDone && !battle.player2CompletedToday) return@runTransaction

                    val newDays = if (isDone) battle.player2Days + 1 else maxOf(0, battle.player2Days - 1)
                    val opponentNewHp = 100 - ((newDays * 100) / totalDays)

                    transaction.update(battleRef, mapOf(
                        "player2Days" to newDays,
                        "player1Hp" to opponentNewHp,
                        "player2CompletedToday" to isDone
                    ))
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}