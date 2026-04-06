package com.SzpontCompany.check.data

data class Friend(
    val name: String,
    val initials: String,
    val xp: Int,
    val online: Boolean = false,
    val lastActive: String = "",
    val mutuals: Int = 0,
    val status: String = ""
)

