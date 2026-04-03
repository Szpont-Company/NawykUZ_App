package com.SzpontCompany.check.data

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    CHALLENGE, FRIEND, REWARD, SYSTEM
}
