package com.SzpontCompany.check.data.badges

import com.SzpontCompany.check.R

object BadgeProvider {
    val allBadges = listOf(
        Badge(
            id = "week_1",
            nameResId = R.string.badge_week_name,
            emoji = "🏆",
            requirementResId = R.string.badge_week_desc,
            isUnlocked = false
        ),
        Badge(
            id = "streak_21",
            nameResId = R.string.badge_streak21_name,
            emoji = "🔥",
            requirementResId = R.string.badge_streak21_desc,
            isUnlocked = false
        ),
        Badge(
            id = "steps_10k",
            nameResId = R.string.badge_steps_name,
            emoji = "🚶",
            requirementResId = R.string.badge_steps_desc,
            isUnlocked = false
        ),
        Badge(
            id = "battle_5",
            nameResId = R.string.badge_battle_name,
            emoji = "⚔️",
            requirementResId = R.string.badge_battle_desc,
            isUnlocked = false
        ),
        Badge(
            id = "streak_100",
            nameResId = R.string.badge_diamond_name,
            emoji = "💎",
            requirementResId = R.string.badge_diamond_desc,
            isUnlocked = false
        )
    )

    fun evaluateBadges(bestStreak: Int, battlesWon: Int): List<Badge> {
        return allBadges.map { badge ->
            val unlocked = when (badge.id) {
                "week_1" -> bestStreak >= 7
                "streak_21" -> bestStreak >= 21
                "streak_100" -> bestStreak >= 100
                "battle_5" -> battlesWon >= 5
                // TODO: Dodać prawdziwą logikę dla kroków, np. na podstawie danych z Google Fit
                "steps_10k" -> true
                else -> false
            }
            badge.copy(isUnlocked = unlocked)
        }.sortedByDescending { it.isUnlocked }
    }
}