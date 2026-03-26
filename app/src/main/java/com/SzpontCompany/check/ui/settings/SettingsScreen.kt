package com.SzpontCompany.check.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.theme.* // Pobiera Twoje kolory z Color.kt

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(48.dp)) // Odstęp od status baru

        // --- TOP BAR ---
        SettingsTopBar(onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(32.dp))

        // --- WYGLĄD ---
        SectionHeader(text = stringResource(R.string.settings_section_appearance))
        // TODO: Komponent ThemeSelector (Ciemny, Jasny, Auto)
        Spacer(modifier = Modifier.height(24.dp))

        // --- KOLOR AKCENTU ---
        SectionHeader(text = stringResource(R.string.settings_section_accent))
        // TODO: Komponent AccentColorSelector
        Spacer(modifier = Modifier.height(24.dp))

        // --- JĘZYK ---
        SectionHeader(text = stringResource(R.string.settings_section_language))
        // TODO: Komponent LanguageSelector
        Spacer(modifier = Modifier.height(24.dp))

        // --- POWIADOMIENIA ---
        SectionHeader(text = stringResource(R.string.settings_section_notifications))
        SettingsListGroup {
            SettingsRowChevron(
                title = stringResource(R.string.settings_push_notifications),
                subtitle = "Codziennie · 08:00",
                iconRes = null, // TODO: Dodaj ikonę dzwonka w tle primary
                onClick = { /* TODO */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowSwitch(
                title = stringResource(R.string.settings_battle_notifications),
                iconRes = null, // TODO: Dodaj ikonę "A"
                isChecked = true,
                onCheckedChange = { /* TODO */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowSwitch(
                title = stringResource(R.string.settings_events_challenges),
                iconRes = null, // TODO: Dodaj ikonę kalendarza
                isChecked = true,
                onCheckedChange = { /* TODO */ }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- AKTYWNOŚĆ ---
        SectionHeader(text = stringResource(R.string.settings_section_activity))
        SettingsListGroup {
            SettingsRowSwitch(
                title = stringResource(R.string.settings_step_counter),
                iconRes = null,
                isChecked = true,
                onCheckedChange = { /* TODO */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowChevron(
                title = stringResource(R.string.settings_daily_step_goal),
                subtitle = "8 000 kroków",
                iconRes = null,
                onClick = { /* TODO */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowSwitch(
                title = stringResource(R.string.settings_friends_location),
                iconRes = null,
                isChecked = true,
                onCheckedChange = { /* TODO */ }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- PRYWATNOŚĆ I KONTO ---
        SectionHeader(text = stringResource(R.string.settings_section_privacy_account))
        SettingsListGroup {
            SettingsRowChevron(
                title = stringResource(R.string.settings_profile_privacy),
                subtitle = "Publiczny",
                iconRes = null,
                onClick = { /* TODO */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowChevron(
                title = stringResource(R.string.settings_about_app),
                subtitle = "Check. v1.0.0",
                iconRes = null,
                onClick = { /* TODO */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- USUŃ KONTO (Specjalny czerwony styl) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A1515)) // Ciemnoczerwone tło (dopasuj według potrzeb)
                .border(1.dp, Color(0xFF592020), RoundedCornerShape(16.dp))
                .clickable { /* TODO */ }
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.settings_delete_account),
                        color = Crimson, // Używamy Twojego koloru z Color.kt
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.settings_delete_account_warning),
                        color = Crimson.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Crimson
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SettingsTopBar(onBackClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "Cofnij",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsListGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
fun SettingsRowChevron(
    title: String,
    subtitle: String? = null,
    iconRes: Int?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: Miejsce na ikonę (np. dzwonek w tle)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsRowSwitch(
    title: String,
    subtitle: String? = "Włączone",
    iconRes: Int?,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: Miejsce na ikonę
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if(isChecked) "Włączone" else "Wyłączone",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.background
            )
        )
    }
}