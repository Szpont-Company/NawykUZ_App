package com.SzpontCompany.check.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.badges.Badge
import com.SzpontCompany.check.data.badges.BadgeProvider
import com.SzpontCompany.check.data.user.User
import com.SzpontCompany.check.ui.components.CheckBackButton
import com.SzpontCompany.check.ui.components.UserAvatar
import com.SzpontCompany.check.R


@Composable
fun FriendProfileScreen(
    friendUid: String,
    onBackClick: () -> Unit = {},
    viewModel: FriendProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }

    LaunchedEffect(friendUid) {
        viewModel.loadFriendProfile(friendUid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            CheckBackButton(onClick = onBackClick)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.player_profile),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.error_occurred, uiState.error ?: ""),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            FriendUserHeaderSection(user = uiState.user)

            Spacer(modifier = Modifier.height(32.dp))

            FriendLevelAndXpBar(user = uiState.user)

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.profile_badges_header),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val friendBadges = remember(uiState) {
                BadgeProvider.evaluateBadges(
                    bestStreak = uiState.user?.bestStreak ?: 0,
                    battlesWon = uiState.battlesWon
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(friendBadges) { badge ->
                    BadgeItem(
                        emoji = badge.emoji,
                        label = stringResource(id = badge.nameResId),
                        isActive = badge.isUnlocked,
                        onClick = { selectedBadge = badge }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.public_habits_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.habits.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_habits_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.habits.forEach { habit ->
                    FriendHabitCard(
                        emoji = habit.icon,
                        title = habit.name,
                        subtitle = stringResource(R.string.active_habit_subtitle),
                        streak = stringResource(R.string.profile_streak_format, habit.streak)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    selectedBadge?.let { badge ->
        BadgeDetailsDialog(badge = badge, onDismiss = { selectedBadge = null })
    }
}

@Composable
fun FriendUserHeaderSection(user: User?) {
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
                    initials = user?.initials ?: "??",
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
                    text = "${stringResource(R.string.profile_level)} ${user?.level ?: 1}",
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = user?.name ?: stringResource(R.string.unknown_player),
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
                        text = stringResource(R.string.profile_streak_format, user?.currentStreak ?: 0),
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
                        text = stringResource(R.string.profile_top_rank, 24),
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
fun FriendLevelAndXpBar(user: User?) {
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
fun FriendHabitCard(emoji: String, title: String, subtitle: String, streak: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$streak",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}