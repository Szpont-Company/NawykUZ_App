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
import com.SzpontCompany.check.ui.community.components.BattleCard
import com.SzpontCompany.check.ui.community.components.ChallengeInviteCard
import com.SzpontCompany.check.ui.community.components.EventCard
import com.SzpontCompany.check.data.Battle
import com.SzpontCompany.check.data.ChallengeInvite
import com.SzpontCompany.check.data.Event
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint

@Composable
fun CommunityScreen() {
    val tabs = listOf("Battle", "Eventy", "Ranking", "Znajomi")
    var selectedTab by remember { mutableStateOf(0) }

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
            themeColor = Color(0xFF00BFA5), // Mint Green
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
            themeColor = Color(0xFF8C9EFF), // Lighter Deep Purple
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
            themeColor = Color(0xFFF57C00), // Orange
            description = "30-dniowe wyzwanie aktywności fizycznej.\nNagroda: ekskluzywna odznaka + 500 monet.",
            buttonText = "Przypomnij mi"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Tytuł
        Text(
            text = "Społeczność",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) { Text("+ Rzuć wyzwanie", fontWeight = FontWeight.SemiBold) }
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) { Text("Znajdź graczy", fontWeight = FontWeight.SemiBold) }
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

                // Sekcja zaproszeń
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) { // nazwa Twojego Theme z Theme.kt
        CommunityScreen()
    }
}