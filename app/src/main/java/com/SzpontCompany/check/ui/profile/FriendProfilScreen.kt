package com.SzpontCompany.check.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.badges.Badge
import com.SzpontCompany.check.data.badges.BadgeProvider
import com.SzpontCompany.check.ui.components.CheckBackButton
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import com.SzpontCompany.check.ui.theme.getColorByName

enum class FriendshipStatus {
    NONE, PENDING, FRIENDS
}

@Composable
fun FriendProfileScreen(
    onBackClick: () -> Unit = {},
    isInitiallyPrivate: Boolean = false,
    privacySetting: String = "FRIENDS_ONLY"
) {
    var friendshipStatus by remember { mutableStateOf(FriendshipStatus.NONE) }
    var selectedBadge by remember { mutableStateOf<Badge?>(null) }
    var showRemoveFriendDialog by remember { mutableStateOf(false) }

    val isProfileLocked = isInitiallyPrivate && friendshipStatus != FriendshipStatus.FRIENDS

    val friendName = "Anna Nowak"
    val friendNick = "@annanowak"
    val friendEmoji = "🦊"
    val friendBgColor = "Lavender"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            CheckBackButton(onClick = onBackClick)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Profil użytkownika",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        FriendUserHeaderSection(
            friendName = friendName,
            friendNick = friendNick,
            friendEmoji = friendEmoji,
            friendBgColor = friendBgColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val btnText = when(friendshipStatus) {
                FriendshipStatus.NONE -> "Dodaj"
                FriendshipStatus.PENDING -> "Wysłano zaproszenie"
                FriendshipStatus.FRIENDS -> "Znajomi"
            }

            val btnIcon = when(friendshipStatus) {
                FriendshipStatus.NONE -> Icons.Default.PersonAdd
                FriendshipStatus.PENDING -> Icons.Outlined.Schedule
                FriendshipStatus.FRIENDS -> Icons.Default.Person
            }

            val btnContainerColor = when(friendshipStatus) {
                FriendshipStatus.NONE -> MaterialTheme.colorScheme.primary
                FriendshipStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
                FriendshipStatus.FRIENDS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            }

            val btnContentColor = when(friendshipStatus) {
                FriendshipStatus.NONE -> MaterialTheme.colorScheme.onPrimary
                FriendshipStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
                FriendshipStatus.FRIENDS -> MaterialTheme.colorScheme.primary
            }

            Button(
                onClick = {
                    when(friendshipStatus) {
                        FriendshipStatus.NONE -> friendshipStatus = FriendshipStatus.PENDING
                        FriendshipStatus.PENDING -> friendshipStatus = FriendshipStatus.FRIENDS
                        FriendshipStatus.FRIENDS -> showRemoveFriendDialog = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = btnContainerColor,
                    contentColor = btnContentColor
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = btnIcon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = btnText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = { /* TODO interakcja */ },
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Text("👋", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isProfileLocked) {
            PrivateProfilState(isFriendsOnly = privacySetting == "FRIENDS_ONLY")
        } else {
            FriendLevelAndXpBar()

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(modifier = Modifier.weight(1f), targetValue = 28, label = "Nawyki")
                    StatCard(modifier = Modifier.weight(1f), targetValue = 12, label = "Dni w rzędzie", valueColor = MaterialTheme.colorScheme.primary)
                    StatCard(modifier = Modifier.weight(1f), targetValue = 420, label = "Monety", valueColor = Color(0xFFBA7517))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(modifier = Modifier.weight(1f), targetValue = 7, label = "Wygrane")
                    StatCard(modifier = Modifier.weight(1f), targetValue = 78, suffix = "%", label = "Skuteczność")
                    StatCard(modifier = Modifier.weight(1f), targetValue = 12, label = "Znajomi")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Zdobyte odznaki",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val friendBadges = BadgeProvider.allBadges.take(3).map {
                it.copy(isUnlocked = true)
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
                text = "Publiczne Nawyki",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            FriendHabitCard(emoji = "🏃", title = "Bieganie", subtitle = "codziennie", streak = "12 dni")
            Spacer(modifier = Modifier.height(12.dp))
            FriendHabitCard(emoji = "📚", title = "Czytam książkę", subtitle = "30 min", streak = "5 dni")
            Spacer(modifier = Modifier.height(12.dp))
            FriendHabitCard(emoji = "💧", title = "Piję wodę", subtitle = "2 litry", streak = "24 dni")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    selectedBadge?.let { badge ->
        BadgeDetailsDialog(badge = badge, onDismiss = { selectedBadge = null })
    }

    if (showRemoveFriendDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveFriendDialog = false },
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(26.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Usuń ze znajomych",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            },
            text = {
                Text(
                    text = "Czy na pewno chcesz usunąć użytkownika $friendName ze swoich znajomych? Ta akcja jest nieodwracalna.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        friendshipStatus = FriendshipStatus.NONE
                        showRemoveFriendDialog = false
                    },
                    modifier = Modifier.padding(bottom = 8.dp, end = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24B4A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Usuń",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveFriendDialog = false },
                    modifier = Modifier.padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Anuluj",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun FriendUserHeaderSection(friendName: String, friendNick: String, friendEmoji: String, friendBgColor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(getColorByName(friendBgColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = friendEmoji, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 4.dp)
                    .background(Color(0xFFBA7517), RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Lvl 6", color = MaterialTheme.colorScheme.background, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = friendName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$friendNick • Check.",
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
                    Text(text = "🔥 12 dni", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFBA7517).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFBA7517), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Top 24", color = Color(0xFFBA7517), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun FriendLevelAndXpBar() {
    var animationPlayed by remember { mutableStateOf(false) }

    val currentLevel = 6
    val targetXp = 800
    val maxXp = 1000
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
                    text = "Poziom $currentLevel",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$animatedXp / $maxXp PD",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Do Lvl ${currentLevel + 1}",
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
                text = "🔥 $streak",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PrivateProfilState(
    modifier: Modifier = Modifier,
    isFriendsOnly: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Prywatny profil",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Konto jest prywatne",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFriendsOnly)
                "Zaproś tego gracza do znajomych, aby zobaczyć jego statystyki i nawyki."
            else
                "Ten użytkownik ukrył swoje szczegóły profilu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FriendProfileScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        FriendProfileScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun FriendProfilePrivatePreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        FriendProfileScreen(isInitiallyPrivate = true)
    }
}
