package com.SzpontCompany.check.ui.addhabit

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.ui.theme.Mint
import com.SzpontCompany.check.ui.theme.getColorName
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import com.SzpontCompany.check.notifications.NotificationScheduler

@Composable
fun AddHabitHost(
    onClose: () -> Unit,
    viewModel: AddHabitViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }

    // --- DANE Z KROKU 1 ---
    var finalHabitName by remember { mutableStateOf("") }
    var finalHabitIcon by remember { mutableStateOf("🎯") }
    var finalHabitColor by remember { mutableStateOf(Mint) }

    // --- DANE Z KROKU 2 ---
    var finalFrequency by remember { mutableStateOf("") }
    var finalSelectedDays by remember { mutableStateOf<Set<String>>(emptySet()) }
    var finalTimesPerWeek by remember { mutableStateOf(0) }
    var finalDailyGoal by remember { mutableStateOf(0) }
    var finalSelectedUnit by remember { mutableStateOf("") }

    // --- DANE Z KROKU 3 ---
    var finalDailyReminder by remember { mutableStateOf(false) }
    var finalReminderTime by remember { mutableStateOf("08:00") }
    var finalEveningReminder by remember { mutableStateOf(false) }
    var finalEveningTime by remember { mutableStateOf("20:00") }
    var finalDifficulty by remember { mutableStateOf("Średni") }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, "Błąd zapisu: $it", Toast.LENGTH_LONG).show()
        }
    }

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            // Jeśli idziemy do przodu (np. 1 -> 2), ekran wjeżdża z prawej
            if (targetState > initialState) {
                (slideInHorizontally { width -> width } + fadeIn(tween(300))).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut(tween(300))
                )
            } else {
                // Jeśli wracamy (np. 2 -> 1), ekran wjeżdża z lewej
                (slideInHorizontally { width -> -width } + fadeIn(tween(300))).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut(tween(300))
                )
            }
        },
        label = "step_transition"
    ) { step ->
        when (step) {
            1 -> {
                AddHabitStep1(
                    onNextClick = { name, icon, color ->
                        finalHabitName = name
                        finalHabitIcon = icon
                        finalHabitColor = color
                        currentStep = 2
                    },
                    onBackClick = { onClose() }
                )
            }
            2 -> {
                AddHabitStep2(
                    onNextClick = { frequency, days, timesPerWeek, goal, unit ->
                        finalFrequency = frequency
                        finalSelectedDays = days
                        finalTimesPerWeek = timesPerWeek
                        finalDailyGoal = goal
                        finalSelectedUnit = unit
                        currentStep = 3
                    },
                    onBackClick = { currentStep = 1 }
                )
            }
            3 -> {
                AddHabitStep3(
                    habitColor = finalHabitColor,
                    onNextClick = { dailyReminder, reminderTime, eveningReminder, eveningTime, difficulty ->
                        finalDailyReminder = dailyReminder
                        finalReminderTime = reminderTime
                        finalEveningReminder = eveningReminder
                        finalEveningTime = eveningTime
                        finalDifficulty = difficulty

                        currentStep = 4
                    },
                    onBackClick = { currentStep = 2 }
                )
            }
            4 -> {
                AddHabitStep4(
                    name = finalHabitName,
                    icon = finalHabitIcon,
                    color = finalHabitColor,
                    frequency = finalFrequency,
                    dailyGoal = finalDailyGoal,
                    unit = finalSelectedUnit,
                    dailyReminder = finalDailyReminder,
                    reminderTime = finalReminderTime,
                    eveningReminder = finalEveningReminder,
                    eveningTime = finalEveningTime,
                    difficulty = finalDifficulty,
                    onBackClick = { currentStep = 3 },
                    onConfirmClick = {
                        val newHabit = Habit(
                            name = finalHabitName,
                            icon = finalHabitIcon,
                            colorName = getColorName(finalHabitColor),
                            frequency = finalFrequency,
                            selectedDays = finalSelectedDays.toList(),
                            timesPerWeek = finalTimesPerWeek,
                            dailyGoal = finalDailyGoal,
                            unit = finalSelectedUnit,
                            dailyReminder = finalDailyReminder,
                            reminderTime = finalReminderTime,
                            eveningReminder = finalEveningReminder,
                            eveningTime = finalEveningTime,
                            difficulty = finalDifficulty
                        )

                        viewModel.saveHabit(habit = newHabit) { newId ->
                            if (finalDailyReminder) {
                                val parts = finalReminderTime.split(":")
                                val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                NotificationScheduler(context).scheduleHabitReminder(newId, finalHabitName, finalHabitIcon, h, m, false)
                            }
                            if (finalEveningReminder) {
                                val parts = finalEveningTime.split(":")
                                val h = parts.getOrNull(0)?.toIntOrNull() ?: 20
                                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                NotificationScheduler(context).scheduleHabitReminder(newId, finalHabitName, finalHabitIcon, h, m, true)
                            }
                            currentStep = 5
                        }
                    }
                )
            }
            5 -> {
                AddHabitSuccess(
                    name = finalHabitName,
                    icon = finalHabitIcon,
                    color = finalHabitColor,
                    dailyReminder = finalDailyReminder,
                    reminderTime = finalReminderTime,
                    onFinishClick = { onClose() }
                )
            }
        }
    }
}