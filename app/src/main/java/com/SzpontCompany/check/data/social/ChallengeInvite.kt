package com.SzpontCompany.check.data.social

data class ChallengeInvite(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val habitName: String = "",
    val challengeType: String = "",
    val stake: Int = 0,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "", 0, "PENDING", 0)
}