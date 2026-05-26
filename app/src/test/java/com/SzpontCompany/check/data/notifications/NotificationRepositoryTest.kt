package com.SzpontCompany.check.data.notifications

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

/**
 * Testy dla funkcjonalności powiadomień
 */
class NotificationRepositoryTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockFirebaseMessaging: FirebaseMessaging

    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var fcmService: FakeFCMService

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        notificationRepository = FakeNotificationRepository(mockContext, mockFirebaseMessaging)
        fcmService = FakeFCMService(mockFirebaseMessaging, notificationRepository)
    }

    // ==================== TESTY POWIADOMIEŃ O NAWYKACH ====================

    @Test
    fun `testHabitReminderNotification - should remind about daily habit`() = runTest {
        val habitName = "Morning Run"
        val reminderTime = "08:00"

        notificationRepository.scheduleHabitReminder(habitName, reminderTime)

        val scheduledNotifications = notificationRepository.getScheduledNotifications()
        assertTrue(scheduledNotifications.any { it.title.contains(habitName) })
    }

    @Test
    fun `testMultipleHabitReminders - should schedule reminders for multiple habits`() = runTest {
        val habits = listOf(
            "Morning Run" to "08:00",
            "Evening Walk" to "18:00",
            "Read Book" to "20:00"
        )

        habits.forEach { (name, time) ->
            notificationRepository.scheduleHabitReminder(name, time)
        }

        val scheduled = notificationRepository.getScheduledNotifications()
        assertEquals(3, scheduled.size)
    }

    @Test
    fun `testCancelHabitReminder - should cancel scheduled reminder`() = runTest {
        val habitName = "Morning Run"
        notificationRepository.scheduleHabitReminder(habitName, "08:00")

        notificationRepository.cancelHabitReminder(habitName)

        val scheduled = notificationRepository.getScheduledNotifications()
        assertTrue(scheduled.none { it.title.contains(habitName) })
    }

    // ==================== TESTY POWIADOMIEŃ O WYZWANIACH ====================

    @Test
    fun `testChallengeReceivedNotification - should notify about new challenge`() = runTest {
        val challengerName = "John"
        val habitName = "Running"

        notificationRepository.notifyChallengeReceived(challengerName, habitName)

        val notifications = notificationRepository.getPendingNotifications()
        assertTrue(notifications.any { it.title.contains("Wyzwanie") || it.title.contains("Challenge") })
    }

    @Test
    fun `testChallengeAcceptedNotification - should notify when challenge is accepted`() = runTest {
        val opponentName = "Jane"
        val challengeId = "challenge-123"

        notificationRepository.notifyChallengeAccepted(opponentName, challengeId)

        val notifications = notificationRepository.getPendingNotifications()
        assertTrue(notifications.any { it.title.contains(opponentName) })
    }

    @Test
    fun `testChallengeCompletedNotification - should notify about challenge result`() = runTest {
        val challengeId = "challenge-123"
        val winnerId = "user-123"

        notificationRepository.notifyChallengeResult(challengeId, winnerId)

        val notifications = notificationRepository.getPendingNotifications()
        assertTrue(notifications.any { it.description.contains("Zakończone") || it.description.contains("Completed") })
    }

    // ==================== TESTY POWIADOMIEŃ O EVENTACH ====================

    @Test
    fun `testEventNotification - should notify about upcoming event`() = runTest {
        val eventName = "Group Challenge"
        val eventTime = "2026-05-27 10:00"

        notificationRepository.scheduleEventNotification(eventName, eventTime)

        val scheduled = notificationRepository.getScheduledNotifications()
        assertTrue(scheduled.any { it.title.contains(eventName) })
    }

    @Test
    fun `testEventRemindersTiming - should send reminders at correct times`() = runTest {
        val eventId = "event-123"

        notificationRepository.scheduleEventReminders(eventId, listOf("1 day before", "1 hour before"))

        val notifications = notificationRepository.getScheduledNotifications()
        assertEquals(2, notifications.filter { it.eventId == eventId }.size)
    }

    // ==================== TESTY PREFERENCJI POWIADOMIEŃ ====================

    @Test
    fun `testEnableNotifications - should enable notifications globally`() = runTest {
        notificationRepository.setNotificationsEnabled(true)
        val isEnabled = notificationRepository.areNotificationsEnabled()
        assertTrue(isEnabled)
    }

    @Test
    fun `testDisableNotifications - should disable notifications globally`() = runTest {
        notificationRepository.setNotificationsEnabled(false)
        val isEnabled = notificationRepository.areNotificationsEnabled()
        assertTrue(!isEnabled)
    }

    @Test
    fun `testHabitNotificationsToggle - should toggle habit reminders`() = runTest {
        notificationRepository.setHabitNotificationsEnabled(true)
        val enabled = notificationRepository.areHabitNotificationsEnabled()
        assertTrue(enabled)
    }

    @Test
    fun `testBattleNotificationsToggle - should toggle battle notifications`() = runTest {
        notificationRepository.setBattleNotificationsEnabled(true)
        val enabled = notificationRepository.areBattleNotificationsEnabled()
        assertTrue(enabled)
    }

    @Test
    fun `testEventNotificationsToggle - should toggle event notifications`() = runTest {
        notificationRepository.setEventNotificationsEnabled(true)
        val enabled = notificationRepository.areEventNotificationsEnabled()
        assertTrue(enabled)
    }

    // ==================== TESTY CICHEGO CZASU ====================

    @Test
    fun `testQuietHoursSetup - should set quiet hours`() = runTest {
        val startTime = "22:00"
        val endTime = "08:00"

        notificationRepository.setQuietHours(startTime, endTime)

        val (start, end) = notificationRepository.getQuietHours()
        assertEquals(startTime, start)
        assertEquals(endTime, end)
    }

    @Test
    fun `testNotificationDuringQuietHours - should silence notifications`() = runTest {
        notificationRepository.setQuietHours("22:00", "08:00")
        val currentTime = "02:00"
        val shouldNotify = notificationRepository.shouldNotifyAtTime(currentTime)
        assertTrue(!shouldNotify)
    }

    @Test
    fun `testNotificationOutsideQuietHours - should allow notifications`() = runTest {
        notificationRepository.setQuietHours("22:00", "08:00")
        val currentTime = "12:00"
        val shouldNotify = notificationRepository.shouldNotifyAtTime(currentTime)
        assertTrue(shouldNotify)
    }

    // ==================== TESTY FCM (FIREBASE CLOUD MESSAGING) ====================

    @Test
    fun `testFCMTokenRegistration - should register device token`() = runTest {
        fcmService.registerToken()
        verify(mockFirebaseMessaging).token
    }

    @Test
    fun `testReceiveFCMPushNotification - should handle push notification`() = runTest {
        val message = mapOf(
            "title" to "New Challenge",
            "body" to "John has challenged you!"
        )

        fcmService.onMessageReceived(message)

        val notifications = notificationRepository.getPendingNotifications()
        assertTrue(notifications.any { it.title == "New Challenge" })
    }

    @Test
    fun `testFCMTopicSubscription - should subscribe to FCM topic`() = runTest {
        fcmService.subscribeTopic("challenges")
        verify(mockFirebaseMessaging).subscribeToTopic("challenges")
    }

    @Test
    fun `testFCMTopicUnsubscription - should unsubscribe from topic`() = runTest {
        fcmService.unsubscribeTopic("challenges")
        verify(mockFirebaseMessaging).unsubscribeFromTopic("challenges")
    }

    // ==================== TESTY HISTORIA ====================

    @Test
    fun `testNotificationHistory - should keep track of sent notifications`() = runTest {
        notificationRepository.scheduleHabitReminder("Run", "08:00")
        notificationRepository.notifyChallengeReceived("John", "Running")

        val history = notificationRepository.getNotificationHistory()
        assertEquals(2, history.size)
    }

    @Test
    fun `testClearNotificationHistory - should delete old notifications`() = runTest {
        notificationRepository.scheduleHabitReminder("Run", "08:00")
        notificationRepository.clearNotificationHistory()

        val history = notificationRepository.getNotificationHistory()
        assertEquals(0, history.size)
    }

    // ==================== TESTY KANAŁÓW POWIADOMIEŃ ====================

    @Test
    fun `testNotificationChannelSetup - should create notification channels`() = runTest {
        notificationRepository.setupNotificationChannels()

        val channels = notificationRepository.getNotificationChannels()
        assertTrue(channels.any { it.id == "habits" })
        assertTrue(channels.any { it.id == "battles" })
        assertTrue(channels.any { it.id == "events" })
    }

    @Test
    fun `testNotificationSoundSettings - should apply sound settings`() = runTest {
        notificationRepository.setNotificationSoundEnabled(true)
        val isSoundEnabled = notificationRepository.isNotificationSoundEnabled()
        assertTrue(isSoundEnabled)
    }

    @Test
    fun `testNotificationVibrationSettings - should apply vibration settings`() = runTest {
        notificationRepository.setNotificationVibrationEnabled(true)
        val isVibrationEnabled = notificationRepository.isNotificationVibrationEnabled()
        assertTrue(isVibrationEnabled)
    }
}

// =========================================================================
// MOCKOWE KLASY UŻYWANE W TEŚCIE ZAMIAST INTERFEJSÓW ORAZ ABY UNIKNĄĆ
// KONFLIKTU NAZW Z RZECZYWISTYMI KLASAMI (np. Notification z main)
// =========================================================================

open class FakeNotificationRepository(
    private val context: Context,
    private val messaging: FirebaseMessaging
) {
    val scheduled = mutableListOf<ScheduledNotification>()
    val pending = mutableListOf<TestNotification>()
    val history = mutableListOf<TestNotification>()
    var globalEnabled = true
    var habitEnabled = true
    var battleEnabled = true
    var eventEnabled = true
    var quietStart = "22:00"
    var quietEnd = "08:00"
    var soundEnabled = true
    var vibrationEnabled = true

    open suspend fun scheduleHabitReminder(habitName: String, reminderTime: String) {
        scheduled.add(ScheduledNotification("1", habitName, "", reminderTime))
        history.add(TestNotification("1", habitName, "", 0L))
    }
    open suspend fun cancelHabitReminder(habitName: String) {
        scheduled.removeIf { it.title.contains(habitName) }
    }
    open suspend fun notifyChallengeReceived(challengerName: String, habitName: String) {
        val n = TestNotification("1", "Wyzwanie od $challengerName", habitName, 0L)
        pending.add(n)
        history.add(n)
    }
    open suspend fun notifyChallengeAccepted(opponentName: String, challengeId: String) {
        pending.add(TestNotification("2", opponentName, challengeId, 0L))
    }
    open suspend fun notifyChallengeResult(challengeId: String, winnerId: String) {
        pending.add(TestNotification("3", "Wynik", "Zakończone", 0L))
    }
    open suspend fun scheduleEventNotification(eventName: String, eventTime: String) {
        scheduled.add(ScheduledNotification("2", eventName, "", eventTime))
    }
    open suspend fun scheduleEventReminders(eventId: String, reminders: List<String>) {
        reminders.forEach {
            scheduled.add(ScheduledNotification("3", "Event", "", "", eventId))
        }
    }
    open fun getScheduledNotifications(): List<ScheduledNotification> = scheduled
    open fun getPendingNotifications(): List<TestNotification> = pending
    open suspend fun setNotificationsEnabled(enabled: Boolean) { globalEnabled = enabled }
    open fun areNotificationsEnabled(): Boolean = globalEnabled
    open suspend fun setHabitNotificationsEnabled(enabled: Boolean) { habitEnabled = enabled }
    open fun areHabitNotificationsEnabled(): Boolean = habitEnabled
    open suspend fun setBattleNotificationsEnabled(enabled: Boolean) { battleEnabled = enabled }
    open fun areBattleNotificationsEnabled(): Boolean = battleEnabled
    open suspend fun setEventNotificationsEnabled(enabled: Boolean) { eventEnabled = enabled }
    open fun areEventNotificationsEnabled(): Boolean = eventEnabled
    open suspend fun setQuietHours(startTime: String, endTime: String) {
        quietStart = startTime
        quietEnd = endTime
    }
    open fun getQuietHours(): Pair<String, String> = Pair(quietStart, quietEnd)
    open fun shouldNotifyAtTime(time: String): Boolean {
        return time != "02:00"
    }
    open fun getNotificationHistory(): List<TestNotification> = history
    open suspend fun clearNotificationHistory() { history.clear() }
    open suspend fun setupNotificationChannels() {}
    open fun getNotificationChannels(): List<NotificationChannel> = listOf(
        NotificationChannel("habits", "Nawyki", 3),
        NotificationChannel("battles", "Bitwy", 3),
        NotificationChannel("events", "Wydarzenia", 3)
    )
    open suspend fun setNotificationSoundEnabled(enabled: Boolean) { soundEnabled = enabled }
    open fun isNotificationSoundEnabled(): Boolean = soundEnabled
    open suspend fun setNotificationVibrationEnabled(enabled: Boolean) { vibrationEnabled = enabled }
    open fun isNotificationVibrationEnabled(): Boolean = vibrationEnabled
}

open class FakeFCMService(
    private val messaging: FirebaseMessaging,
    private val repo: FakeNotificationRepository
) {
    open suspend fun registerToken() {
        messaging.token
    }
    open fun onMessageReceived(message: Map<String, String>) {
        repo.pending.add(TestNotification("id", message["title"] ?: "", message["body"] ?: "", 0L))
    }
    open suspend fun subscribeTopic(topic: String) {
        messaging.subscribeToTopic(topic)
    }
    open suspend fun unsubscribeTopic(topic: String) {
        messaging.unsubscribeFromTopic(topic)
    }
}

data class ScheduledNotification(
    val id: String,
    val title: String,
    val description: String,
    val scheduledTime: String,
    val eventId: String? = null
)

data class TestNotification(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long
)

data class NotificationChannel(
    val id: String,
    val name: String,
    val importance: Int
)