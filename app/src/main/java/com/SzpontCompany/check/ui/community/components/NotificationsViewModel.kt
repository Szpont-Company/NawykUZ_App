package com.SzpontCompany.check.ui.community.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.notifications.Notification
import com.SzpontCompany.check.data.notifications.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    val notifications: StateFlow<List<Notification>> = repository.getUserNotifications()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadCount: StateFlow<Int> = notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            repository.deleteNotification(notificationId)
        }
    }

    // pozniej dodamy funkcje jak acceptfriendrequest itp
}