package com.SzpontCompany.check.data.social

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Wyczerpujące testy dla systemu Habit Battle
 * Punkt 10: Rywalizacja, zakłady (monety) i kary (utrata HP)
 */
class ChallengeRepositoryTest {

    private lateinit var challengeRepository: FakeChallengeRepository
    private lateinit var coinsRepository: FakeCoinsRepository

    @Before
    fun setUp() {
        coinsRepository = FakeCoinsRepository()
        challengeRepository = FakeChallengeRepository(coinsRepository)
    }

    // ==================== TESTY KARY I ATAKU (HP) ====================

    @Test
    fun `testPenaltyLogic - player loses HP when habit is NOT done`() = runTest {
        // Given: Gracz P1 ma 100 HP na start
        val battleId = "battle_1"
        challengeRepository.createBattle(FakeBattle(id = battleId, player1Id = "p1", player2Id = "p2", player1Hp = 100))

        // When: P1 NIE wykonuje nawyku (isDone = false)
        challengeRepository.updateBattleProgress(battleId, "p1", isDone = false)

        // Then: HP gracza P1 spada w ramach kary
        val battle = challengeRepository.getBattle(battleId)
        assertTrue("HP gracza 1 powinno spaść (kara za opuszczenie nawyku)", battle!!.player1Hp < 100)
    }

    @Test
    fun `testAttackLogic - opponent loses HP when habit IS done`() = runTest {
        // Given: Gracz P2 ma 100 HP na start
        val battleId = "battle_2"
        challengeRepository.createBattle(FakeBattle(id = battleId, player1Id = "p1", player2Id = "p2", player2Hp = 100))

        // When: P1 WYKONUJE nawyk (atakuje P2)
        challengeRepository.updateBattleProgress(battleId, "p1", isDone = true)

        // Then: HP przeciwnika (P2) powinno spaść
        val battle = challengeRepository.getBattle(battleId)
        assertTrue("HP przeciwnika (P2) powinno spaść po udanym wykonaniu nawyku przez P1", battle!!.player2Hp < 100)
    }

    // ==================== TESTY ZAKŁADÓW (MONETY) I ZWYCIĘSTWA ====================

    @Test
    fun `testVirtualCurrencyBetting - winner receives the bet pool`() = runTest {
        // Given: Pula zakładu wynosi 500 monet. Obaj gracze mają po 0 monet na kontach.
        val battleId = "battle_bet"
        val betAmount = 500

        coinsRepository.setBalance("p1", 0)
        coinsRepository.setBalance("p2", 0)

        challengeRepository.createBattle(FakeBattle(id = battleId, player1Id = "p1", player2Id = "p2", betAmount = betAmount, player2Hp = 10))

        // When: P1 wykonuje zadanie, zadając cios kończący (zbijający HP P2 do 0 lub poniżej)
        challengeRepository.updateBattleProgress(battleId, "p1", isDone = true)

        // Then: P1 wygrywa bitwę i zgarnia pulę
        val battle = challengeRepository.getBattle(battleId)
        val winnerBalance = coinsRepository.getBalance("p1")

        assertEquals("Status bitwy powinien zmienić się na ZAKOŃCZONA", BattleStatus.FINISHED, battle!!.status)
        assertEquals("Zwycięzca powinien otrzymać nagrodę z zakładu", betAmount, winnerBalance)
    }

    @Test
    fun `testWinCondition - game ends when player reaches 0 HP`() = runTest {
        val battleId = "battle_finish"
        challengeRepository.createBattle(FakeBattle(id = battleId, player1Id = "p1", player2Id = "p2", player2Hp = 5))

        challengeRepository.updateBattleProgress(battleId, "p1", isDone = true)

        val battle = challengeRepository.getBattle(battleId)
        assertTrue("HP pokonanego nie powinno spaść poniżej 0", battle!!.player2Hp == 0)
        assertEquals("Bitwa jest skończona", BattleStatus.FINISHED, battle.status)
        assertEquals("P1 wygrywa", "p1", battle.winnerId)
    }
}

// =========================================================================
// FAKE CLASSES - Symulują mechanikę Habit Battle i zarządzanie portfelem
// =========================================================================

enum class BattleStatus {
    ACTIVE, FINISHED
}

data class FakeBattle(
    val id: String,
    val player1Id: String,
    val player2Id: String,
    var player1Hp: Int = 100,
    var player2Hp: Int = 100,
    val betAmount: Int = 0,
    var status: BattleStatus = BattleStatus.ACTIVE,
    var winnerId: String? = null
)

class FakeCoinsRepository {
    private val balances = mutableMapOf<String, Int>()

    fun setBalance(userId: String, amount: Int) {
        balances[userId] = amount
    }

    fun getBalance(userId: String): Int {
        return balances[userId] ?: 0
    }

    fun addCoins(userId: String, amount: Int) {
        val current = getBalance(userId)
        balances[userId] = current + amount
    }
}

class FakeChallengeRepository(private val coinsRepository: FakeCoinsRepository) {
    private val battles = mutableMapOf<String, FakeBattle>()
    private val damagePerMiss = 10
    private val damagePerHit = 15

    fun createBattle(battle: FakeBattle) {
        battles[battle.id] = battle
    }

    fun getBattle(id: String): FakeBattle? = battles[id]

    fun updateBattleProgress(battleId: String, currentUserId: String, isDone: Boolean): Result<Unit> {
        val battle = battles[battleId] ?: return Result.failure(Exception("Battle not found"))

        if (battle.status == BattleStatus.FINISHED) return Result.success(Unit)

        val isPlayer1 = currentUserId == battle.player1Id

        if (isDone) {
            if (isPlayer1) {
                battle.player2Hp -= damagePerHit
            } else {
                battle.player1Hp -= damagePerHit
            }
        } else {
            if (isPlayer1) {
                battle.player1Hp -= damagePerMiss
            } else {
                battle.player2Hp -= damagePerMiss
            }
        }

        checkWinCondition(battle)

        return Result.success(Unit)
    }

    private fun checkWinCondition(battle: FakeBattle) {
        if (battle.player1Hp <= 0) {
            battle.player1Hp = 0
            battle.status = BattleStatus.FINISHED
            battle.winnerId = battle.player2Id
            coinsRepository.addCoins(battle.player2Id, battle.betAmount)
        } else if (battle.player2Hp <= 0) {
            battle.player2Hp = 0
            battle.status = BattleStatus.FINISHED
            battle.winnerId = battle.player1Id
            coinsRepository.addCoins(battle.player1Id, battle.betAmount)
        }
    }
}