package com.SzpontCompany.check.data.user

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val nickname: String = "",
    val isAdmin: Boolean = false,
    val avatarEmoji: String = "",
    val bgColor: String = "Mint"
) {
    val initials: String
        get() = name
            .trim()
            .split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
}