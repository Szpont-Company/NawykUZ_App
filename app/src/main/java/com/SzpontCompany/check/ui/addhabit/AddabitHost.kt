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
                onNextClick = { _, _, _ -> },
                onBackClick = { onClose() }
            )
        }
    }
}