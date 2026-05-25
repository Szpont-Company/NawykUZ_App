package com.SzpontCompany.check

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize

/**
 * CheckApp - główna klasa aplikacji rozszerzająca Application.
 *
 * Odpowiada za inicjalizację Firebase SDK i innych globalnych komponentów
 * aplikacji przy jej uruchomieniu.
 *
 * Funkcje:
 * - Inicjalizacja Firebase (Authentication, Firestore, Cloud Messaging, Functions)
 * - Konfiguracja pozostałych bibliotek globalnych
 *
 * @since 1.0
 * @author Szpont Company
 */
class CheckApp : Application() {
    /**
     * Wywoływana przy tworzeniu aplikacji.
     *
     * Przeprowadza inicjalizację Firebase SDK, która jest wymagana
     * do poprawnego działania wszystkich usług Firebase w aplikacji.
     */
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
    }
}