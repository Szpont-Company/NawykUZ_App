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

    // Nowe pole pod MiniBarChart (7 wartości od 0.0f do 1.0f)
    val weeklyProgress: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
) {
    val initials: String
        get() = name
            .trim()
            .split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
}