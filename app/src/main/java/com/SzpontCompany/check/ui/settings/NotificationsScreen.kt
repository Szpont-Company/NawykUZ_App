package com.SzpontCompany.check.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.SzpontCompany.check.ui.theme.Amber
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint

enum class NotificationFrequency {
    EVERYDAY, WORKDAYS, CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBackClick: () -> Unit = {}) {
    var mainReminders by remember { mutableStateOf(true) }
    var eveningReminders by remember { mutableStateOf(true) }
    var notificationSound by remember { mutableStateOf(true) }
    var vibrations by remember { mutableStateOf(true) }

    var selectedFrequency by remember { mutableStateOf(NotificationFrequency.EVERYDAY) }

    var savedHour by remember { mutableStateOf(8) }
    var savedMinute by remember { mutableStateOf(0) }
    var showTimePicker by remember { mutableStateOf(false) }

    var selectedDays by remember { mutableStateOf(setOf(0, 1, 2, 3, 4)) }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_push_notifications),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            NotificationSectionTitle(stringResource(R.string.notifications_section_main))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationToggleOption(
                    title = stringResource(R.string.notifications_enable_title),
                    description = stringResource(R.string.notifications_enable_desc),
                    isToggled = mainReminders,
                    onToggle = { mainReminders = it }
                )

                TimeSelectionOption(
                    title = stringResource(R.string.notifications_time_title),
                    time = String.format(java.util.Locale.getDefault(), "%02d:%02d", savedHour, savedMinute),
                    onClick = { showTimePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            NotificationSectionTitle(stringResource(R.string.notifications_section_frequency))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FrequencyChip(
                    title = stringResource(R.string.notifications_freq_everyday),
                    isSelected = selectedFrequency == NotificationFrequency.EVERYDAY,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFrequency = NotificationFrequency.EVERYDAY }
                )
                FrequencyChip(
                    title = stringResource(R.string.notifications_freq_workdays),
                    isSelected = selectedFrequency == NotificationFrequency.WORKDAYS,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFrequency = NotificationFrequency.WORKDAYS }
                )
                FrequencyChip(
                    title = stringResource(R.string.notifications_freq_custom),
                    isSelected = selectedFrequency == NotificationFrequency.CUSTOM,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFrequency = NotificationFrequency.CUSTOM }
                )
            }

            AnimatedVisibility(
                visible = selectedFrequency == NotificationFrequency.CUSTOM,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = stringResource(R.string.days_initials).split(",")
                        days.forEachIndexed { index, day ->
                            val isSelected = selectedDays.contains(index)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .clickable {
                                        selectedDays = if (isSelected) {
                                            selectedDays - index
                                        } else {
                                            selectedDays + index
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NotificationSectionTitle(stringResource(R.string.notifications_section_additional))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationToggleOption(
                    title = stringResource(R.string.notifications_evening_title),
                    description = stringResource(R.string.notifications_evening_desc),
                    isToggled = eveningReminders,
                    onToggle = { eveningReminders = it }
                )

                NotificationToggleOption(
                    title = stringResource(R.string.notifications_sound_title),
                    description = stringResource(R.string.notifications_sound_desc),
                    isToggled = notificationSound,
                    onToggle = { notificationSound = it }
                )

                NotificationToggleOption(
                    title = stringResource(R.string.notifications_vibration_title),
                    description = null,
                    isToggled = vibrations,
                    onToggle = { vibrations = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { /* TODO: Zapisz logikę */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = stringResource(R.string.notifications_save_button),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showTimePicker) {
            var tempHour by remember { mutableIntStateOf(savedHour) }
            var tempMinute by remember { mutableIntStateOf(savedMinute) }

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                modifier = Modifier.clip(RoundedCornerShape(24.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                title = null,
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                        WheelTimePicker(
                            initialHour = savedHour,
                            initialMinute = savedMinute,
                            onTimeSelected = { hour, minute ->
                                tempHour = hour
                                tempMinute = minute
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        savedHour = tempHour
                        savedMinute = tempMinute
                        showTimePicker = false
                    }) {
                        Text(
                            text = stringResource(R.string.action_save),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun NotificationSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun NotificationToggleOption(
    title: String,
    description: String?,
    isToggled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!description.isNullOrEmpty()) {
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Switch(
            checked = isToggled,
            onCheckedChange = { onToggle(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.background
            ),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun TimeSelectionOption(
    title: String,
    time: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 18.sp
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun FrequencyChip(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    CheckTheme(darkTheme = true, accent = Amber) {
        NotificationsScreen()
    }
}