package com.SzpontCompany.check.data.notifications

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class NotificationType {
    FRIEND_REQUEST,
    FRIEND_ACCEPTED,
    BATTLE_INVITE,
    BATTLE_ACCEPTED,
    BATTLE_RESULT,
    MESSAGE,
    SYSTEM
}

data class Notification(
    val id: String = "",
    val type: String = NotificationType.SYSTEM.name,
    val title: String = "",
    val message: String = "",
    val senderId: String? = null,
    @ServerTimestamp val createdAt: Date? = null,
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,
    val relatedEntityId: String? = null
)