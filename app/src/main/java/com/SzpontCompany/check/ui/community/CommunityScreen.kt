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
import com.SzpontCompany.check.data.Battle
import com.SzpontCompany.check.data.ChallengeInvite
import com.SzpontCompany.check.data.Event
import com.SzpontCompany.check.data.RankingEntry
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications

@Composable
fun CommunityScreen(
    onProfileClick: () -> Unit = {}
) {
    val tabs = listOf("Battle", "Eventy", "Ranking", "Znajomi")
    var selectedTab by remember { mutableStateOf(0) }
    var selectedSubTab by remember { mutableStateOf(0) }
    val subTabs = listOf("Globalny", "Znajomi", "Tygodniowy")

    // --- Przykładowe dane ---
    val battles = listOf(
        Battle(
            title = "Codzienny spacer", daysLeft = 3,
            myDays = 6, totalDays = 7, myHp = 95,
            opponentName = "Kacper M.", opponentDays = 5, opponentHp = 60,
            opponentCompleted = false, betAmount = 50, endDate = "23 mar",
            isLosingWarning = false, isDoneToday = true
        ),
        Battle(
            title = "Czytanie 20 min", daysLeft = 5,
            myDays = 4, totalDays = 7, myHp = 55,
            opponentName = "Ania W.", opponentDays = 7, opponentHp = 100,
            opponentCompleted = true, betAmount = 100, endDate = null,
            isLosingWarning = true, isDoneToday = false
        )
    )

    val invites = listOf(
        ChallengeInvite(
            senderName = "Tomek K.",
            activityName = "Bieganie",
            durationMinutes = 30,
            days = 30,
            betAmount = 200
        )
    )

    val events = listOf(
        Event(
            title = "Globalny Marsz Marca",
            subtitle = "Łącznie 1 000 000 kroków",
            badgeText = "Global",
            themeColor = Color(0xFF00BFA5),
            progress = 0.67f,
            progressText = "672 450 / 1 000 000 kroków",
            timeText = "12 dni",
            participantsCount = "8 431 uczestników",
            buttonText = "Dołącz"
        ),
        Event(
            title = "Tydzień Czytania",
            subtitle = "7 dni z rzędu min. 20 min",
            badgeText = "Społeczność",
            themeColor = Color(0xFF8C9EFF),
            progress = 0.43f,
            progressText = "3 / 7 dni ukończono",
            timeText = "4 dni",
            rewardHighlight = "Odznaka \"Bookworm\" + 300 monet"
        ),
        Event(
            title = "Wiosenny Sprint",
            subtitle = "Rusza za 3 dni!",
            isSubtitleColored = true,
            badgeText = "Wkrótce",
            themeColor = Color(0xFFF57C00),
            description = "30-dniowe wyzwanie aktywności fizycznej.\nNagroda: ekskluzywna odznaka + 500 monet.",
            buttonText = "Przypomnij mi"
        )
    )

    val rankingEntries = listOf(
        RankingEntry(rank = 1, name = "Piotr K.", initials = "PK", xp = 4200, avatarColor = Color(0xFFD8912A)),
        RankingEntry(rank = 2, name = "Ania S.", initials = "AS", xp = 3800, avatarColor = Color(0xFF5E35B1)),
        RankingEntry(rank = 3, name = "Marek J.", initials = "MJ", xp = 3100, avatarColor = Color(0xFFE24B4A)),
        RankingEntry(rank = 14, name = "Ty", initials = "TY", xp = 1240, avatarColor = MaterialTheme.colorScheme.primary, isMe = true)
    )

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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { /* TODO: Otwórz powiadomienia */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Powiadomienia",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Czerwona kropka jeśli są zaproszenia
                    if (invites.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 8.dp)
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("MK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                    onClick = { selectedTab = index },
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

        // Zawartość zakładki Battle
        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Przyciski górne
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("+ Wyzwanie", fontWeight = FontWeight.Bold, fontSize = 15.sp) }

                        Button(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Znajdź graczy", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp) }
                    }
                }

                // Sekcja zaproszeń WYNIESIONA NA POCZĄTEK
                if (invites.isNotEmpty()) {
                    item {
                        Text(
                            "OCZEKUJĄCE ZAPROSZENIA (${invites.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(invites.size) { i ->
                        ChallengeInviteCard(
                            invite = invites[i],
                            onAccept = {},
                            onReject = {}
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

                items(battles.size) { i ->
                    BattleCard(
                        battle = battles[i],
                        onDoneClick = {},
                        onDetailsOrSurrenderClick = {}
                    )
                }
            }
        } else if (selectedTab == 1) { // Zakładka Eventy
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
        } else if (selectedTab == 2) { // Zakładka Ranking
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
                                    .clickable { selectedSubTab = index }
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
                    RankingListCard(entries = rankingEntries)
                }
            }
        } else if (selectedTab == 3) {
            FriendsCard(modifier = Modifier.fillMaxSize())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        CommunityScreen()
    }
}