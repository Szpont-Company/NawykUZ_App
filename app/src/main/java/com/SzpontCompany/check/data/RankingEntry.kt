package com.SzpontCompany.check.data

import androidx.compose.ui.graphics.Color

data class RankingEntry(
    val rank: Int,
    val name: String,
    val initials: String,
    val xp: Int,
    val avatarColor: Color,
    val isMe: Boolean = false
)
