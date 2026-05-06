package com.SzpontCompany.check.data.user

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val nickname: String = "",
    val isAdmin: Boolean = false,
    val avatarEmoji: String = "",
    val bgColor: String = "Mint",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastGlobalStreakDate: String = "",
    val coins: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val weeklyProgress: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
) {
    fun getXpThreshold(): Int {
        return level * 100
    }

    fun getRewardMultiplier(todayString: String): Float {
        val baseStreak = if (lastGlobalStreakDate == todayString) {
            maxOf(0, currentStreak - 1)
        } else {
            currentStreak
        }

        val multiplier = 1.0f + (baseStreak * 0.05f)
        return multiplier.coerceIn(1.0f, 2.5f)
    }

    fun calculateNewStreak(allDoneToday: Boolean, todayString: String, yesterdayString: String): User {
        var newGlobalStreak = currentStreak
        var newLastDate = lastGlobalStreakDate

        if (allDoneToday) {
            if (lastGlobalStreakDate != todayString) {
                newGlobalStreak += 1
                newLastDate = todayString
            }
        } else {
            if (lastGlobalStreakDate == todayString) {
                newGlobalStreak = maxOf(0, newGlobalStreak - 1)
                newLastDate = yesterdayString
            }
        }

        val newBestStreak = maxOf(bestStreak, newGlobalStreak)

        return copy(
            currentStreak = newGlobalStreak,
            bestStreak = newBestStreak,
            lastGlobalStreakDate = newLastDate
        )
    }

    val initials: String
        get() = name
            .trim()
            .split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
}