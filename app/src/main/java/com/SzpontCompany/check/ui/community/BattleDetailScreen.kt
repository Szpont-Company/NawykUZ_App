package com.SzpontCompany.check.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
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
import com.SzpontCompany.check.data.social.Battle
import com.SzpontCompany.check.ui.theme.Amber
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Crimson
import com.SzpontCompany.check.ui.theme.Mint
import com.SzpontCompany.check.ui.components.CheckBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleDetailScreen(
    battle: Battle,
    onBackClick: () -> Unit,
    onMarkDoneClick: () -> Unit,
    onSurrenderClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły Bitwy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    CheckBackButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- NAGŁÓWEK WYZWANIA ---
            Text(
                text = battle.title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Amber.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "🪙", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${battle.betAmount} Check Coins",
                    color = Amber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SEKCJA VS (Karta z graczami) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gracz 1 (Ty)
                    PlayerDetailColumn(
                        name = "Ty",
                        avatarText = "TY",
                        hp = battle.myHp,
                        days = battle.myDays,
                        totalDays = battle.totalDays,
                        avatarColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    // Znaczek VS
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("VS", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }

                    // Gracz 2 (Przeciwnik)
                    PlayerDetailColumn(
                        name = battle.opponentName,
                        avatarText = battle.opponentName.take(2).uppercase(),
                        hp = battle.opponentHp,
                        days = battle.opponentDays,
                        totalDays = battle.totalDays,
                        avatarColor = Color(0xFFD85A30),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- DODATKOWE INFORMACJE ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                InfoBadge(title = "Pozostało", value = "${battle.daysLeft} dni")
                InfoBadge(
                    title = "Koniec",
                    value = battle.endDate ?: "Nieznany",
                    valueColor = if (battle.daysLeft <= 1) Crimson else MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- PRZYCISKI AKCJI ---
            if (!battle.isDoneToday) {
                Button(
                    onClick = onMarkDoneClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Oznacz jako zrobione", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                // Zrobione dzisiaj stan
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zrobione na dzisiaj!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onSurrenderClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = Crimson)
            ) {
                Icon(Icons.Rounded.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Poddaj się (Oddaj monety)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PlayerDetailColumn(
    name: String,
    avatarText: String,
    hp: Int,
    days: Int,
    totalDays: Int,
    avatarColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = avatarText, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Customowy pasek HP dla detali
        Text(text = "HP: $hp", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { hp / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (hp > 30) Mint else Crimson,
            trackColor = MaterialTheme.colorScheme.background,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress dni
        Text(text = "Dni", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "$days / $totalDays",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun InfoBadge(title: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onBackground) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Preview(showBackground = true)
@Composable
fun BattleDetailScreenPreview() {
    val sampleBattle = Battle(
        title = "Codzienny spacer", daysLeft = 3,
        myDays = 6, totalDays = 7, myHp = 95,
        opponentName = "Kacper M.", opponentDays = 5, opponentHp = 25,
        opponentCompleted = false, betAmount = 50, endDate = "23 mar",
        isLosingWarning = false, isDoneToday = false
    )
    CheckTheme(darkTheme = true, accent = Mint) {
        BattleDetailScreen(
            battle = sampleBattle,
            onBackClick = {},
            onMarkDoneClick = {},
            onSurrenderClick = {}
        )
    }
}