package com.SzpontCompany.check.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.SzpontCompany.check.ui.community.components.ChallengeInviteCard
import com.SzpontCompany.check.ui.community.components.YourPositionCard
import com.SzpontCompany.check.ui.community.components.NotificationsSheet
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import com.SzpontCompany.check.ui.theme.getColorByName
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.data.social.Friend
import com.SzpontCompany.check.ui.community.components.CreateChallengeSheet
import com.SzpontCompany.check.ui.profile.ProfileViewModel
import kotlinx.coroutines.coroutineScope
import android.widget.Toast
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.SzpontCompany.check.data.social.Battle

@Composable
fun CommunityScreen(
    onProfileClick: () -> Unit = {},
    onFriendProfileClick: () -> Unit = {},
    onMessageClick: (Friend) -> Unit = {},
    onBattleClick: (Battle) -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
    friendsViewModel: FriendsViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user

    val battles by communityViewModel.battles.collectAsState()
    val incomingInvites by communityViewModel.incomingInvites.collectAsState()
    val events by communityViewModel.events.collectAsState()
    val rankingEntries by communityViewModel.rankingEntries.collectAsState()

    val tabs = listOf("Battle", "Eventy", "Ranking", "Znajomi")
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedSubTab by remember { mutableStateOf(0) }
    var friendsSubTab by remember { mutableStateOf(0) }
    var showNotifications by remember { mutableStateOf(false) }
    val subTabs = listOf("Globalny", "Znajomi", "Tygodniowy")
    val haptic = LocalHapticFeedback.current

    var showCreateChallengeSheet by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val dynamicRankingEntries = rankingEntries.map {
        if (it.isMe) it.copy(avatarColor = primaryColor) else it
    }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Tytuł i header profilowy
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Społeczność",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { showNotifications = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Powiadomienia",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Czerwona kropka jeśli są zaproszenia
                    if (incomingInvites.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-10).dp, y = 10.dp)
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(getColorByName(user?.bgColor ?: "Mint"))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    val displayAvatar = if (user?.avatarEmoji.isNullOrEmpty()) user?.initials ?: "MK" else user?.avatarEmoji ?: ""
                    Text(displayAvatar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Zakładki
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = index
                    },
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zawartość zakładki
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "community_tab_transition"
        ) { tab ->
            when (tab) {
                0 -> { // Zakładka Battle
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Przyciski górne
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showCreateChallengeSheet = true
                                              },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("+ Wyzwanie", fontWeight = FontWeight.Bold, fontSize = 15.sp) }

                                Button(
                                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(16.dp)
                                ) { Text("Znajdź graczy", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp) }
                            }
                        }

                        // Sekcja zaproszeń
                        if (incomingInvites.isNotEmpty()) {
                            item {
                                Text(
                                    "OCZEKUJĄCE ZAPROSZENIA (${incomingInvites.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(incomingInvites.size) { i ->
                                ChallengeInviteCard(
                                    invite = incomingInvites[i],
                                    onAccept = {
                                        communityViewModel.acceptInvite(incomingInvites[i], user)
                                        Toast.makeText(context, "Bitwa rozpoczęta! Sprawdź ekran Dzisiaj.", Toast.LENGTH_SHORT).show()
                                    },
                                    onReject = {
                                        communityViewModel.rejectInvite(incomingInvites[i])
                                    }
                                )
                            }
                        }

                        // Sekcja aktywnych bitew
                        item {
                            Text(
                                "AKTYWNE BITWY (${battles.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(
                            items = battles,
                            key = { battle -> battle.id }
                        ) { battle ->
                            BattleCard(
                                battle = battle,
                                currentUserId = user?.uid ?: "",
                                onDoneClick = { isNowDone ->
                                    communityViewModel.toggleBattleDone(battle, isNowDone)
                                },
                                onDetailsOrSurrenderClick = {
                                    onBattleClick(battle)
                                },
                                onAcknowledgeClick = {
                                    communityViewModel.acknowledgeBattle(battle)
                                }
                            )
                        }
                    }
                }
                1 -> { // Zakładka Eventy
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Text(
                                "AKTYWNE EVENTY",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(events.size) { i ->
                            EventCard(event = events[i])
                        }
                    }
                }
                2 -> { // Zakładka Ranking
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            // Pod-menu kafelkowe (Sub-tabs)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                subTabs.forEachIndexed { index, title ->
                                    val isSelected = selectedSubTab == index
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedSubTab = index
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            YourPositionCard(
                                rank = 14,
                                xp = 1240,
                                level = 8,
                                trendText = "+3 od zeszłego tyg."
                            )
                        }

                        item {
                            RankingListCard(entries = dynamicRankingEntries)
                        }
                    }
                }
                3 -> { // Zakładka Znajomi
                    FriendsCard(
                       selectedSubTab = friendsSubTab,
                        onSubTabSelected = { friendsSubTab = it },
                        modifier = Modifier.fillMaxSize(),
                        onFriendProfileClick = { onFriendProfileClick() },
                        onMessageClick = { friend -> onMessageClick(friend) }
                    )
                }
            }
        }
    }

    if (showNotifications) {
        NotificationsSheet(
            onDismiss = { showNotifications = false }
        )
    }

    if (showCreateChallengeSheet) {
        val friendsState by friendsViewModel.uiState.collectAsState()
        val friendsList = friendsState.activeFriends + friendsState.offlineFriends

        CreateChallengeSheet(
            friendsList = friendsList,
            onDismiss = { showCreateChallengeSheet = false },
            onAddFriendClick = {
                selectedTab = 3
                friendsSubTab = 2
            },
            onSendChallenge = { friend, template, betAmount ->
                // Tu w przyszłości dodamy backend
                showCreateChallengeSheet = false
                communityViewModel.sendChallenge(user, friend, template, betAmount)
                Toast.makeText(
                    context,
                    "Wyzwanie rzucone: ${friend.name}!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        CommunityScreen()
    }
}
