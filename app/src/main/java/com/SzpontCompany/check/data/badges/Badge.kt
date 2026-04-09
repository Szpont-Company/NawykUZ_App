package com.SzpontCompany.check.data.badges

data class Badge(
    val id: String,
    val nameResId: Int,
    val emoji: String,
    val requirementResId: Int,
    val isUnlocked: Boolean
)