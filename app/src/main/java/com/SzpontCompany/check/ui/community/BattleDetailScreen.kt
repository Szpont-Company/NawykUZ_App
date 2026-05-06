package com.SzpontCompany.check.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
import com.SzpontCompany.check.ui.theme.getColorByName
import com.SzpontCompany.check.ui.components.CheckBackButton
import com.SzpontCompany.check.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleDetailScreen(
    battle: Battle,
    currentUserId: String,
    onBackClick: () -> Unit,
    onMarkDoneClick: () -> Unit,
    onSurrenderClick: () -> Unit,
    onAcknowledgeClick: () -> Unit = {}
) {
    val isPlayer1 = battle.player1Id == currentUserId
    val todayString = java.time.LocalDate.now().toString()

    val myLastLogDate = if (isPlayer1) battle.player1LastLogDate else battle.player2LastLogDate
    val myHp = if (isPlayer1) battle.player1Hp else battle.player2Hp
    val myDays = if (isPlayer1) battle.player1Days else battle.player2Days
    val myEmoji = if (isPlayer1) battle.player1Emoji else battle.player2Emoji
    val myBgColor = if (isPlayer1) battle.player1BgColor else battle.player2BgColor
    val myCompletedToday = myLastLogDate == todayString

    val opponentName = if (isPlayer1) battle.player2Name else battle.player1Name
    val opponentHp = if (isPlayer1) battle.player2Hp else battle.player1Hp
    val opponentDays = if (isPlayer1) battle.player2Days else battle.player1Days
    val opponentEmoji = if (isPlayer1) battle.player2Emoji else battle.player1Emoji
    val opponentBgColor = if (isPlayer1) battle.player2BgColor else battle.player1BgColor
    
    val daysLeft = maxOf(0, battle.totalDays - maxOf(battle.player1Days, battle.player2Days))
    val isFinished = battle.status == "COMPLETED" || battle.status == "SURRENDERED"
    val isWinner = battle.winnerId == currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.battle_details_title), fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = getLocalizedHabitName(battle.title),
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
                        text = stringResource(R.string.battle_check_coins, battle.betAmount),
                        color = Amber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

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
                        PlayerDetailColumn(
                            name = stringResource(R.string.you),
                            avatarEmoji = myEmoji,
                            bgColorName = myBgColor,
                            hp = myHp,
                            days = myDays,
                            totalDays = battle.totalDays,
                            hpColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

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

                        PlayerDetailColumn(
                            name = opponentName,
                            avatarEmoji = opponentEmoji,
                            bgColorName = opponentBgColor,
                            hp = opponentHp,
                            days = opponentDays,
                            totalDays = battle.totalDays,
                            hpColor = Crimson,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    InfoBadge(title = stringResource(R.string.battle_remaining_label),
                        value = stringResource(R.string.battle_days_left, daysLeft)
                    )
                    InfoBadge(
                        title = stringResource(R.string.battle_end_label),
                        value = battle.endDate ?: stringResource(R.string.battle_soon),
                        valueColor = if (daysLeft <= 1) Crimson else MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (battle.status == "ACTIVE") {
                    if (!myCompletedToday) {
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
                            Text(stringResource(R.string.battle_mark_done), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
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
                                Text(stringResource(R.string.battle_done_for_today), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                        Text(stringResource(R.string.battle_surrender_give_coins), fontWeight = FontWeight.Bold)
                    }
                } else {
                    val resultColor = if (isWinner) Color(0xFFD8912A) else Crimson
                    val resultTitle = if (isWinner) stringResource(R.string.battle_victory_title) else stringResource(R.string.battle_defeat_title)
                    val resultMessage = if (isWinner)
                        stringResource(R.string.battle_victory_message, opponentName, battle.betAmount * 2)
                        else stringResource(R.string.battle_defeat_message, opponentName)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(resultColor.copy(alpha = 0.1f))
                            .border(1.dp, resultColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(resultTitle, fontWeight = FontWeight.ExtraBold, color = resultColor, fontSize = 20.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(resultMessage, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onAcknowledgeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = resultColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (isWinner) stringResource(R.string.battle_claim_reward) else stringResource(R.string.battle_understood),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PlayerDetailColumn(
    name: String,
    avatarEmoji: String,
    bgColorName: String,
    hp: Int,
    days: Int,
    totalDays: Int,
    hpColor: Color,
    modifier: Modifier = Modifier
) {
    val myNameStr = stringResource(R.string.you)
    val initials = if (name == myNameStr) myNameStr else {
        name.trim().split("\\s+".toRegex()).mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    }
    val displayAvatar = if (avatarEmoji.isEmpty()) initials else avatarEmoji

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(getColorByName(bgColorName)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayAvatar,
                color = if (avatarEmoji.isEmpty()) Color.White else Color.Unspecified,
                fontSize = if (avatarEmoji.isEmpty()) (if (initials == myNameStr) 20.sp else 24.sp) else 32.sp,
                fontWeight = FontWeight.Bold
            )
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

        val displayHp = hp.coerceIn(0, 100)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "HP", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Text(text = "$displayHp/100", fontSize = 11.sp, color = hpColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { displayHp.toFloat() / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = hpColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.days_label), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        id = "preview_1",
        title = "Codzienny spacer",
        totalDays = 7,
        betAmount = 50,
        endDate = "23 mar",
        player1Id = "my_id",
        player1Name = "Ty",
        player1Emoji = "🏃",
        player1BgColor = "Mint",
        player1Hp = 95,
        player1Days = 6,
        player2Id = "enemy_id",
        player2Name = "Kacper M.",
        player2Emoji = "",
        player2BgColor = "Coral",
        player2Hp = 25,
        player2Days = 5
    )
    CheckTheme(darkTheme = true, accent = Mint) {
        BattleDetailScreen(
            battle = sampleBattle,
            currentUserId = "my_id",
            onBackClick = {},
            onMarkDoneClick = {},
            onSurrenderClick = {}
        )
    }
}
