package com.SzpontCompany.check.util

import android.content.Context
import java.util.Locale

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