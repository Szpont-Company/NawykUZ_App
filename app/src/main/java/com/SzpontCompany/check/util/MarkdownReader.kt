package com.SzpontCompany.check.util

import android.content.Context
import java.util.Locale

/**
 * Ładuje zlokalizowane warunki korzystania z usługi (Terms of Service).
 *
 * Obsługuje języki:
 * - Polski (pl) - tos_pl.md
 * - Angielski (domyślnie) - tos_en.md
 *
 * Pliki są przechowywane w folderze assets projektu.
 *
 * @param context Kontekst aplikacji
 * @return Zawartość TOS w formacie Markdown jako String
 */
fun loadLocalizedTos(context: Context): String {

    val language = Locale.getDefault().language

    val fileName = when (language) {
        "pl" -> "tos_pl.md"
        else -> "tos_en.md"
    }

    return context.assets
        .open(fileName)
        .bufferedReader()
        .use { it.readText() }
}

/**
 * Ładuje zlokalizowaną politykę prywatności.
 *
 * Obsługuje języki:
 * - Polski (pl) - policy_pl.md
 * - Angielski (domyślnie) - policy_en.md
 *
 * Pliki są przechowywane w folderze assets projektu.
 *
 * @param context Kontekst aplikacji
 * @return Zawartość polityki w formacie Markdown jako String
 */
fun loadLocalizedPolicy(context: Context): String {
    val language = Locale.getDefault().language

    val fileName = when (language) {
        "pl" -> "policy_pl.md"
        else -> "policy_en.md"
    }

    return context.assets
        .open(fileName)
        .bufferedReader()
        .use { it.readText() }
}