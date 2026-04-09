package com.SzpontCompany.check.data.chat

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val type: String = "TEXT",
    val isRead: Boolean = false
)
