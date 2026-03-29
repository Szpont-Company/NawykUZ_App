package com.SzpontCompany.check.data

data class Badge(
    val id: String,
    val name: String,
    val emoji: String,
    val requirement: String,
    val isUnlocked: Boolean
)