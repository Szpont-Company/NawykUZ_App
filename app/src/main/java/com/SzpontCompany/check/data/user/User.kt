package com.SzpontCompany.check.data.user

/**
 * User - data class reprezentująca użytkownika aplikacji Check.
 *
 * Zawiera wszystkie istotne informacje o profilu użytkownika, statystyce
 * postępów, systemie osiągnięć i nagrodzenia (XP, monety, poziomy).
 *
 * Pola podstawowe:
 * - uid: Unikalny identyfikator użytkownika z Firebase
 * - name: Pełne imię i nazwisko
 * - email: Adres email
 * - nickname: Pseudonim widoczny dla innych użytkowników
 * - avatarEmoji: Emoji wybrane jako avatar
 * - bgColor: Kolor tła profilu
 *
 * Pola progress i statystyka:
 * - currentStreak: Aktualny ciąg dni z wykonanymi nawykami
 * - bestStreak: Najlepszy dotychczasowy ciąg dni
 * - stepGoal: Dzienny cel kroków
 * - weeklyProgress: Lista progresji przez 7 dni tygodnia
 *
 * System nagród:
 * - coins: Liczba zgromadzonych monet (waluta gry)
 * - xp: Doświadczenie
 * - level: Poziom użytkownika
 * - unlockedBadges: Lista ID zgromadzonych odznak
 *
 * @property uid Unikalny identyfikator z Firebase
 * @property name Pełne imię użytkownika
 * @property email Adres email
 * @property nickname Pseudonim w grze
 * @property isAdmin Flaga czy użytkownik jest administratorem
 * @property avatarEmoji Emoji użytkownika
 * @property bgColor Kolor tła profilu (Mint, Indigo, Coral, Sky, Rose, Cactus, Amber, Crimson)
 * @property currentStreak Aktualny ciąg dni z wykonanymi wszystkimi nawykami
 * @property bestStreak Najlepszy ciąg
 * @property lastGlobalStreakDate Ostatnia data, w której streak był aktualizowany
 * @property stepGoal Cel kroków na dzień
 * @property unlockedBadges Lista ID odznak
 * @property coins Liczba monet
 * @property xp Doświadczenie
 * @property level Poziom
 * @property weeklyProgress Postęp na każdy dzień tygodnia
 *
 * @since 1.0
 * @author Szpont Company
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val nickname: String = "",
    val isAdmin: Boolean = false,
    val avatarEmoji: String = "",
    val bgColor: String = "Mint",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastGlobalStreakDate: String = "",

    val stepGoal: Int = 8000,
  
    val unlockedBadges: List<String> = emptyList(),
    val coins: Int = 0,
    val xp: Int = 0,
    val level: Int = 1,
    val weeklyProgress: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
) {
    /**
     * Oblicza próg doświadczenia wymagany do awansu na następny poziom.
     *
     * Formuła: threshold = level * 100
     *
     * @return Liczba XP wymaganych do osiągnięcia następnego poziomu
     */
    fun getXpThreshold(): Int {
        return level * 100
    }

    /**
     * Oblicza mnożnik nagród bazując na aktualnym ciągu dni.
     *
     * Im dłuższy ciąg, tym wyższy mnożnik (maksymalnie 2.5x).
     * Mnożnik = 1.0 + (ciąg * 0.05), zaciśnięty do zakresu [1.0, 2.5]
     *
     * @param todayString Data dzisiejszego dnia
     * @return Mnożnik nagród (1.0 - 2.5)
     */
    fun getRewardMultiplier(todayString: String): Float {
        val baseStreak = if (lastGlobalStreakDate == todayString) {
            maxOf(0, currentStreak - 1)
        } else {
            currentStreak
        }

        val multiplier = 1.0f + (baseStreak * 0.05f)
        return multiplier.coerceIn(1.0f, 2.5f)
    }

    /**
     * Oblicza nowy ciąg dni bazując na tym czy wszystkie nawyki zostały wykonane.
     *
     * Logika:
     * - Jeśli `allDoneToday` jest true i to pierwszy raz dzisiaj: ciąg += 1
     * - Jeśli `allDoneToday` jest false i ciąg był dzisiaj: ciąg -= 1
     *
     * @param allDoneToday Czy wszystkie nawyki zostały wykonane dzisiaj
     * @param todayString Data dzisiejszego dnia
     * @param yesterdayString Data wczorajszego dnia
     * @return Nowy obiekt User ze zaktualizowanym ciągiem
     */
    fun calculateNewStreak(allDoneToday: Boolean, todayString: String, yesterdayString: String): User {
        var newGlobalStreak = currentStreak
        var newLastDate = lastGlobalStreakDate

        if (allDoneToday) {
            if (lastGlobalStreakDate != todayString) {
                newGlobalStreak += 1
                newLastDate = todayString
            }
        } else {
            if (lastGlobalStreakDate == todayString) {
                newGlobalStreak = maxOf(0, newGlobalStreak - 1)
                newLastDate = yesterdayString
            }
        }

        val newBestStreak = maxOf(bestStreak, newGlobalStreak)

        return copy(
            currentStreak = newGlobalStreak,
            bestStreak = newBestStreak,
            lastGlobalStreakDate = newLastDate
        )
    }

    /**
     * Oblicza inicjały użytkownika na podstawie jego pełnego imienia.
     *
     * Zwraca pierwsze litery dwóch słów z pełnego imienia, wielkimi literami.
     *
     * Przykłady:
     * - "Jan Kowalski" -> "JK"
     * - "Maria" -> "M"
     * - "Anna Maria Nowak" -> "AN"
     *
     * @return String zawierający inicjały (max 2 znaki)
     */
    val initials: String
        get() = name
            .trim()
            .split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
}