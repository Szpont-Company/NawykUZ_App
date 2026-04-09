package com.SzpontCompany.check.ui.community

import androidx.lifecycle.ViewModel
import com.SzpontCompany.check.data.social.NotificationItem
import com.SzpontCompany.check.data.social.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CommunityViewModel : ViewModel() {

    private val initialNotifications = listOf(
        NotificationItem(
            id = "1",
            title = "Nowe wyzwanie!",
            message = "Tomek K. zaprasza Cię do bitwy: Bieganie 30 min.",
            timeAgo = "5 min temu",
            type = NotificationType.CHALLENGE,
            isRead = false,
            requiresAction = true
        ),
        NotificationItem(
            id = "4",
            title = "Nowy znajomy",
            message = "Ania W. chce dodać Cię do znajomych. Możecie teraz rywalizować!",
            timeAgo = "1 godz. temu",
            type = NotificationType.FRIEND,
            isRead = false,
            requiresAction = true
        ),
        NotificationItem(
            id = "2",
            title = "Zdobyto dzisiaj odznakę!",
            message = "Zdobyto odznakę \"Streak 21 dni\" 🔥. Wymóg: Utrzymaj passę 21 dni.",
            timeAgo = "2 godz. temu",
            type = NotificationType.REWARD,
            isRead = true,
            requiresAction = false
        ),
        NotificationItem(
            id = "3",
            title = "Globalny event niedługo!",
            message = "Rozpoczął się nowy Globalny Marsz. Dołącz do reszty społeczności!",
            timeAgo = "1 dzień temu",
            type = NotificationType.SYSTEM,
            isRead = true,
            requiresAction = false
        )
    )

    private val _notifications = MutableStateFlow(initialNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun markAsRead(notificationId: String) {
        _notifications.update { list ->
            list.map {
                if (it.id == notificationId) it.copy(isRead = true) else it
            }
        }
    }

    fun markAllAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun acceptAction(notificationId: String) {
        // W przyszłości: wywołanie do bazy danych (zmiana statusu zaproszenia, dodanie do znajomych)

        // Na razie usuwamy powiadomienie z widoku, symulując sukces backendu
        _notifications.update { list ->
            list.filterNot { it.id == notificationId }
        }
    }

    fun declineAction(notificationId: String) {
        _notifications.update { list ->
            list.filterNot { it.id == notificationId }
        }
    }
}

