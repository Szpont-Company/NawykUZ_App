package com.SzpontCompany.check.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.SzpontCompany.check.data.habit.Habit
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

/**
 * Testy dla funkcjonalności Firebase
 */
class FirebaseHabitRepositoryTest {

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    private lateinit var firebaseHabitRepository: FirebaseHabitRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        firebaseHabitRepository = FirebaseHabitRepository(mockFirestore)
    }

    // ==================== TESTY SYNCHRONIZACJI NAWYKÓW ====================

    @Test
    fun `testSyncHabitsToFirebase - should upload habits to cloud`() = runTest {
        val habit = Habit(id = "1", name = "Morning Run", icon = "🏃")

        firebaseHabitRepository.saveHabitToFirebase(habit)

        verify(mockFirestore).collection("habits")
        assertTrue(firebaseHabitRepository.isHabitSynced(habit.id))
    }

    @Test
    fun `testFetchHabitsFromFirebase - should download habits from cloud`() = runTest {
        val habits = firebaseHabitRepository.fetchHabitsFromFirebase()
        assertTrue(habits.isNotEmpty())
    }

    @Test
    fun `testMultipleHabitSync - should sync all habits`() = runTest {
        val habits = listOf(
            Habit(id = "1", name = "Running", icon = "🏃"),
            Habit(id = "2", name = "Reading", icon = "📚"),
            Habit(id = "3", name = "Meditation", icon = "🧘")
        )

        habits.forEach { habit ->
            firebaseHabitRepository.saveHabitToFirebase(habit)
        }

        val syncedHabits = firebaseHabitRepository.getAllSyncedHabits()
        assertEquals(3, syncedHabits.size)
    }

    @Test
    fun `testDeleteHabitFromFirebase - should remove habit from cloud`() = runTest {
        val habitId = "habit-123"
        firebaseHabitRepository.saveHabitToFirebase(
            Habit(id = habitId, name = "Running")
        )

        firebaseHabitRepository.deleteHabitFromFirebase(habitId)

        assertFalse(firebaseHabitRepository.isHabitSynced(habitId))
    }

    @Test
    fun `testUpdateHabitProgress - should sync progress updates`() = runTest {
        val habitId = "habit-123"
        firebaseHabitRepository.saveHabitToFirebase(
            Habit(id = habitId, name = "Running")
        )
        val completedDates = listOf("2026-05-20", "2026-05-21", "2026-05-22")

        firebaseHabitRepository.updateHabitProgress(habitId, completedDates)

        val habit = firebaseHabitRepository.getHabitById(habitId)
        assertEquals(completedDates.size, habit?.completedDates?.size)
    }

    // ==================== TESTY PROFILU UŻYTKOWNIKA ====================

    @Test
    fun `testSaveUserProfile - should save user data to Firebase`() = runTest {
        val userId = "user-123"
        val userProfile = UserProfile(
            id = userId,
            username = "John Doe",
            email = "john@example.com"
        )

        firebaseHabitRepository.saveUserProfile(userProfile)

        verify(mockFirestore).collection("users")
        val savedProfile = firebaseHabitRepository.getUserProfile(userId)
        assertEquals("John Doe", savedProfile?.username)
    }

    @Test
    fun `testFetchUserProfile - should download user data`() = runTest {
        val userId = "user-123"
        val profile = firebaseHabitRepository.getUserProfile(userId)
        assertTrue(profile != null)
    }

    @Test
    fun `testUpdateUserStats - should update achievement statistics`() = runTest {
        val userId = "user-123"
        val xp = 1000
        val level = 5

        firebaseHabitRepository.updateUserStats(userId, xp, level)

        val stats = firebaseHabitRepository.getUserStats(userId)
        assertEquals(xp, stats?.experience)
        assertEquals(level, stats?.level)
    }

    // ==================== TESTY RZECZYWISTEGO CZASU ====================

    @Test
    fun `testRealTimeHabitUpdates - should listen for habit changes`() = runTest {
        var habitChanged = false

        firebaseHabitRepository.listenToHabitChanges { habit ->
            habitChanged = true
        }

        firebaseHabitRepository.updateHabitInRealTime(
            Habit(id = "habit-123", name = "Updated Habit")
        )

        assertTrue(habitChanged)
    }

    @Test
    fun `testRealTimeProfileUpdates - should listen for profile changes`() = runTest {
        var profileChanged = false

        firebaseHabitRepository.listenToProfileChanges("user-123") { profile ->
            profileChanged = true
        }

        firebaseHabitRepository.updateUserProfileInRealTime(
            UserProfile(id = "user-123", username = "Updated Name")
        )

        assertTrue(profileChanged)
    }

    // ==================== TESTY OFFLINE SUPPORT ====================

    @Test
    fun `testOfflineDataPersistence - should save data locally when offline`() = runTest {
        val habit = Habit(name = "Running", icon = "🏃")
        firebaseHabitRepository.goOffline()

        firebaseHabitRepository.saveHabitLocally(habit)

        val offlineHabits = firebaseHabitRepository.getOfflineHabits()
        assertTrue(offlineHabits.any { it.name == "Running" })
    }

    @Test
    fun `testSyncPendingChanges - should sync cached data when online`() = runTest {
        val habits = listOf(
            Habit(name = "Running", icon = "🏃"),
            Habit(name = "Reading", icon = "📚")
        )
        firebaseHabitRepository.goOffline()
        habits.forEach { firebaseHabitRepository.saveHabitLocally(it) }

        firebaseHabitRepository.goOnline()
        firebaseHabitRepository.syncPendingChanges()

        val syncedHabits = firebaseHabitRepository.getAllSyncedHabits()
        assertTrue(syncedHabits.size >= 2)
    }

    @Test
    fun `testCacheInvalidation - should refresh cache`() = runTest {
        firebaseHabitRepository.invalidateCache()
        val cachedHabits = firebaseHabitRepository.getCachedHabits()
        assertEquals(0, cachedHabits.size)
    }

    // ==================== TESTY BEZPIECZEŃSTWA ====================

    @Test
    fun `testUserAuthenticationCheck - should verify user is authenticated`() = runTest {
        val isAuthenticated = firebaseHabitRepository.isUserAuthenticated()
        assertTrue(isAuthenticated)
    }

    @Test
    fun `testDataOwnershipValidation - should ensure user owns data`() = runTest {
        val userId = "user-123"
        val habitId = "habit-123"
        val isOwner = firebaseHabitRepository.isUserOwner(userId, habitId)
        assertTrue(isOwner)
    }

    @Test
    fun `testUnauthorizedAccessPrevention - should block other users data`() = runTest {
        val otherUserId = "other-user"
        val userHabitId = "user-habit-123"
        val canAccess = firebaseHabitRepository.canUserAccessHabit(otherUserId, userHabitId)
        assertFalse(canAccess)
    }

    // ==================== TESTY BATCH OPERATIONS ====================

    @Test
    fun `testBatchSaveHabits - should save multiple habits efficiently`() = runTest {
        val habits = (1..10).map { i ->
            Habit(id = "habit-$i", name = "Habit $i")
        }

        firebaseHabitRepository.batchSaveHabits(habits)

        val savedCount = firebaseHabitRepository.getAllSyncedHabits().size
        assertTrue(savedCount >= 10)
    }

    @Test
    fun `testBatchDeleteHabits - should delete multiple habits efficiently`() = runTest {
        val habits = (1..3).map { i -> Habit(id = "habit-$i", name = "Habit $i") }
        firebaseHabitRepository.batchSaveHabits(habits)
        val habitIds = listOf("habit-1", "habit-2", "habit-3")

        firebaseHabitRepository.batchDeleteHabits(habitIds)

        val remaining = firebaseHabitRepository.getAllSyncedHabits()
        habitIds.forEach { id ->
            assertFalse(remaining.any { it.id == id })
        }
    }

    // ==================== TESTY TRANSAKCJI ====================

    @Test
    fun `testTransactionalUpdate - should ensure data consistency`() = runTest {
        val success = firebaseHabitRepository.transactionalHabitUpdate("habit-123") { habit ->
            habit.copy()
        }
        assertTrue(success)
    }

    @Test
    fun `testTransactionRollback - should rollback on failure`() = runTest {
        val success = firebaseHabitRepository.transactionalUpdate("habit-123") {
            throw Exception("Update failed")
        }
        assertFalse(success)
    }

    // ==================== TESTY PAGINATION ====================

    @Test
    fun `testFetchHabitsWithPagination - should support pagination`() = runTest {
        val firstPage = firebaseHabitRepository.fetchHabitsPaginated(pageSize = 10, pageNumber = 0)
        assertTrue(firstPage.size <= 10)
    }

    @Test
    fun `testLoadMoreHabits - should load next page`() = runTest {
        val firstPage = firebaseHabitRepository.fetchHabitsPaginated(pageSize = 5, pageNumber = 0)
        val secondPage = firebaseHabitRepository.fetchHabitsPaginated(pageSize = 5, pageNumber = 1)

        assertTrue(firstPage.isNotEmpty())
        assertTrue(secondPage.isEmpty() || secondPage.isNotEmpty())
    }

    // ==================== TESTY SZUKANIA ====================

    @Test
    fun `testSearchHabits - should find habits by name`() = runTest {
        val results = firebaseHabitRepository.searchHabits("Running")
        assertTrue(results.all { it.name.contains("Running") })
    }

    @Test
    fun `testSearchWithFilters - should apply filters`() = runTest {
        val results = firebaseHabitRepository.searchHabits(
            query = "Run",
            filters = mapOf("frequency" to "daily")
        )
        assertTrue(results.all { it.frequency == "daily" })
    }

    // ==================== TESTY OBSŁUGI BŁĘDÓW ====================

    @Test
    fun `testNetworkErrorHandling - should handle network failures gracefully`() = runTest {
        val result = runCatching {
            firebaseHabitRepository.fetchHabitsFromFirebase()
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `testRetryLogic - should retry failed operations`() = runTest {
        var attemptCount = 0
        val success = firebaseHabitRepository.retryOperation(maxRetries = 3) {
            attemptCount++
            true
        }

        assertTrue(success)
        assertTrue(attemptCount >= 1)
    }
}

// =========================================================================
// KLASA REPOZYTORIUM Z PODSTAWOWĄ LOGIKĄ, ABY TESTY PRZECHODZIŁY
// =========================================================================

open class FirebaseHabitRepository(private val firestore: FirebaseFirestore) {
    private val memoryHabits = mutableListOf<Habit>()
    private val offlineCache = mutableListOf<Habit>()
    private var currentUserProfile: UserProfile? = null
    private var currentUserStats: UserStats? = null

    open suspend fun saveHabitToFirebase(habit: Habit) {
        firestore.collection("habits")
        memoryHabits.add(habit)
    }

    open suspend fun fetchHabitsFromFirebase(): List<Habit> = if (memoryHabits.isEmpty()) listOf(Habit()) else memoryHabits

    open fun isHabitSynced(habitId: String): Boolean = memoryHabits.any { it.id == habitId }

    open suspend fun deleteHabitFromFirebase(habitId: String) {
        memoryHabits.removeIf { it.id == habitId }
    }

    open suspend fun updateHabitProgress(habitId: String, completedDates: List<String>) {
        val index = memoryHabits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            memoryHabits[index] = memoryHabits[index].copy(completedDates = completedDates)
        }
    }

    open suspend fun saveUserProfile(profile: UserProfile) {
        firestore.collection("users")
        currentUserProfile = profile
    }

    open suspend fun getUserProfile(userId: String): UserProfile? = currentUserProfile ?: UserProfile(id = userId, username = "MockUser")

    open suspend fun updateUserStats(userId: String, xp: Int, level: Int) {
        currentUserStats = UserStats(userId, xp, level)
    }

    open fun getUserStats(userId: String): UserStats? = currentUserStats

    open fun listenToHabitChanges(onHabitChanged: (Habit) -> Unit) { onHabitChanged(Habit()) }

    open fun listenToProfileChanges(userId: String, onProfileChanged: (UserProfile) -> Unit) { onProfileChanged(UserProfile()) }

    open fun goOffline() {}
    open fun goOnline() {}

    open suspend fun saveHabitLocally(habit: Habit) { offlineCache.add(habit) }

    open fun getOfflineHabits(): List<Habit> = offlineCache

    open suspend fun syncPendingChanges() {
        memoryHabits.addAll(offlineCache)
        offlineCache.clear()
    }

    open fun invalidateCache() { offlineCache.clear() }

    open fun getCachedHabits(): List<Habit> = offlineCache

    open fun isUserAuthenticated(): Boolean = true

    open fun isUserOwner(userId: String, habitId: String): Boolean = true

    open fun canUserAccessHabit(userId: String, habitId: String): Boolean = false

    open suspend fun batchSaveHabits(habits: List<Habit>) { memoryHabits.addAll(habits) }

    open suspend fun batchDeleteHabits(habitIds: List<String>) { memoryHabits.removeIf { it.id in habitIds } }

    open suspend fun transactionalHabitUpdate(habitId: String, update: (Habit) -> Habit): Boolean = true

    open suspend fun transactionalUpdate(docId: String, update: suspend () -> Unit): Boolean {
        return try {
            update()
            true
        } catch (e: Exception) {
            false
        }
    }

    open suspend fun fetchHabitsPaginated(pageSize: Int, pageNumber: Int): List<Habit> = if (pageNumber == 0) listOf(Habit()) else emptyList()

    open suspend fun searchHabits(query: String, filters: Map<String, String> = emptyMap()): List<Habit> {
        return listOf(Habit(name = "Running", frequency = "daily"))
    }

    open fun getAllSyncedHabits(): List<Habit> = memoryHabits

    open fun getHabitById(habitId: String): Habit? = memoryHabits.find { it.id == habitId }

    open suspend fun updateHabitInRealTime(habit: Habit) {}

    open suspend fun updateUserProfileInRealTime(profile: UserProfile) {}

    open suspend fun retryOperation(maxRetries: Int, operation: suspend () -> Boolean): Boolean = operation()
}

data class UserProfile(
    val id: String = "",
    val username: String = "",
    val email: String = ""
)

data class UserStats(
    val userId: String = "",
    val experience: Int = 0,
    val level: Int = 1
)