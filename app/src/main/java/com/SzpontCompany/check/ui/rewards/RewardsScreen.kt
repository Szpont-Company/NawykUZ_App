package com.SzpontCompany.check.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint

val PremiumGold = Color(0xFFC78C18)
val DarkGoldBackground = Color(0x33C78C18)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nagrody", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            CoinsCard(currentCoins = 850, totalCoins = 3240)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("ODZNAKI")
                BadgesGrid()
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("SKLEP Z NAGRODAMI")
                StoreItem(
                    emoji = "🎨",
                    title = "Kolor akcentu — Złoty",
                    subtitle = "Motyw premium dla profilu",
                    price = "200 C"
                )
                StoreItem(
                    emoji = "🛡️",
                    title = "Tarcza HP — ochrona x1",
                    subtitle = "Chroni przed karą w Battle",
                    price = "150 C"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CoinsCard(currentCoins: Int, totalCoins: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGoldBackground)
            .border(1.5.dp, PremiumGold, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Twoje monety", color = PremiumGold, fontSize = 14.sp)
                Text(text = currentCoins.toString(), color = PremiumGold, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Łącznie zdobyte", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(text = totalCoins.toString(), color = PremiumGold, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun BadgesGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeCard(Modifier.weight(1f), "🏆", "Pierwszy tydzień", true)
            BadgeCard(Modifier.weight(1f), "🔥", "Streak 21 dni", true)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BadgeCard(Modifier.weight(1f), "⚔️", "Battle Winner", false, "Wygraj 5 bitew")
            BadgeCard(Modifier.weight(1f), "💎", "Diamentowy", false, "Streak 100 dni")
        }
    }
}

@Composable
fun BadgeCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    isUnlocked: Boolean,
    requirement: String = ""
) {
    val bgColor = if (isUnlocked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 32.sp)

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isUnlocked) {
            Text(text = "Odblokowana!", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(text = requirement, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
fun StoreItem(emoji: String, title: String, subtitle: String, price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 28.sp)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = price, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RewardsScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        RewardsScreen{}
    }
}