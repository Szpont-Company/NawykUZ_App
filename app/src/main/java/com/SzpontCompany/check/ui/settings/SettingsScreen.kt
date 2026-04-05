package com.SzpontCompany.check.ui.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.theme.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.scale
import com.SzpontCompany.check.ui.components.CheckBackButton
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType


@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onStepGoalClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(LocalContext.current.applicationContext)
    )
) {

    val currentTheme by viewModel.themeState.collectAsState()
    val currentAccentColor by viewModel.accentColorState.collectAsState()
    val currentLanguage by viewModel.languageState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        SettingsTopBar(onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(32.dp))

        SectionHeader(text = stringResource(R.string.settings_section_appearance))
        ThemeSelector(
            selectedTheme = currentTheme,
            onThemeSelected = { viewModel.updateTheme(it) }
        )
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = stringResource(R.string.settings_section_accent))
        AccentColorSelector(
            selectedColorName = currentAccentColor,
            onColorSelected = { viewModel.updateAccentColor(it) }
        )
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = stringResource(R.string.settings_section_language))
        LanguageSelector(
            selectedLanguage = currentLanguage,
            onLanguageSelected = { selectedLang ->
                viewModel.updateLanguage(selectedLang)
                val localeCode = if (selectedLang == "Polski") "pl" else "en"
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeCode))
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = stringResource(R.string.settings_section_notifications))
        SettingsListGroup {
            SettingsRowChevron(
                title = stringResource(R.string.settings_push_notifications),
                subtitle = "Codziennie · 08:00",
                icon = Icons.Rounded.Notifications,
                baseColor = Mint,
                onClick = onNotificationsClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowSwitch(
                title = stringResource(R.string.settings_battle_notifications),
                icon = Icons.Rounded.FlashOn,
                baseColor = Coral,
                isChecked = true,
                onCheckedChange = { /* TODO */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowSwitch(
                title = stringResource(R.string.settings_events_challenges),
                icon = Icons.Rounded.Event,
                baseColor = Indigo,
                isChecked = true,
                onCheckedChange = { /* TODO */ }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))


        SectionHeader(text = stringResource(R.string.settings_section_activity))
        SettingsListGroup {
            SettingsRowSwitch(
                title = stringResource(R.string.settings_step_counter),
                icon = Icons.Rounded.DirectionsWalk,
                baseColor = Sky,
                isChecked = true,
                onCheckedChange = { /* TODO */ }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowChevron(
                title = stringResource(R.string.settings_daily_step_goal),
                subtitle = "8 000 kroków",
                icon = Icons.Rounded.Adjust,
                baseColor = Cactus,
                onClick = onStepGoalClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowSwitch(
                title = stringResource(R.string.settings_friends_location),
                icon = Icons.Rounded.LocationOn,
                baseColor = Rose,
                isChecked = true,
                onCheckedChange = { /* TODO */ }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader(text = stringResource(R.string.settings_section_privacy_account))
        SettingsListGroup {
            SettingsRowChevron(
                title = stringResource(R.string.settings_profile_privacy),
                subtitle = "Publiczny",
                icon = Icons.Rounded.Lock,
                baseColor = Indigo,
                onClick = onPrivacyClick
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)
            SettingsRowChevron(
                title = stringResource(R.string.settings_about_app),
                subtitle = "Check. v1.0.0",
                icon = Icons.Rounded.Info,
                baseColor = Amber,
                onClick = { /* TODO */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


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
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsIcon(
                    icon = Icons.Rounded.DeleteOutline,
                    baseColor = Crimson
                )
                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_delete_account),
                        color = Crimson,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsTopBar(onBackClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        CheckBackButton(onClick = onBackClick)

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
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    baseColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            SettingsIcon(icon = icon, baseColor = baseColor)
            Spacer(modifier = Modifier.width(16.dp))
        }
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
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    baseColor: Color = MaterialTheme.colorScheme.primary,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            SettingsIcon(icon = icon, baseColor = baseColor)
            Spacer(modifier = Modifier.width(16.dp))
        }

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
fun SettingsIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    baseColor: Color = MaterialTheme.colorScheme.primary
) {

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(baseColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = baseColor,
            modifier = Modifier.size(20.dp)
        )
    }
}



@Composable
fun ThemeSelector(selectedTheme: String, onThemeSelected: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemeCard(
            title = stringResource(R.string.settings_theme_dark),
            isSelected = selectedTheme == "Ciemny",
            onClick = {
                if (selectedTheme != "Ciemny") {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onThemeSelected("Ciemny")
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            DarkThemePreview()
        }

        ThemeCard(
            title = stringResource(R.string.settings_theme_light),
            isSelected = selectedTheme == "Jasny",
            onClick = {
                if (selectedTheme != "Jasny") {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onThemeSelected("Jasny")
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            LightThemePreview()
        }

        ThemeCard(
            title = stringResource(R.string.settings_theme_auto),
            isSelected = selectedTheme == "Auto",
            onClick = {
                if (selectedTheme != "Auto") {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onThemeSelected("Auto")
                }
            },
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

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "theme_card_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
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
    val elementColor = when {
        isAuto -> Color(0xFF8A8A8E)
        isLight -> Color(0xFFD0D0CC)
        else -> Color(0xFF42424A)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(elementColor)
            )
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(elementColor)
            )
        }
    }
}
data class AccentColorItem(
    val color: Color,
    val nameResId: Int,
    val name: String
)

@Composable
fun AccentColorSelector(selectedColorName: String, onColorSelected: (String) -> Unit) {
    val colors = listOf(
        AccentColorItem(Mint, R.string.color_mint, "Mint"),
        AccentColorItem(Indigo, R.string.color_indigo, "Indigo"),
        AccentColorItem(Coral, R.string.color_coral, "Coral"),
        AccentColorItem(Sky, R.string.color_sky, "Sky"),
        AccentColorItem(Rose, R.string.color_rose, "Rose"),
        AccentColorItem(Cactus, R.string.color_cactus, "Cactus"),
        AccentColorItem(Amber, R.string.color_amber, "Amber"),
        AccentColorItem(Crimson, R.string.color_crimson, "Crimson")
    )

    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        colors.chunked(4).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowColors.forEach { item ->
                    val isSelected = item.name == selectedColorName

                    val alpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.5f,
                        label = "alpha_anim"
                    )

                    val size by animateDpAsState(
                        targetValue = if (isSelected) 64.dp else 44.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "size_anim"
                    )

                    val logoAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        label = "logo_alpha_anim"
                    )

                    val logoScale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.5f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "logo_scale_anim"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .alpha(alpha)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onColorSelected(item.name)
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier.height(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(size)
                                    .clip(CircleShape)
                                    .background(item.color),
                                contentAlignment = Alignment.Center
                            ) {
                                if (logoAlpha > 0f) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_check_logo),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(7.dp)
                                            .alpha(logoAlpha)
                                            .scale(logoScale)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(item.nameResId),
                            fontSize = 11.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelector(selectedLanguage: String, onLanguageSelected: (String) -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color((0xFF151517)))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LanguageButton(
            text = "Polski",
            isSelected = selectedLanguage == "Polski",
            modifier = Modifier.weight(1f),
            onClick = { onLanguageSelected("Polski") }
        )
        LanguageButton(
            text = "English",
            isSelected = selectedLanguage == "English",
            modifier = Modifier.weight(1f),
            onClick = { onLanguageSelected("English") }
        )
    }
}

@Composable
fun LanguageButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    val flag = when (text) {
        "Polski" -> "🇵🇱"
        "English" -> "🇺🇸"
        else -> ""
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = flag,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        SettingsScreen(
            onBackClick = {},
            onStepGoalClick = {},
            onPrivacyClick = {},
            onNotificationsClick = {}
        )
    }
}