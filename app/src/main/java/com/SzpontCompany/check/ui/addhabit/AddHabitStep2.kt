package com.SzpontCompany.check.ui.addhabit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint

@Composable
fun AddHabitStep2(
    onNextClick: (frequency: String, days: Set<String>, timesPerWeek: Int, goal: Int, unit: String) -> Unit,
    onBackClick: () -> Unit
) {
    // STAN: Przechowuje angielskie klucze bazy danych
    var frequencyKey by remember { mutableStateOf("DAILY") }
    var selectedDays by remember { mutableStateOf(setOf("1", "3", "5")) } // Przechowujemy id dnia (1-7)
    var timesPerWeek by remember { mutableStateOf(3) }
    var dailyGoal by remember { mutableStateOf(30) }
    var selectedUnitKey by remember { mutableStateOf("MINUTES") }

    // --- ANIMACJA PASKA POSTĘPU ---
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )
    LaunchedEffect(Unit) {
        progress = 0.5f // 50% dla kroku 2
    }

    // --- SŁOWNIKI (Mapowanie klucza bazy na wyświetlany tekst) ---
    val freqOptions = listOf("DAILY", "WEEKLY", "CUSTOM")
    val freqLabels = mapOf(
        "DAILY" to stringResource(R.string.habit_freq_daily),
        "WEEKLY" to stringResource(R.string.habit_freq_weekly),
        "CUSTOM" to stringResource(R.string.habit_freq_custom)
    )

    val unitOptions = listOf("MINUTES", "STEPS", "TIMES", "ML")
    val unitLabels = mapOf(
        "MINUTES" to stringResource(R.string.habit_unit_min),
        "STEPS" to stringResource(R.string.habit_unit_steps),
        "TIMES" to stringResource(R.string.habit_unit_times),
        "ML" to stringResource(R.string.habit_unit_ml)
    )

    // Pobieranie liter dni tygodnia z pliku tłumaczeń np. "Pn,Wt,Śr..."
    val daysInitials = stringResource(R.string.days_initials).split(",")
    val daysOfWeek = listOf(
        "1" to daysInitials.getOrElse(0) { "Pn" },
        "2" to daysInitials.getOrElse(1) { "Wt" },
        "3" to daysInitials.getOrElse(2) { "Śr" },
        "4" to daysInitials.getOrElse(3) { "Cz" },
        "5" to daysInitials.getOrElse(4) { "Pt" },
        "6" to daysInitials.getOrElse(5) { "So" },
        "7" to daysInitials.getOrElse(6) { "Nd" }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.habit_back_btn), tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = stringResource(R.string.habit_schedule),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "2 / 4",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PASEK POSTĘPU ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress) // Animowana wartość
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(stringResource(R.string.habit_freq_label))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                freqOptions.forEach { optionKey ->
                    SelectableChip(
                        text = freqLabels[optionKey] ?: optionKey,
                        isSelected = frequencyKey == optionKey,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { frequencyKey = optionKey }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = frequencyKey == "CUSTOM",
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column {
                    SectionTitle(stringResource(R.string.habit_days_label))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        daysOfWeek.forEach { (dayKey, dayLabel) ->
                            val isSelected = selectedDays.contains(dayKey)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedDays = if (isSelected) selectedDays - dayKey else selectedDays + dayKey
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLabel,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            AnimatedVisibility(
                visible = frequencyKey == "WEEKLY",
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column {
                    SectionTitle(stringResource(R.string.habit_times_week_label))
                    NumberPicker(
                        value = timesPerWeek,
                        onValueChange = { timesPerWeek = it.coerceIn(1, 7) },
                        onMinus = { if (timesPerWeek > 1) timesPerWeek-- },
                        onPlus = { if (timesPerWeek < 7) timesPerWeek++ },
                        suffix = stringResource(R.string.habit_unit_times)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            SectionTitle(if (frequencyKey == "WEEKLY") stringResource(R.string.habit_goal_session_label) else stringResource(R.string.habit_goal_daily_label))

            // Krok inkrementacji zależy od wybranej jednostki
            val currentStep = when (selectedUnitKey) {
                "STEPS" -> 500
                "ML" -> 100
                else -> 1
            }

            NumberPicker(
                value = dailyGoal,
                onValueChange = { dailyGoal = it },
                onMinus = { if (dailyGoal > 1) dailyGoal-- },
                onPlus = { dailyGoal++ },
                suffix = unitLabels[selectedUnitKey] ?: "",
                step = currentStep
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(stringResource(R.string.habit_unit_label))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                unitOptions.forEach { unitKey ->
                    SelectableChip(
                        text = unitLabels[unitKey] ?: unitKey,
                        isSelected = selectedUnitKey == unitKey,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedUnitKey = unitKey }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNextClick(frequencyKey, selectedDays, timesPerWeek, dailyGoal, selectedUnitKey) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        ) {
            Text(text = stringResource(R.string.habit_next_btn), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(text = stringResource(R.string.habit_back_btn), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    suffix: String,
    step: Int = 1
) {
    var textValue by remember(value) { mutableStateOf(if (value > 0) value.toString() else "") }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { repeat(step) { onMinus() } },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Mniej", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        textValue = newValue
                        val parsed = newValue.toIntOrNull()
                        if (parsed != null) {
                            onValueChange(parsed)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { repeat(step) { onPlus() } },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Więcej", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = suffix,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.width(50.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddHabitStep2Preview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        AddHabitStep2(
            onNextClick = { _, _, _, _, _ -> },
            onBackClick = {}
        )
    }
}