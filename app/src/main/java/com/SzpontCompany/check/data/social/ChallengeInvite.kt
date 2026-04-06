package com.SzpontCompany.check.data.social

data class ChallengeInvite(
    val senderName: String,
    val activityName: String,
    val durationMinutes: Int,
    val days: Int,
    val betAmount: Int
)