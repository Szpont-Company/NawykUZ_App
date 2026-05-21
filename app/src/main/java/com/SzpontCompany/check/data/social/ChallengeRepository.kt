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
                    //close(error) TODO: poprawic to bo to chyba tylko maskuje blad
                    trySend(emptyList())
                    close()
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
            .whereIn("status", listOf("ACTIVE", "COMPLETED", "SURRENDERED"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    //close(error) TODO: poprawic to bo to chyba tylko maskuje blad
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }

                val battles = snapshot?.toObjects(Battle::class.java)?.filter { battle ->
                    if (battle.status == "ACTIVE") true
                    else {
                        val isPlayer1 = battle.player1Id == userId
                        if (isPlayer1) !battle.player1Acknowledged else !battle.player2Acknowledged
                    }
                } ?: emptyList()
                trySend(battles).isSuccess
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateBattleProgress(
        battleId: String,
        currentUserId: String,
        isDone: Boolean,
        habitId: String? = null,
        newDates: List<String>? = null,
        newHabitStreak: Int? = null
    ): Result<Unit> {
        return try {
            val battleRef = battlesCollection.document(battleId)
            val habitRef = habitId?.let { db.collection("users").document(currentUserId).collection("habits").document(it) }

            db.runTransaction { transaction ->
                val snapshot = transaction.get(battleRef)
                val battle = snapshot.toObject(Battle::class.java) ?: throw Exception("Battle not found")

                if (battle.status != "ACTIVE") return@runTransaction

                val isPlayer1 = battle.player1Id == currentUserId
                val totalDays = battle.totalDays
                val todayString = java.time.LocalDate.now().toString()

                if (isPlayer1) {
                    val wasDoneToday = battle.player1LastLogDate == todayString
                    if (isDone && wasDoneToday) return@runTransaction
                    if (!isDone && !wasDoneToday) return@runTransaction

                    val newDays = if (isDone) battle.player1Days + 1 else maxOf(0, battle.player1Days - 1)
                    val damagePerDay = 100 / totalDays
                    val opponentNewHp = if (isDone) maxOf(0, battle.player2Hp - damagePerDay) else minOf(100, battle.player2Hp + damagePerDay)
                    val newLogDate = if (isDone) todayString else ""

                    val isWin = newDays >= totalDays || opponentNewHp <= 0
                    val newStatus = if (isWin) "COMPLETED" else "ACTIVE"
                    val newWinnerId = if (isWin) currentUserId else battle.winnerId

                    transaction.update(battleRef, mapOf(
                        "player1Days" to newDays,
                        "player2Hp" to opponentNewHp,
                        "player1LastLogDate" to newLogDate,
                        "status" to newStatus,
                        "winnerId" to newWinnerId
                    ))
                } else {
                    val wasDoneToday = battle.player2LastLogDate == todayString
                    if (isDone && wasDoneToday) return@runTransaction
                    if (!isDone && !wasDoneToday) return@runTransaction

                    val newDays = if (isDone) battle.player2Days + 1 else maxOf(0, battle.player2Days - 1)
                    val damagePerDay = 100 / totalDays
                    val opponentNewHp = if (isDone) maxOf(0, battle.player1Hp - damagePerDay) else minOf(100, battle.player1Hp + damagePerDay)
                    val newLogDate = if (isDone) todayString else ""

                    val isWin = newDays >= totalDays || opponentNewHp <= 0
                    val newStatus = if (isWin) "COMPLETED" else "ACTIVE"
                    val newWinnerId = if (isWin) currentUserId else battle.winnerId

                    transaction.update(battleRef, mapOf(
                        "player2Days" to newDays,
                        "player1Hp" to opponentNewHp,
                        "player2LastLogDate" to newLogDate,
                        "status" to newStatus,
                        "winnerId" to newWinnerId
                    ))
                }

                if (habitRef != null && newDates != null && newHabitStreak != null) {
                    transaction.update(habitRef, mapOf(
                        "completedDates" to newDates,
                        "streak" to newHabitStreak
                    ))
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getWonBattlesCount(userId: String): Flow<Int> = callbackFlow {
        val listener = battlesCollection
            .whereEqualTo("winnerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                val count = snapshot?.size() ?: 0
                trySend(count)
            }
        awaitClose { listener.remove() }
    }


}