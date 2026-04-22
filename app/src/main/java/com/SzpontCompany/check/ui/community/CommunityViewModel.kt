package com.SzpontCompany.check.ui.community

import androidx.lifecycle.ViewModel
import com.SzpontCompany.check.data.social.Battle
import com.SzpontCompany.check.data.social.ChallengeInvite
import com.SzpontCompany.check.data.social.NotificationItem
import com.SzpontCompany.check.data.social.NotificationType
import com.SzpontCompany.check.data.social.RankingEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.social.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunityViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val challengeRepository: ChallengeRepository = ChallengeRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    // symulacja danych z backendu (mocki)
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

    private val initialBattles = listOf(
        Battle(
            title = "Codzienny spacer", daysLeft = 3,
            myDays = 6, totalDays = 7, myHp = 95,
            opponentName = "Kacper M.", opponentDays = 5, opponentHp = 60,
            opponentCompleted = false, betAmount = 50, endDate = "23 mar",
            isLosingWarning = false, isDoneToday = true
        ),
        Battle(
            title = "Czytanie 20 min", daysLeft = 5,
            myDays = 4, totalDays = 7, myHp = 55,
            opponentName = "Ania W.", opponentDays = 7, opponentHp = 100,
            opponentCompleted = true, betAmount = 100, endDate = null,
            isLosingWarning = true, isDoneToday = false
        )
    )


    private val initialEvents = listOf(
        Event(
            title = "Globalny Marsz Marca", subtitle = "Łącznie 1 000 000 kroków",
            badgeText = "Global", themeColor = Color(0xFF00BFA5),
            progress = 0.67f, progressText = "672 450 / 1 000 000 kroków",
            timeText = "12 dni", participantsCount = "8 431 uczestników", buttonText = "Dołącz"
        ),
        Event(
            title = "Tydzień Czytania", subtitle = "7 dni z rzędu min. 20 min",
            badgeText = "Społeczność", themeColor = Color(0xFF8C9EFF),
            progress = 0.43f, progressText = "3 / 7 dni ukończono",
            timeText = "4 dni", rewardHighlight = "Odznaka \"Bookworm\" + 300 monet"
        ),
        Event(
            title = "Wiosenny Sprint", subtitle = "Rusza za 3 dni!", isSubtitleColored = true,
            badgeText = "Wkrótce", themeColor = Color(0xFFF57C00),
            description = "30-dniowe wyzwanie aktywności fizycznej.\nNagroda: ekskluzywna odznaka + 500 monet.",
            buttonText = "Przypomnij mi"
        )
    )

    private val initialRanking = listOf(
        RankingEntry(rank = 1, name = "Piotr K.", initials = "PK", xp = 4200, avatarColor = Color(0xFFD8912A)),
        RankingEntry(rank = 2, name = "Ania S.", initials = "AS", xp = 3800, avatarColor = Color(0xFF5E35B1)),
        RankingEntry(
            rank = 3,
            name = "Marek J.",
            initials = "MJ",
            xp = 3100,
            avatarColor = Color(0xFFE24B4A)
        ),
        RankingEntry(rank = 14, name = "Ty", initials = "TY", xp = 1240, avatarColor = Color.Gray, isMe = true)
    )

    //--- state flow ---

    private val _notifications = MutableStateFlow(initialNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _battles = MutableStateFlow(initialBattles)
    val battles: StateFlow<List<Battle>> = _battles.asStateFlow()

    private val _incomingInvites = MutableStateFlow<List<ChallengeInvite>>(emptyList())
    val incomingInvites: StateFlow<List<ChallengeInvite>> = _incomingInvites.asStateFlow()

    private val _events = MutableStateFlow(initialEvents)
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _rankingEntries = MutableStateFlow(initialRanking)
    val rankingEntries: StateFlow<List<RankingEntry>> = _rankingEntries.asStateFlow()

    init {
        loadIncomingInvites()
    }

    // ---- LOGIK ---
    fun markAsRead(notificationId: String) {
        _notifications.update { list -> list.map { if (it.id == notificationId) it.copy(isRead = true) else it } }
    }

    fun markAllAsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
    }

    fun acceptAction(notificationId: String) {
        _notifications.update { list -> list.filterNot { it.id == notificationId } }
    }

    fun declineAction(notificationId: String) {
        _notifications.update { list -> list.filterNot { it.id == notificationId } }
    }

    private fun loadIncomingInvites() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                challengeRepository.getPendingInvitesForUser(currentUserId).collect { invites ->
                    _incomingInvites.value = invites
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendChallenge(friend: Friend, template: ChallengeTemplate, betAmount: Int) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUserName = auth.currentUser?.displayName ?: "Nieznany"

        val newInvite = ChallengeInvite(
            senderId = currentUserId,
            senderName = currentUserName,
            receiverId = friend.uid,
            habitName = template.title,
            stake = betAmount
        )

        viewModelScope.launch {
            challengeRepository.sendInvite(newInvite)
        }
    }

}

