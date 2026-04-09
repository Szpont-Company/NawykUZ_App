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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.SzpontCompany.check.ui.components.CheckBackButton
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.SzpontCompany.check.notifications.NotificationScheduler

enum class NotificationFrequency {
    EVERYDAY, WORKDAYS, CUSTOM
}

@Composable
fun NotificationsScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("check_notifications", Context.MODE_PRIVATE) }
    val scheduler = remember { NotificationScheduler(context) }

    var mainReminders by remember { mutableStateOf(prefs.getBoolean("mainReminders", false)) }
    var eveningReminders by remember { mutableStateOf(prefs.getBoolean("eveningReminders", true)) }
    var notificationSound by remember { mutableStateOf(prefs.getBoolean("notificationSound", true)) }
    var vibrations by remember { mutableStateOf(prefs.getBoolean("vibrations", true)) }

    val savedFrequencyStr = prefs.getString("selectedFrequency", NotificationFrequency.EVERYDAY.name) ?: NotificationFrequency.EVERYDAY.name
    var selectedFrequency by remember { mutableStateOf(NotificationFrequency.valueOf(savedFrequencyStr)) }

    var savedHour by remember { mutableIntStateOf(prefs.getInt("savedHour", 20)) }
    var savedMinute by remember { mutableIntStateOf(prefs.getInt("savedMinute", 0)) }
    var showTimePicker by remember { mutableStateOf(false) }

    val savedDaysStrSet = prefs.getStringSet("selectedDays", setOf("0", "1", "2", "3", "4")) ?: setOf("0", "1", "2", "3", "4")
    var selectedDays by remember { mutableStateOf(savedDaysStrSet.map { it.toInt() }.toSet()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scheduler.scheduleDailyReminder(savedHour, savedMinute)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            CheckBackButton(onClick = onBackClick)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.settings_push_notifications),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = stringResource(R.string.days_initials).split(",")
                    days.forEachIndexed { index, day ->
                        val isSelected = selectedDays.contains(index)

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .let { modifier ->
                                    if (isSelected) {
                                        modifier.border(
                                            width = 1.5.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else {
                                        modifier
                                    }
                                }
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
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
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
            onClick = {
                prefs.edit()
                    .putBoolean("mainReminders", mainReminders)
                    .putBoolean("eveningReminders", eveningReminders)
                    .putBoolean("notificationSound", notificationSound)
                    .putBoolean("vibrations", vibrations)
                    .putInt("savedHour", savedHour)
                    .putInt("savedMinute", savedMinute)
                    .putString("selectedFrequency", selectedFrequency.name)
                    .putStringSet("selectedDays", selectedDays.map { it.toString() }.toSet())
                    .apply()

                if (mainReminders) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            scheduler.scheduleDailyReminder(savedHour, savedMinute)
                        }
                    } else {
                        scheduler.scheduleDailyReminder(savedHour, savedMinute)
                    }
                } else {
                    scheduler.cancelReminder()
                }
                onBackClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
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

        Spacer(modifier = Modifier.height(20.dp))

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