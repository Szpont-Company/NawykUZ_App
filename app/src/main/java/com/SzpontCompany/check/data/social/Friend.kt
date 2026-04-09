package com.SzpontCompany.check.data.social

data class Friend(
    val uid: String = "",
    val name: String = "",
    val initials: String = "",
    val xp: Int = 0,
    val online: Boolean = false,
    val lastActive: String = "",
    val mutuals: Int = 0,
    val status: String = "",
    val avatarEmoji: String = "",
    val bgColor: String = "Mint"
)

