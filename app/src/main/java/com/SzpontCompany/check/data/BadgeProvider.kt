package com.SzpontCompany.check.data

import com.SzpontCompany.check.R

object BadgeProvider {
    val allBadges = listOf(
        Badge(
            id = "week_1",
            nameResId = R.string.badge_week_name,
            emoji = "🏆",
            requirementResId = R.string.badge_week_desc,
            isUnlocked = true
        ),
        Badge(
            id = "streak_21",
            nameResId = R.string.badge_streak21_name,
            emoji = "🔥",
            requirementResId = R.string.badge_streak21_desc,
            isUnlocked = true
        ),
        Badge(
            id = "steps_10k",
            nameResId = R.string.badge_steps_name,
            emoji = "🚶",
            requirementResId = R.string.badge_steps_desc,
            isUnlocked = true
        ),
        Badge(
            id = "battle_5",
            nameResId = R.string.badge_battle_name,
            emoji = "⚔️",
            requirementResId = R.string.badge_battle_desc,
            isUnlocked = true
        ),
        Badge(
            id = "streak_100",
            nameResId = R.string.badge_diamond_name,
            emoji = "💎",
            requirementResId = R.string.badge_diamond_desc,
            isUnlocked = false
        )
    )

    fun getUnlockedBadges() = allBadges.filter { it.isUnlocked }
}