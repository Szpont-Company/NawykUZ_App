package com.SzpontCompany.check.ui.addhabit

import androidx.compose.runtime.*
import com.SzpontCompany.check.ui.theme.Mint

@Composable
fun AddHabitHost(
    onClose: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }

    var finalHabitName by remember { mutableStateOf("") }
    var finalHabitIcon by remember { mutableStateOf("🎯") }
    var finalHabitColor by remember { mutableStateOf(Mint) }

    when (currentStep){
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
                onNextClick = { _, _, _, _, -> },
                onBackClick = { currentStep = 1 }
            )
        }
    }
}