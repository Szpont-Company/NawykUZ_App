package com.SzpontCompany.check.data.social

data class ChallengeInvite(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderInitials: String = "",
    val senderEmoji: String = "",
    val senderBgColor: String = "Mint",
    val receiverId: String = "",
    val habitName: String = "",
    val habitColorName: String = "Mint", // Dodano pole koloru nawyku
    val challengeType: String = "", // Tu przechowujemy emoji nawyku
    val stake: Int = 0,
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "Mint", "", "", "Mint", "BATTLE", 0, "PENDING", 0)
}