package com.SzpontCompany.check.data.user

import android.content.Context
import androidx.core.content.edit

/**
 * UserCache - cache lokalny dla danych użytkownika.
 *
 * Przechowuje podstawowe informacje o użytkowniku w SharedPreferences
 * dla szybkiego dostępu offline i zmniejszenia liczby zapytań do Firebase.
 *
 * Cachuje następujące dane:
 * - uid: Identyfikator użytkownika
 * - name: Pełne imię
 * - email: Adres email
 * - nickname: Pseudonim w grze
 * - isAdmin: Status administratora
 * - avatarEmoji: Avatar emoji
 * - bgColor: Kolor tła profilu
 * - updatedAt: Timestamp ostatniej aktualizacji (TTL = 6 godzin)
 *
 * Unieważnia cache automatycznie po 6 godzinach bez dostępu do Firestore.
 *
 * @since 1.0
 * @author Szpont Company
 */
class UserCache(private val context: Context) {
    /** SharedPreferences do przechowywania cache użytkownika */
    private val prefs = context.getSharedPreferences("user_cache", Context.MODE_PRIVATE)

    /**
     * Zapisuje dane użytkownika do cache lokalnego.
     *
     * @param user Obiekt User do zapisania
     */
    fun save(user: User) {
        prefs.edit {
            putString("uid", user.uid)
                .putString("name", user.name)
                .putString("email", user.email)
                .putString("nickname", user.nickname)
                .putBoolean("isAdmin", user.isAdmin)
                .putString("avatarEmoji", user.avatarEmoji)
                .putString("bgColor", user.bgColor)
                .putLong("updatedAt", System.currentTimeMillis())
        }
    }

    /**
     * Pobiera dane użytkownika z cache.
     *
     * Zwraca null jeśli:
     * - Nie ma danych w cache
     * - UID nie zgadza się z aktualnie zalogowanym użytkownikiem
     *
     * @param uid Identyfikator użytkownika do pobrania
     * @return Obiekt User lub null jeśli nie znaleziono
     */
    fun get(uid: String) : User? {
        val cachedUid = prefs.getString("uid", null) ?: return null
        if (cachedUid != uid) return null
        val name = prefs.getString("name", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val nickname = prefs.getString("nickname", "") ?: ""
        val isAdmin = prefs.getBoolean("isAdmin", false)
        val avatarEmoji = prefs.getString("avatarEmoji", "") ?: ""
        val bgColor = prefs.getString("bgColor", "Mint") ?: "Mint"

        return User(uid, name, email, nickname, isAdmin, avatarEmoji, bgColor)
    }

    /**
     * Sprawdza czy cache jest jeszcze ważny.
     *
     * Cache jest ważny przez 6 godzin od ostatniej aktualizacji.
     * Po tym czasie powinien być odświeżony z Firestore.
     *
     * @return true jeśli cache jest ważny, false jeśli wygasł
     */
    fun isValid() : Boolean {
        val updatedAt = prefs.getLong("updatedAt", 0)
        val ttl = 6 * 60 * 60 * 1000 // 6 godzin w milisekundach
        return System.currentTimeMillis() - updatedAt < ttl
    }

    /**
     * Pobiera ostatni UID użytkownika z cache.
     *
     * Stosowany do odtworzenia sesji offline.
     *
     * @return UID ostatniego zalogowanego użytkownika lub null
     */
    fun getLastUid(): String? {
        return prefs.getString("uid", null)
    }

    /**
     * Czyści całą zawartość cache.
     *
     * Wywoływane przy wylogowaniu użytkownika.
     */
    fun clear() {
        prefs.edit {clear()}
    }
}