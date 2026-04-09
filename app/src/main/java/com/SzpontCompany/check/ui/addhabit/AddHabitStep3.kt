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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.components.WheelTimePicker
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint

@Composable
fun AddHabitStep3(
    habitColor: Color,
    onNextClick: (dailyReminder: Boolean, reminderTime: String, eveningReminder: Boolean, eveningTime: String, difficulty: String) -> Unit,
    onBackClick: () -> Unit
) {
    var dailyReminderEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("08:00") }
    var showMainTimePicker by remember { mutableStateOf(false) }

    var eveningReminderEnabled by remember { mutableStateOf(false) }
    var eveningReminderTime by remember { mutableStateOf("20:00") }
    var showEveningTimePicker by remember { mutableStateOf(false) }

    // STAN: Przechowuje angielskie klucze dla bazy danych
    var difficultyKey by remember { mutableStateOf("MEDIUM") }

    // --- ANIMACJA PASKA POSTĘPU ---
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )
    LaunchedEffect(Unit) {
        progress = 0.75f // 75% dla kroku 3
    }

    // --- SŁOWNIK TRUDNOŚCI ---
    val diffOptions = listOf("EASY" to "😊", "MEDIUM" to "💪", "HARD" to "🔥")
    val diffLabels = mapOf(
        "EASY" to stringResource(R.string.habit_diff_easy),
        "MEDIUM" to stringResource(R.string.habit_diff_medium),
        "HARD" to stringResource(R.string.habit_diff_hard)
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
                text = stringResource(R.string.habit_notifications),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "3 / 4",
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
                    .fillMaxWidth(animatedProgress)
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

            SectionTitle(stringResource(R.string.habit_notifications_caps))

            NotificationToggleCard(
                title = stringResource(R.string.habit_notif_daily),
                subtitle = stringResource(R.string.habit_notif_daily_desc),
                isChecked = dailyReminderEnabled,
                activeColor = habitColor,
                onCheckedChange = { dailyReminderEnabled = it }
            )

            AnimatedVisibility(
                visible = dailyReminderEnabled,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeSelectionBox(time = reminderTime) { showMainTimePicker = true }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NotificationToggleCard(
                title = stringResource(R.string.habit_notif_evening),
                subtitle = stringResource(R.string.habit_notif_evening_desc),
                isChecked = eveningReminderEnabled,
                activeColor = habitColor,
                onCheckedChange = { eveningReminderEnabled = it }
            )

            AnimatedVisibility(
                visible = eveningReminderEnabled,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeSelectionBox(time = eveningReminderTime) { showEveningTimePicker = true }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle(stringResource(R.string.habit_difficulty_label))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                diffOptions.forEach { (key, emoji) ->
                    DifficultyCard(
                        emoji = emoji,
                        label = diffLabels[key] ?: key,
                        isSelected = difficultyKey == key,
                        modifier = Modifier.weight(1f),
                        onClick = { difficultyKey = key }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            // Przekazujemy klucz trudności (np. "EASY") zamiast słowa "Łatwy"
            onClick = { onNextClick(dailyReminderEnabled, reminderTime, eveningReminderEnabled, eveningReminderTime, difficultyKey) },
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

    // Modale wyboru czasu
    if (showMainTimePicker) {
        TimePickerDialog(
            initialTime = reminderTime,
            onDismiss = { showMainTimePicker = false },
            onConfirm = {
                reminderTime = it
                showMainTimePicker = false
            }
        )
    }

    if (showEveningTimePicker) {
        TimePickerDialog(
            initialTime = eveningReminderTime,
            onDismiss = { showEveningTimePicker = false },
            onConfirm = {
                eveningReminderTime = it
                showEveningTimePicker = false
            }
        )
    }
}

@Composable
fun TimeSelectionBox(time: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = "Wybierz godzinę",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = initialTime.split(":")
    val initHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
    val initMin = parts.getOrNull(1)?.toIntOrNull() ?: 0

    var tempHour by remember { mutableIntStateOf(initHour) }
    var tempMinute by remember { mutableIntStateOf(initMin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.clip(RoundedCornerShape(24.dp)),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = null,
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                WheelTimePicker(
                    initialHour = initHour,
                    initialMinute = initMin,
                    onTimeSelected = { h, m ->
                        tempHour = h
                        tempMinute = m
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val formatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", tempHour, tempMinute)
                onConfirm(formatted)
            }) {
                Text(stringResource(R.string.action_save), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            }
        }
    )
}

@Composable
fun NotificationToggleCard(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    activeColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun DifficultyCard(
    emoji: String,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddHabitStep3Preview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        AddHabitStep3(
            habitColor = Mint,
            onNextClick = { _, _, _, _, _ -> },
            onBackClick = {}
        )
    }
}