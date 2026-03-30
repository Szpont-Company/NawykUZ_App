package com.SzpontCompany.check.data

data class Badge(
    val id: String,
    val nameResId: Int,
    val emoji: String,
    val requirementResId: Int,
    val isUnlocked: Boolean
)