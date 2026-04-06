package com.SzpontCompany.check.ui.profile

import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.SzpontCompany.check.data.BadgeProvider
import com.SzpontCompany.check.ui.components.CheckBackButton
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        ProfileTopBar(
            onBackClick = onBackClick,
            onShareClick = { showShareDialog = true }
        )
        Spacer(modifier = Modifier.height(24.dp))

        UserHeaderSection()
        Spacer(modifier = Modifier.height(24.dp))

        LevelAndXpBar()
        Spacer(modifier = Modifier.height(24.dp))

        StatsGridSection()
        Spacer(modifier = Modifier.height(24.dp))

        BadgesSection()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.profile_settings_header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SettingsSection(
            onSettingsClick = onSettingsClick,
            onRewardsClick = onRewardsClick,
            onEditClick = onEditProfileClick,
            onLogoutClick = { showLogoutDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    onLogoutClick()
                },
                onDismiss = {
                    showLogoutDialog = false
                }
            )
        }

        if (showShareDialog) {
            ShareProfileDialog(
                onDismiss = { showShareDialog = false },
                onShareConfirm = {
                    showShareDialog = false


                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Hej! Mam 8 poziom i 21-dniowy streak w Check. 🔥 Dołącz do mnie!")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Udostępnij przez"))
                }
            )
        }

    }
}

@Composable
fun ProfileTopBar(onBackClick: () -> Unit, onShareClick: () -> Unit) {

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        CheckBackButton(onClick = onBackClick)

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            onShareClick()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Udostępnij profil",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            /* TODO: Otworz modal z dodawaniem znajomego */
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = "Dodaj znajomego",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UserHeaderSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "MK", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 4.dp)
                    .background(Color(0xFFBA7517), RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Lvl 8", color = MaterialTheme.colorScheme.background, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = "Marek Kowalski", // zostawiawmy hardcored, jak bedzie baza zmienimy
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@marekk • Check.", // Tez hardcored
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = stringResource(R.string.profile_streak_format, 21), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFBA7517).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFBA7517), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Top 14", color = Color(0xFFBA7517), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun LevelAndXpBar() {

    var animationPlayed by remember { mutableStateOf(false) }

    val currentLevel = 8
    val targetXp = 1240
    val maxXp = 1600
    val targetProgress = targetXp.toFloat() / maxXp.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) targetProgress else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "xp_progress_animation"
    )

    val animatedXp by animateIntAsState(
        targetValue = if (animationPlayed) targetXp else 0,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "xp_count_animation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${stringResource(R.string.profile_level)} 8",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.profile_xp_format, animatedXp, maxXp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = stringResource(R.string.profile_next_level, 9),
                color = Color(0xFFBA7517),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFD85A30), Color(0xFFBA7517))
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun StatsGridSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = 34,
                label = stringResource(R.string.profile_habits)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = 21,
                label = stringResource(R.string.profile_streak_days),
                valueColor = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = 850,
                label = stringResource(R.string.profile_coins),
                valueColor = Color(0xFFBA7517)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = 7,
                label = stringResource(R.string.profile_battles_won)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = 78,
                suffix = "%",
                label = stringResource(R.string.profile_effectiveness)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = 12,
                label = stringResource(R.string.profile_friends)
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    targetValue: Int,
    suffix: String = "",
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {

    var animationPlayed by remember { mutableStateOf(false) }

    val animatedValue by animateIntAsState(
        targetValue = if (animationPlayed) targetValue else 0,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "stat_count_animation"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$animatedValue$suffix", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsSection(
    onSettingsClick: () -> Unit,
    onRewardsClick: () -> Unit,
    onEditClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
    ) {
        SettingsItem(
            icon = Icons.Default.Person,
            iconTint = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.profile_edit),
            onClick = onEditClick
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

        SettingsItem(
            icon = Icons.Default.EmojiEvents,
            iconTint = Color(0xFFBA7517),
            title = stringResource(R.string.profile_rewards),
            onClick = onRewardsClick
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

        SettingsItem(
            icon = Icons.Default.Settings,
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            title = stringResource(R.string.profile_settings),
            onClick = onSettingsClick
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp))
            .clickable { onLogoutClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.profile_logout),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SettingsItem(icon: ImageVector, iconTint: Color, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun BadgesSection() {

    var selectedBadge by remember { mutableStateOf<com.SzpontCompany.check.data.Badge?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_badges_header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val sortedBadges = BadgeProvider.allBadges.sortedByDescending { it.isUnlocked }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(sortedBadges) { badge ->
                BadgeItem(
                    emoji = badge.emoji,
                    label = stringResource(id = badge.nameResId),
                    isActive = badge.isUnlocked,
                    onClick = { selectedBadge = badge }
                )
            }
        }
    }
    selectedBadge?.let { badge ->
        BadgeDetailsDialog(
            badge = badge,
            onDismiss = { selectedBadge = null }
        )
    }
}

@Composable
fun BadgeItem(emoji: String, label: String, isActive: Boolean, onClick: () -> Unit) {
    // aktywna -> obramowanie w kolorze primary,  nie -> przezroczyste
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent

    // wypełnienie (15% akcentu dla aktywnych, szare dla nieaktywnych)
    val bgColor = if (isActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    // nieaktywna -> lekko przezroczysta cala odznaka
    val alpha = if (isActive) 1f else 0.4f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(bgColor, RoundedCornerShape(16.dp))
                .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            //narazie emoji , potem ewentualnie jakies image/icon
            Text(text = emoji, fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier.fillMaxWidth(0.92f),
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(R.string.profile_logout),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_logout_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.padding(bottom = 8.dp, end = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE24B4A)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_logout_confirm),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialog_logout_cancel),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    )
}


@Composable
fun ShareProfileDialog(
    onDismiss: () -> Unit,
    onShareConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.9f),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = "Udostępnij profil",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.background
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("MK", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Marek Kowalski",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(text = "@marekk", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ShareStatItem(value = "Lvl 8", label = "Poziom")
                            ShareStatItem(value = "🔥 21", label = "Streak", valueColor = MaterialTheme.colorScheme.primary)
                            ShareStatItem(value = "🏆 7", label = "Wygrane", valueColor = Color(0xFFBA7517))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Check. • Wygrywaj każdy dzień",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShareConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Udostępnij", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        dismissButton = null
    )
}

@Composable
fun ShareStatItem(value: String, label: String, valueColor: Color = MaterialTheme.colorScheme.onBackground) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
fun BadgeDetailsDialog(
    badge: com.SzpontCompany.check.data.Badge,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.85f),
        shape = RoundedCornerShape(26.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            if (badge.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(24.dp)
                        )
                        .border(
                            2.dp,
                            if (badge.isUnlocked) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.emoji,
                        fontSize = 48.sp,
                        modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = badge.nameResId),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (badge.isUnlocked) {
                    Text(
                        text = "🏆 Odblokowana!",
                        color = Color(0xFFBA7517),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "🔒 Jeszcze nieodblokowana",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                 Text(
                     text = stringResource(id = badge.requirementResId),
                     style = MaterialTheme.typography.bodyMedium,
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     textAlign = TextAlign.Center
                 )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Super!", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        ProfileScreen()
    }
}