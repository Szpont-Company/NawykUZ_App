package com.SzpontCompany.check.data.user

import android.util.Log
import android.content.Context
import com.SzpontCompany.check.config.FirebaseConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * UserRepository - repozytorium zarządzające danymi użytkownika.
 *
 * Odpowiada za:
 * - Pobieranie i przechowywanie danych użytkownika z Firestore
 * - Obserwowanie zmian profilu użytkownika w real-time
 * - Aktualizację profilu użytkownika
 * - Zarządzanie osiągnięciami i nagrodami
 * - Operacje na koncie (rejestracja, usunięcie konta)
 * - Cache lokalny dla szybszego dostępu
 *
 * Używa wzorca Singleton do zapewnienia jednej instancji w aplikacji.
 * Obsługuje real-time synchronizację danych z Firestore.
 *
 * @since 1.0
 * @author Szpont Company
 */
class UserRepository private constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cache: UserCache
) {

    /** Listener do zmian w profilu użytkownika */
    private var userListener: ListenerRegistration? = null

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        /**
         * Pobiera lub tworzy instancję UserRepository (Singleton).
         *
         * @param context Kontekst aplikacji
         * @return Jedyna instancja UserRepository
         */
        fun getInstance(context: Context): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(
                    FirebaseAuth.getInstance(),
                    FirebaseFirestore.getInstance(),
                    UserCache(context.applicationContext)
                ).also { INSTANCE = it }
            }
        }
    }

    /** State Flow emitujący aktualne dane użytkownika */
    private val _userFlow = MutableStateFlow<User?>(value = null)

    /**
     * Publiczny State Flow dla aktualnych danych użytkownika.
     *
     * Obserwujesz ten flow aby otrzymywać aktualizacje profilu w real-time.
     */
    val userFlow: StateFlow<User?> = _userFlow.asStateFlow()

    /**
     * Rozpoczyna obserwację zmian profilu użytkownika w Firestore.
     *
     * Nasłuchuje zmian dokumentu użytkownika i aktualizuje State Flow
     * oraz cache lokalny. Automatycznie obsługuje disconnect/reconnect.
     *
     * Może być wywoływane wielokrotnie - drugi i kolejne wywołania
     * są ignorowane jeśli listener już istnieje.
     */
    fun startUserObservation() {
        val uid = auth.currentUser?.uid ?: return
        if (userListener != null) return

        userListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserRepository", "Błąd listenera: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = User(
                        uid = uid,
                        name = snapshot.getString("name") ?: "",
                        email = snapshot.getString("email") ?: "",
                        nickname = snapshot.getString("nickname") ?: "",
                        isAdmin = snapshot.getBoolean("isAdmin") ?: false,
                        avatarEmoji = snapshot.getString("avatarEmoji") ?: "",
                        bgColor = snapshot.getString("bgColor") ?: "Mint",
                        currentStreak = snapshot.getLong("currentStreak")?.toInt() ?: 0,
                        bestStreak = snapshot.getLong("bestStreak")?.toInt() ?: 0,
                        lastGlobalStreakDate = snapshot.getString("lastGlobalStreakDate") ?: "",
                        stepGoal = snapshot.getLong("stepGoal")?.toInt() ?: 8000,
                        unlockedBadges = (snapshot.get("unlockedBadges") as? List<*>)?.mapNotNull { it as? String }
                            ?: emptyList(),
                        coins = snapshot.getLong("coins")?.toInt() ?: 0,
                        xp = snapshot.getLong("xp")?.toInt() ?: 0,
                        level = snapshot.getLong("level")?.toInt() ?: 1,
                        weeklyProgress = (snapshot.get("weeklyProgress") as? List<*>)?.map {
                            (it as? Number)?.toFloat() ?: 0f
                        } ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
                    )
                    _userFlow.value = user
                    cache.save(user)
                }
            }
    }

    /**
     * Zatrzymuje obserwację zmian profilu użytkownika.
     *
     * Powinno być wywoływane przy wylogowaniu lub niszczeniu fragmentu/aktywności.
     */
    fun stopObservation() {
        userListener?.remove()
        userListener = null
    }

    /**
     * Pobiera dane aktualnego użytkownika.
     *
     * Logika:
     * 1. Jeśli użytkownik jest zalogowany, próbuje pobrać z cache
     * 2. Jeśli cache jest ważny, zwraca cache bez zapytania do Firestore
     * 3. W przeciwnym razie pobiera świeże dane z Firestore
     * 4. Jeśli nie ma zalogowanego użytkownika, próbuje użyć ostatniego cache offline
     *
     * @param forceRefresh Jeśli true, pomija cache i pobiera zawsze z Firestore
     * @return Obiekt User
     * @throws Exception Jeśli nie ma zalogowanego użytkownika i brak cache offline
     */
    suspend fun getUser(forceRefresh: Boolean = false): User {
        val firebaseUser = auth.currentUser

        if (firebaseUser == null) {
            val cachedUid = cache.getLastUid()
            val cached = cachedUid?.let { cache.get(it) }

            if (cached != null) {
                if (_userFlow.value == null) {
                    _userFlow.value = cached
                }
                return cached
            } else {
                throw Exception("No user and no cache")
            }
        }

        val uid = firebaseUser.uid

        if (!forceRefresh) {
            val cached = cache.get(uid)
            if (cached != null && cache.isValid()) {
                Log.i("UserRepository", "Using cached user data for UID: $uid")
                if (userListener == null) {
                    _userFlow.value = cached
                }
                return cached
            }
        }

        Log.i("UserRepository", "Fetching user data from Firestore for UID: $uid")
        val document = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        val user = User(
            uid = uid,
            name = document.getString("name") ?: "",
            email = document.getString("email") ?: "",
            nickname = document.getString("nickname") ?: "",
            isAdmin = document.getBoolean("isAdmin") ?: false,
            avatarEmoji = document.getString("avatarEmoji") ?: "",
            bgColor = document.getString("bgColor") ?: "Mint",
            currentStreak = document.getLong("currentStreak")?.toInt() ?: 0,
            bestStreak = document.getLong("bestStreak")?.toInt() ?: 0,
            lastGlobalStreakDate = document.getString("lastGlobalStreakDate") ?: "",

            stepGoal = document.getLong("stepGoal")?.toInt() ?: 8000,

            unlockedBadges = (document.get("unlockedBadges") as? List<*>)?.mapNotNull { it as? String }
                ?: emptyList(),
            coins = document.getLong("coins")?.toInt() ?: 0,
            xp = document.getLong("xp")?.toInt() ?: 0,
            level = document.getLong("level")?.toInt() ?: 1,
            weeklyProgress = (document.get("weeklyProgress") as? List<*>)?.map {
                (it as? Number)?.toFloat() ?: 0f
            } ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )

        cache.save(user)
        _userFlow.value = user

        return user
    }

    /**
     * Czyści cache lokalny i State Flow.
     *
     * Wywoływane przy wylogowaniu użytkownika.
     */
    fun clearCache() {
        stopObservation()
        cache.clear()
        _userFlow.value = null
    }

    /**
     * Zapisuje dane nowego użytkownika po rejestracji.
     *
     * @param uid Identyfikator Firebase
     * @param name Pełne imię
     * @param email Adres email
     */
    suspend fun saveUserData(uid: String, name: String, email: String) {
        val userData = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "nickname" to "",
            "isAdmin" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "currentStreak" to 0,
            "bestStreak" to 0,
            "lastGlobalStreakDate" to "",

            // default
            "stepGoal" to 8000,

            "coins" to 0,
            "xp" to 0,
            "level" to 1,
            "weeklyProgress" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
        )
        firestore
            .collection("users")
            .document(uid)
            .set(userData, SetOptions.merge())
            .await()

        val updatedUser = User(
            uid = uid,
            name = name,
            email = email,
            nickname = "",
            isAdmin = false,
            avatarEmoji = "",
            bgColor = "Mint",
            currentStreak = 0,
            bestStreak = 0,
            lastGlobalStreakDate = "",
            stepGoal = 8000,
            coins = 0,
            xp = 0,
            level = 1
        )
        cache.save(updatedUser)
        _userFlow.value = updatedUser
    }

    /**
     * Sprawdza czy dany pseudonim jest już zarezerwowany.
     *
     * @param nickname Pseudonim do sprawdzenia
     * @return true jeśli pseudonim jest zajęty
     */
    suspend fun isNicknameTaken(nickname: String): Boolean {
        if (nickname.isBlank()) return false
        val myUid = auth.currentUser?.uid
        val snapshot = firestore.collection("users")
            .whereEqualTo("nickname", nickname)
            .get()
            .await()

        return snapshot.documents.any { it.id != myUid }
    }

    /**
     * Aktualizuje profil użytkownika za pomocą Cloud Function.
     *
     * Funkcja backend:
     * - Aktualizuje główny dokument użytkownika
     * - Propaguje zmiany avatara i koloru do profili przyjaciół
     *
     * @param name Nowe pełne imię
     * @param nickname Nowy pseudonim
     * @param avatarEmoji Nowy avatar
     * @param bgColor Nowy kolor tła
     */
    suspend fun updateProfileViaFunctions(
        name: String,
        nickname: String,
        avatarEmoji: String,
        bgColor: String
    ) {
        val data = hashMapOf(
            "name" to name,
            "nickname" to nickname,
            "avatarEmoji" to avatarEmoji,
            "bgColor" to bgColor
        )

        FirebaseConfig.functions.getHttpsCallable("updateUserProfile").call(data).await()

        val current = _userFlow.value
        if (current != null) {
            val updatedUser = current.copy(
                name = name,
                nickname = nickname,
                avatarEmoji = avatarEmoji,
                bgColor = bgColor
            )
            cache.save(updatedUser)
            _userFlow.value = updatedUser

            try {
                val myId = updatedUser.uid
                val myFriendsSnapshot =
                    firestore.collection("users").document(myId).collection("friends").get().await()
                if (!myFriendsSnapshot.isEmpty) {
                    firestore.runBatch { batch ->
                        val friendData = mapOf(
                            "name" to name,
                            "avatarEmoji" to avatarEmoji,
                            "bgColor" to bgColor
                        )
                        for (doc in myFriendsSnapshot.documents) {
                            val friendId = doc.id
                            val theirFriendRef = firestore.collection("users").document(friendId)
                                .collection("friends").document(myId)
                            batch.update(theirFriendRef, friendData)
                        }
                    }.await()
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to sync profile update to friends", e)
            }
        }
    }

    /**
     * Usuwa konto użytkownika i wszystkie powiązane dane.
     *
     * Korzysta z Cloud Function do spełnienia wymagań RODO.
     * Wylogowuje użytkownika po usunięciu konta.
     */
    suspend fun deleteUserAccount() {
        val user = auth.currentUser ?: throw Exception("Brak zalogowanego użytkownika")

        FirebaseConfig.functions.getHttpsCallable("deleteUserAccount").call().await()

        auth.signOut()

        clearCache()
        _userFlow.value = null
    }

    /**
     * Obserwuje liczbę monet użytkownika w real-time.
     *
     * @return Flow emitujący bieżącą liczbę monet
     */
    fun getUserCoins(): Flow<Int> = callbackFlow {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            trySend(0)
            close(Exception("No user"))
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val coins = snapshot.getLong("coins")?.toInt() ?: 0
                    trySend(coins)
                } else {
                    trySend(0)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Aktualizuje streak użytkownika w Firestore.
     *
     * Wywoływane gdy zmienia się stan wykonania nawyków dla dnia.
     *
     * @param uid ID użytkownika
     * @param currentStreak Aktualny ciąg dni
     * @param bestStreak Najlepszy ciąg
     * @param lastGlobalStreakDate Data ostatniej aktualizacji streaka
     */
    suspend fun updateUserStreaks(
        uid: String,
        currentStreak: Int,
        bestStreak: Int,
        lastGlobalStreakDate: String
    ) {
        firestore.collection("users").document(uid)
            .update(
                mapOf(
                    "currentStreak" to currentStreak,
                    "bestStreak" to bestStreak,
                    "lastGlobalStreakDate" to lastGlobalStreakDate
                )
            ).await()

        val currentUserState = _userFlow.value
        if (currentUserState != null && currentUserState.uid == uid) {
            val updatedUser = currentUserState.copy(
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                lastGlobalStreakDate = lastGlobalStreakDate
            )
            _userFlow.value = updatedUser
            cache.save(updatedUser)
        }
    }

    /**
     * Przyznaje odznaką (badge) użytkownikowi.
     *
     * @param uid ID użytkownika
     * @param badgeId ID odznaki do przyznania
     */
    suspend fun claimBadge(uid: String, badgeId: String) {
        firestore.collection("users").document(uid)
            .update("unlockedBadges", FieldValue.arrayUnion(badgeId))
            .await()
    }

    /**
     * Dodaje nagrody (XP i monety) użytkownikowi.
     *
     * Automatycznie oblicza:
     * - Level up/down
     * - Maksimum progów XP
     * - Minimalna wartość monet (nie może być ujemna)
     *
     * @param uid ID użytkownika
     * @param addedXp Liczba XP do dodania
     * @param addedCoins Liczba monet do dodania (może być ujemna)
     */
    suspend fun addReward(uid: String, addedXp: Int, addedCoins: Int) {
        val userDoc = firestore.collection("users").document(uid)

        val snapshot = userDoc.get().await()
        var currentXp = snapshot.getLong("xp")?.toInt() ?: 0
        var currentLevel = snapshot.getLong("level")?.toInt() ?: 1
        var currentCoins = snapshot.getLong("coins")?.toInt() ?: 0

        currentXp += addedXp
        currentCoins = maxOf(0, currentCoins + addedCoins)

        var threshold = currentLevel * 100
        while (currentXp >= threshold) {
            currentXp -= threshold
            currentLevel++
            threshold = currentLevel * 100
            Log.d("UserRepository", "LEVEL UP! Nowy poziom: $currentLevel")
        }

        while (currentXp < 0 && currentLevel > 1) {
            currentLevel--
            val prevThreshold = currentLevel * 100
            currentXp += prevThreshold
            Log.d("UserRepository", "LEVEL DOWN! Spadek na poziom: $currentLevel")
        }

        currentXp = maxOf(0, currentXp)

        userDoc.update(
            mapOf(
                "xp" to currentXp,
                "coins" to currentCoins,
                "level" to currentLevel
            )
        ).await()
    }

    /**
     * Aktualizuje cel kroków na dzień dla użytkownika.
     *
     * @param goal Nowy cel kroków
     */
    suspend fun updateStepGoal(goal: Int) {
        val user = auth.currentUser ?: throw Exception("Brak zalogowanego użytkownika")
        val uid = user.uid
        firestore.collection("users").document(uid)
            .update("stepGoal", goal)
            .await()
    }
}
