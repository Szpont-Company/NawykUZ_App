package com.SzpontCompany.check.config

import com.google.firebase.functions.FirebaseFunctions

/**
 * FirebaseConfig - centralny punkt konfiguracji usług Firebase.
 *
 * Singleton zawierający referencje do skonfigurowanych instancji Firebase Functions
 * z wstępnie ustawionym regionem (europe-central2).
 *
 * Umożliwia łatwy dostęp do Firebase Cloud Functions z dowolnego miejsca w aplikacji,
 * zapewniając spójną konfigurację i zarządzanie funkcjami backendowymi.
 *
 * Funkcje dostępne przez Firebase Functions:
 * - updateUserProfile - aktualizacja profilu użytkownika
 * - deleteUserAccount - usunięcie konta użytkownika
 * - i inne funkcje biznesowe
 *
 * @since 1.0
 * @author Szpont Company
 */
object FirebaseConfig {
    /**
     * Instancja Firebase Functions skonfigurowana dla regionu europe-central2.
     *
     * Ten region zapewnia niskie opóźnienia dla użytkowników w Europie.
     * Stosuje się do wszystkich Cloud Functions wywoływanych z poziomu aplikacji.
     */
    val functions = FirebaseFunctions.getInstance("europe-central2")
}