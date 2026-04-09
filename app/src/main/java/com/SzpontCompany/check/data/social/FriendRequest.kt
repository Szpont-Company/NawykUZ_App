package com.SzpontCompany.check.data.social

data class FriendRequest(
    val requestId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val senderBgColor: String = "Mint",
    val receiverId: String = "",
    val status: String = "PENDING",
    val timestamp: Long = System.currentTimeMillis()
)
