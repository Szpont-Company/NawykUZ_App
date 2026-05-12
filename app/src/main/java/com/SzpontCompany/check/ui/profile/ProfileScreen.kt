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
import com.SzpontCompany.check.ui.theme.getColorByName
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.SzpontCompany.check.data.badges.BadgeProvider
import com.SzpontCompany.check.ui.components.CheckBackButton
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.SzpontCompany.check.data.badges.Badge
import com.SzpontCompany.check.ui.components.UserAvatar
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.content.FileProvider
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.draw.drawWithCache
import com.SzpontCompany.check.data.user.User
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    var previousLevel by remember { mutableStateOf<Int?>(null) }
    var showLevelUpDialog by remember { mutableStateOf(false) }
    var newLevelToDisplay by remember { mutableIntStateOf(0) }

    LaunchedEffect(user?.level) {
        val currentLevel = user?.level
        if (currentLevel != null) {
            if (previousLevel != null && currentLevel > previousLevel!!) {
                newLevelToDisplay = currentLevel
                showLevelUpDialog = true
            }
            previousLevel = currentLevel
        }
    }

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

        UserHeaderSection(user = user)
        Spacer(modifier = Modifier.height(24.dp))

        LevelAndXpBar(user = user)
        Spacer(modifier = Modifier.height(24.dp))

        StatsGridSection(uiState = uiState)
        Spacer(modifier = Modifier.height(24.dp))

        BadgesSection(uiState = uiState)
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
                uiState = uiState,
                onDismiss = { showShareDialog = false },
                onShareConfirm = { imageUri ->
                    showShareDialog = false

                    val streak = uiState.user?.currentStreak ?: 0
                    val battles = uiState.battlesWon
                    val shareText =
                        "Hej! Mój streak to $streak dni, a na koncie mam $battles wygranych pojedynków. 🔥 Dołącz do mnie w Check. !"

                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Udostępnij profil"))
                }
            )
        }

        if (showLevelUpDialog) {
            LevelUpDialog(
                newLevel = newLevelToDisplay,
                onDismiss = { showLevelUpDialog = false }
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
fun UserHeaderSection(user: com.SzpontCompany.check.data.user.User?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                UserAvatar(
                    avatarEmoji = user?.avatarEmoji ?: "",
                    initials = user?.initials ?: "MK",
                    bgColor = user?.bgColor ?: "Mint",
                    size = 74.dp,
                    emojiSize = 28f,
                    initialsSize = 28f
                )
            }
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 4.dp)
                    .background(Color(0xFFBA7517), RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Lvl ${user?.level ?: 1}",
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = user?.name ?: "Brak danych",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (user?.nickname.isNullOrBlank()) "@nick" else "@${user?.nickname} • Check.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(
                            R.string.profile_streak_format,
                            user?.currentStreak ?: 0
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFBA7517).copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color(0xFFBA7517), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Top 14",
                        color = Color(0xFFBA7517),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LevelAndXpBar(user: User?) {

    var animationPlayed by remember { mutableStateOf(false) }

    val currentLevel = user?.level ?: 1
    val targetXp = user?.xp ?: 0
    val maxXp = user?.getXpThreshold() ?: 100
    val targetProgress = if (maxXp > 0) targetXp.toFloat() / maxXp.toFloat() else 0f

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
                    text = "${stringResource(R.string.profile_level)} $currentLevel",
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
                text = stringResource(R.string.profile_next_level, currentLevel + 1),
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
fun StatsGridSection(uiState: ProfileUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = uiState.habitsCount,
                label = stringResource(R.string.profile_habits)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = uiState.user?.currentStreak ?: 0,
                label = stringResource(R.string.profile_streak_days),
                valueColor = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = uiState.coins,
                label = stringResource(R.string.profile_coins),
                valueColor = Color(0xFFBA7517)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = uiState.battlesWon,
                label = stringResource(R.string.profile_battles_won)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = uiState.effectiveness,
                suffix = "%",
                label = stringResource(R.string.profile_effectiveness)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                targetValue = uiState.friendsCount,
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
        Text(
            text = "$animatedValue$suffix",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
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
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
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
fun BadgesSection(uiState: ProfileUiState) {

    var selectedBadge by remember { mutableStateOf<Badge?>(null) }


    val sortedBadges = remember(uiState) {
        BadgeProvider.evaluateBadges(
            bestStreak = uiState.user?.bestStreak ?: 0,
            battlesWon = uiState.battlesWon
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_badges_header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

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
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onShareConfirm: (Uri) -> Unit
) {
    val user = uiState.user
    val context = LocalContext.current
    val picture = remember { Picture() }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithCache {
                        val width = size.width.toInt()
                        val height = size.height.toInt()

                        onDrawWithContent {
                            val pictureCanvas = androidx.compose.ui.graphics.Canvas(
                                picture.beginRecording(width, height)
                            )

                            draw(
                                this,
                                layoutDirection,
                                pictureCanvas,
                                size
                            ) {
                                this@onDrawWithContent.drawContent()
                            }

                            picture.endRecording()

                            drawIntoCanvas { canvas ->
                                canvas.nativeCanvas.drawPicture(picture)
                            }
                        }
                    },
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
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatar(
                                avatarEmoji = user?.avatarEmoji ?: "",
                                initials = user?.initials ?: "??",
                                bgColor = user?.bgColor ?: "Mint",
                                size = 80.dp,
                                emojiSize = 32f,
                                initialsSize = 32f
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user?.name ?: "Nieznany",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = if (user?.nickname.isNullOrBlank()) "@nick" else "@${user?.nickname}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ShareStatItem(value = "Lvl ${user?.level ?: 1}", label = "Poziom")

                            ShareStatItem(
                                value = "🔥 ${user?.currentStreak ?: 0}",
                                label = "Streak",
                                valueColor = MaterialTheme.colorScheme.primary
                            )

                            ShareStatItem(
                                value = "🏆 ${uiState.battlesWon}",
                                label = "Wygrane",
                                valueColor = Color(0xFFBA7517)
                            )
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
                onClick = {
                    if (picture.width > 0 && picture.height > 0) {
                        val bitmap = Bitmap.createBitmap(
                            picture.width,
                            picture.height,
                            Bitmap.Config.ARGB_8888
                        )

                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        canvas.drawPicture(picture)

                        val uri = saveBitmapAndGetUri(context, bitmap)
                        uri?.let { onShareConfirm(it) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Udostępnij", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        dismissButton = null
    )
}

@Composable
fun ShareStatItem(
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


@Composable
fun BadgeDetailsDialog(
    badge: Badge,
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
                        text = stringResource(R.string.rewards_unlocked),
                        color = Color(0xFFBA7517),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.rewards_locked),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Super!", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}

fun saveBitmapAndGetUri(context: android.content.Context, bitmap: Bitmap): Uri? {
    return try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "profile_share.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}