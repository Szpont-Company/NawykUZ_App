package com.SzpontCompany.check.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBackClick: () -> Unit = {}) {
    var mainReminders by remember { mutableStateOf(true) }
    var eveningReminders by remember { mutableStateOf(true) }
    var notificationSound by remember { mutableStateOf(true) }
    var vibrations by remember { mutableStateOf(true) }

    var selectedFrequency by remember { mutableStateOf("Codziennie") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Powiadomienia push",
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

            NotificationSectionTitle("GŁÓWNE PRZYPOMNIENIE")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationToggleOption(
                    title = "Włącz przypomnienia",
                    description = "Codzienne powiadomienie o nawykach",
                    isToggled = mainReminders,
                    onToggle = { mainReminders = it }
                )

                TimeSelectionOption(
                    title = "Godzina\nprzypomnienia",
                    time = "08:00",
                    onClick = { /* TODO: Otwórz picker czasu */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            NotificationSectionTitle("CZĘSTOTLIWOŚĆ")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FrequencyChip(
                    title = "Codziennie",
                    isSelected = selectedFrequency == "Codziennie",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFrequency = "Codziennie" }
                )
                FrequencyChip(
                    title = "Dni rob.",
                    isSelected = selectedFrequency == "Dni rob.",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFrequency = "Dni rob." }
                )
                FrequencyChip(
                    title = "Własne",
                    isSelected = selectedFrequency == "Własne",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedFrequency = "Własne" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            NotificationSectionTitle("DODATKOWE")

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NotificationToggleOption(
                    title = "Przypomnienie wieczorne",
                    description = "Jeśli nawyk nieukończony do 20:00",
                    isToggled = eveningReminders,
                    onToggle = { eveningReminders = it }
                )

                NotificationToggleOption(
                    title = "Dźwięk powiadomień",
                    description = "Domyślny systemowy",
                    isToggled = notificationSound,
                    onToggle = { notificationSound = it }
                )

                NotificationToggleOption(
                    title = "Wibracje",
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
                    text = "Zapisz ustawienia",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
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
    CheckTheme(darkTheme = true, accent = Mint) {
        NotificationsScreen()
    }
}