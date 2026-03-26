package com.SzpontCompany.check.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import com.SzpontCompany.check.ui.theme.*

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
        Spacer(modifier = Modifier.height(48.dp))

        // --- TOP BAR ---
        SettingsTopBar(onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(32.dp))

        // --- WYGLĄD ---
        SectionHeader(text = stringResource(R.string.settings_section_appearance))
        ThemeSelector()
        Spacer(modifier = Modifier.height(24.dp))

        // --- KOLOR AKCENTU ---
        SectionHeader(text = stringResource(R.string.settings_section_accent))
        AccentColorSelector()
        Spacer(modifier = Modifier.height(24.dp))

        // --- JĘZYK ---
        SectionHeader(text = stringResource(R.string.settings_section_language))
        LanguageSelector()
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

        // --- USUŃ KONTO ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A1515))
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
                        color = Crimson,
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

@Composable
fun ThemeSelector() {
    var selectedTheme by remember { mutableStateOf("Ciemny") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemeCard(
            title = stringResource(R.string.settings_theme_dark),
            isSelected = selectedTheme == "Ciemny",
            onClick = { selectedTheme = "Ciemny" },
            modifier = Modifier.weight(1f)
        ) {
            DarkThemePreview()
        }

        ThemeCard(
            title = stringResource(R.string.settings_theme_light),
            isSelected = selectedTheme == "Jasny",
            onClick = { selectedTheme = "Jasny" },
            modifier = Modifier.weight(1f)
        ) {
            LightThemePreview()
        }

        ThemeCard(
            title = stringResource(R.string.settings_theme_auto),
            isSelected = selectedTheme == "Auto",
            onClick = { selectedTheme = "Auto" },
            modifier = Modifier.weight(1f)
        ) {
            AutoThemePreview()
        }
    }
}

@Composable
fun ThemeCard(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    previewContent: @Composable BoxScope.() -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f),
            contentAlignment = Alignment.Center
        ) {
            previewContent()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DarkThemePreview() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF111112))
    ) {
        FakeUIElements()
    }
}

@Composable
fun LightThemePreview() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF4F4F0))
    ) {
        FakeUIElements(isLight = true)
    }
}

@Composable
fun AutoThemePreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val darkPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(0f, height)
                close()
            }
            drawPath(darkPath, color = Color(0xFF111112))

            val lightPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(width, 0f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(lightPath, color = Color(0xFFF4F4F0))
        }
        FakeUIElements(isAuto = true)
    }
}

@Composable
fun FakeUIElements(isLight: Boolean = false, isAuto: Boolean = false) {
    val elementColor = if (isLight) Color(0xFFE8E8E4) else Color(0xFF2A2A2E)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isAuto) Color.Transparent else elementColor)
            )
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isAuto) Color.Transparent else elementColor)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccentColorSelector() {
    val colors = listOf(Mint, Indigo, Coral, Sky, Rose, Cactus, Amber, Crimson)

    var selectedColor by remember { mutableStateOf(Mint) }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        colors.forEach { color ->
            val isSelected = color == selectedColor

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .clickable { selectedColor = color },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(color)
                            .border(3.dp, MaterialTheme.colorScheme.background, androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageSelector() {
    var selectedLanguage by remember { mutableStateOf("Polski") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LanguageButton(
            text = "Polski",
            isSelected = selectedLanguage == "Polski",
            modifier = Modifier.weight(1f),
            onClick = { selectedLanguage = "Polski" }
        )
        Spacer(modifier = Modifier.width(8.dp))
        LanguageButton(
            text = "English",
            isSelected = selectedLanguage == "English",
            modifier = Modifier.weight(1f),
            onClick = { selectedLanguage = "English" }
        )
    }
}

@Composable
fun LanguageButton (
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}