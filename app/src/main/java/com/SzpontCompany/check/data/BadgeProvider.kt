package com.SzpontCompany.check.data

object BadgeProvider {
    val allBadges = listOf(
        Badge(
            id = "week_1",
            name = "Pierwszy tydzień",
            emoji = "🏆",
            requirement = "Używaj apki przez 7 dni",
            isUnlocked = true
        ),
        Badge(
            id = "streak_21",
            name = "Streak 21 dni",
            emoji = "🔥",
            requirement = "Utrzymaj passę 21 dni",
            isUnlocked = true
        ),
        Badge(
            id = "steps_10k",
            name = "10k steps",
            emoji = "🚶",
            requirement = "Zrób 10 000 kroków w jeden dzień",
            isUnlocked = true
        ),
        Badge(
            id = "battle_5",
            name = "Battle Winner",
            emoji = "⚔️",
            requirement = "Wygraj 5 bitew",
            isUnlocked = true
        ),
        Badge(
            id = "streak_100",
            name = "Diamentowy",
            emoji = "💎",
            requirement = "Streak 100 dni",
            isUnlocked = false
        )
    )

    fun getUnlockedBadges() = allBadges.filter { it.isUnlocked }
}