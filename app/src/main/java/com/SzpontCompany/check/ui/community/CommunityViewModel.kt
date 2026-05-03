package com.SzpontCompany.check.ui.community

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.data.habit.HabitRepository
import com.SzpontCompany.check.data.social.*
import com.SzpontCompany.check.data.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommunityViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val challengeRepository: ChallengeRepository = ChallengeRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val habitRepository = HabitRepository()

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

    private val _battles = MutableStateFlow<List<Battle>>(emptyList())
    val battles: StateFlow<List<Battle>> = _battles.asStateFlow()

    private val _incomingInvites = MutableStateFlow<List<ChallengeInvite>>(emptyList())
    val incomingInvites: StateFlow<List<ChallengeInvite>> = _incomingInvites.asStateFlow()

    private val _events = MutableStateFlow(initialEvents)
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _rankingEntries = MutableStateFlow(initialRanking)
    val rankingEntries: StateFlow<List<RankingEntry>> = _rankingEntries.asStateFlow()

    init {
        loadIncomingInvites()
        loadActiveBattles()
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

    private fun loadActiveBattles() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                challengeRepository.getActiveBattlesForUser(currentUserId).collect { activeBattles ->
                    _battles.value = activeBattles
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    fun sendChallenge(currentUser: User?, friend: Friend, template: ChallengeTemplate, betAmount: Int) {
        val currentUserId = currentUser?.uid ?: return

        val newInvite = ChallengeInvite(
            senderId = currentUserId,
            senderName = currentUser.name.ifEmpty { "Nieznany" },
            senderInitials = currentUser.initials,
            senderEmoji = currentUser.avatarEmoji,
            senderBgColor = currentUser.bgColor,
            receiverId = friend.uid,
            habitName = template.title,
            stake = betAmount
        )

        viewModelScope.launch {
            challengeRepository.sendInvite(newInvite)
        }
    }


    fun acceptInvite(invite: ChallengeInvite, currentUserName: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                challengeRepository.updateInviteStatus(invite.id, "ACCEPTED")

                val newBattle = Battle(
                    title = invite.habitName,
                    betAmount = invite.stake,
                    totalDays = 7,
                    participants = listOf(invite.senderId, currentUserId),

                    player1Id = invite.senderId,
                    player1Name = invite.senderName,

                    player2Id = currentUserId,
                    player2Name = currentUserName
                )

                val result = challengeRepository.createBattle(newBattle)
                val generatedBattleId = result.getOrNull() ?: return@launch

                val myBattleHabit = Habit(
                    name = invite.habitName,
                    icon = "⚔️",
                    colorName = "Coral",
                    frequency = "codziennie",
                    selectedDays = listOf("poniedziałek", "wtorek", "środa", "czwartek", "piątek", "sobota", "niedziela"),
                    timesPerWeek = 7,
                    battleId = generatedBattleId,
                    opponentName = invite.senderName,
                    isActive = true
                )
                habitRepository.addHabit(myBattleHabit)

                val opponentBattleHabit = myBattleHabit.copy(
                    opponentName = currentUserName
                )
                habitRepository.addHabitForUser(invite.senderId, opponentBattleHabit)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectInvite(invite: ChallengeInvite) {
        viewModelScope.launch {
            challengeRepository.updateInviteStatus(invite.id, "REJECTED")
        }
    }

    fun toggleBattleDone(battle: Battle, isDone: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return
        val todayString = java.time.LocalDate.now().toString()
        val yesterdayString = java.time.LocalDate.now().minusDays(1).toString()

        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(currentUserId)
                val habitsRef = userRef.collection("habits")

                val querySnapshot = habitsRef.whereEqualTo("battleId", battle.id).get().await()

                if (querySnapshot.isEmpty) {
                    challengeRepository.updateBattleProgress(battle.id, currentUserId, isDone)
                    if (isDone) habitRepository.earnCoinsCloud("habit_done")
                } else {
                    val doc = querySnapshot.documents.first()
                    val habit = doc.toObject(Habit::class.java) ?: return@launch
                    val habitId = doc.id

                    val newDates = if (isDone) {
                        (habit.completedDates + todayString).distinct()
                    } else {
                        habit.completedDates.filter { it != todayString }
                    }
                    val newHabitStreak =
                        if (isDone) habit.streak + 1 else maxOf(0, habit.streak - 1)

                    val allHabitsSnapshot = habitsRef.get().await()
                    val updatedHabits = allHabitsSnapshot.mapNotNull { snapshot ->
                        val h = snapshot.toObject(Habit::class.java) ?: return@mapNotNull null
                        if (snapshot.id == habitId) h.copy(
                            completedDates = newDates,
                            streak = newHabitStreak
                        ) else h
                    }

                    val allDoneToday = updatedHabits.isNotEmpty() && updatedHabits.all {
                        it.completedDates.contains(todayString)
                    }

                    val userSnapshot = userRef.get().await()
                    val userObj = userSnapshot.toObject(User::class.java)

                    var userUpdates: Map<String, Any>? = null

                    if (userObj != null) {
                        var newGlobalStreak = userObj.currentStreak
                        var newLastDate = userObj.lastGlobalStreakDate

                        if (allDoneToday) {
                            if (userObj.lastGlobalStreakDate != todayString) {
                                newGlobalStreak += 1
                                newLastDate = todayString
                            }
                        } else {
                            if (userObj.lastGlobalStreakDate == todayString) {
                                newGlobalStreak = maxOf(0, newGlobalStreak - 1)
                                newLastDate = yesterdayString
                            }
                        }

                        val newBestStreak = maxOf(userObj.bestStreak, newGlobalStreak)

                        if (newGlobalStreak != userObj.currentStreak || newLastDate != userObj.lastGlobalStreakDate) {
                            userUpdates = mapOf(
                                "currentStreak" to newGlobalStreak,
                                "bestStreak" to newBestStreak,
                                "lastGlobalStreakDate" to newLastDate
                            )
                        }
                    }

                    challengeRepository.updateBattleProgress(
                        battleId = battle.id,
                        currentUserId = currentUserId,
                        isDone = isDone,
                        habitId = habitId,
                        newDates = newDates,
                        newHabitStreak = newHabitStreak
                    )

                    if (userUpdates != null) {
                        userRef.update(userUpdates).await()
                    }

                    if (isDone) habitRepository.earnCoinsCloud("habit_done")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acknowledgeBattle(battle: Battle) {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val isPlayer1 = battle.player1Id == currentUserId
                val isWinner = battle.winnerId == currentUserId

                val ackField = if (isPlayer1) "player1Acknowledged" else "player2Acknowledged"
                db.collection("battles").document(battle.id).update(ackField, true).await()

                if (isWinner) {
                    val reward = battle.betAmount * 2
                    habitRepository.earnCoinsCloud("battle_win", reward)
                }

                val habitsRef = db.collection("users").document(currentUserId).collection("habits")
                val snapshot = habitsRef.whereEqualTo("battleId", battle.id).get().await()
                if (!snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        doc.reference.delete().await()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
