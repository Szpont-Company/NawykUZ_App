package com.SzpontCompany.check.data.habit

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Habit(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val colorName: String = "",
    val frequency: String = "",
    val selectedDays: List<String> = emptyList(),
    val timesPerWeek: Int = 0,
    val dailyGoal: Int = 0,
    val unit: String = "",
    val dailyReminder: Boolean = false,
    val reminderTime: String = "",
    val eveningReminder: Boolean = false,
    val eveningTime: String = "",
    val difficulty: String = "",
    val isActive: Boolean = true,

    val battleId: String? = null,
    val opponentName: String? = null,

    val streak: Int = 0,
    val monthlyCompletionRate: Int = 0,
    val completedDates: List<String> = emptyList(),
    val dailyNotes: Map<String, String> = emptyMap(),

    @ServerTimestamp val createdAt: Date? = null
)