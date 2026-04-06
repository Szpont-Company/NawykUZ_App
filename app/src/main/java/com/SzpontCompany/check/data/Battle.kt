package com.SzpontCompany.check.data

data class Battle(
    val title: String,
    val daysLeft: Int,
    val myDays: Int,
    val totalDays: Int,
    val myHp: Int,
    val opponentName: String,
    val opponentDays: Int,
    val opponentHp: Int,
    val opponentCompleted: Boolean,
    val betAmount: Int,
    val endDate: String?,
    val isLosingWarning: Boolean,
    val isDoneToday: Boolean
)