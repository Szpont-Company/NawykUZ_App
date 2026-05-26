package com.SzpontCompany.check.data.badges

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testy dla funkcjonalności osiągnięć (odznaki)
 * Punkt 4: Zliczanie kroków -> osiągnięcia
 */
class BadgeProviderTest {

    @Test
    fun `testEvaluateBadges - should unlock week_1 badge for 7 day streak`() {
        // Given
        val streak = 7
        val wins = 0

        // When
        val badges = BadgeProvider.evaluateBadges(bestStreak = streak, battlesWon = wins)

        // Then
        val weekBadge = badges.find { it.id == "week_1" }
        assertTrue("Badge week_1 should be unlocked for 7 day streak", weekBadge?.isUnlocked == true)
    }

    @Test
    fun `testEvaluateBadges - should unlock streak_21 badge for 21 day streak`() {
        // Given
        val streak = 21
        val wins = 0

        // When
        val badges = BadgeProvider.evaluateBadges(bestStreak = streak, battlesWon = wins)

        // Then
        val streak21Badge = badges.find { it.id == "streak_21" }
        assertTrue("Badge streak_21 should be unlocked for 21 day streak", streak21Badge?.isUnlocked == true)
    }

    @Test
    fun `testEvaluateBadges - should unlock battle_5 badge for 5 wins`() {
        // Given
        val streak = 0
        val wins = 5

        // When
        val badges = BadgeProvider.evaluateBadges(bestStreak = streak, battlesWon = wins)

        // Then
        val battleBadge = badges.find { it.id == "battle_5" }
        assertTrue("Badge battle_5 should be unlocked for 5 wins", battleBadge?.isUnlocked == true)
    }

    @Test
    fun `testEvaluateBadges - should not unlock badges if requirements not met`() {
        // Given
        val streak = 2
        val wins = 1

        // When
        val badges = BadgeProvider.evaluateBadges(bestStreak = streak, battlesWon = wins)

        // Then
        assertFalse(badges.find { it.id == "week_1" }?.isUnlocked ?: true)
        assertFalse(badges.find { it.id == "battle_5" }?.isUnlocked ?: true)
    }

    @Test
    fun `testBadgeSorting - unlocked badges should be at the top`() {
        // Given
        val streak = 10
        val wins = 0

        // When
        val badges = BadgeProvider.evaluateBadges(bestStreak = streak, battlesWon = wins)

        // Then
        assertTrue("First badge in list should be unlocked", badges.first().isUnlocked)
    }
}