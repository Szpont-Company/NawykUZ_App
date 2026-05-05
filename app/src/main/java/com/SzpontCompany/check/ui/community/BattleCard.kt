package com.SzpontCompany.check.ui.community

import androidx.compose.animation.animateColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.social.Battle
import com.SzpontCompany.check.ui.community.components.PlayerVsRow
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.SzpontCompany.check.ui.theme.getColorByName
import com.SzpontCompany.check.R

@Composable
fun BattleCard(
    battle: Battle,
    currentUserId: String,
    onDoneClick: (Boolean) -> Unit,
    onDetailsOrSurrenderClick: () -> Unit,
    onAcknowledgeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val isPlayer1 = battle.player1Id == currentUserId
    val todayString = java.time.LocalDate.now().toString()

    val isFinished = battle.status == "COMPLETED" || battle.status == "SURRENDERED"
    var showResultDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val myHp = if (isPlayer1) battle.player1Hp else battle.player2Hp
    val myDays = if (isPlayer1) battle.player1Days else battle.player2Days
    val myEmoji = if (isPlayer1) battle.player1Emoji else battle.player2Emoji
    val myBgColor = if (isPlayer1) battle.player1BgColor else battle.player2BgColor
    val isDoneToday = if (isPlayer1) battle.player1LastLogDate == todayString else battle.player2LastLogDate == todayString

    val opponentName = if (isPlayer1) battle.player2Name else battle.player1Name
    val opponentHp = if (isPlayer1) battle.player2Hp else battle.player1Hp
    val opponentDays = if (isPlayer1) battle.player2Days else battle.player1Days
    val opponentEmoji = if (isPlayer1) battle.player2Emoji else battle.player1Emoji
    val opponentBgColor = if (isPlayer1) battle.player2BgColor else battle.player1BgColor
    val opponentCompleted = if (isPlayer1) battle.player2LastLogDate == todayString else battle.player1LastLogDate == todayString

    val daysLeft = maxOf(0, battle.totalDays - maxOf(battle.player1Days, battle.player2Days))

    val isLosingWarning = myHp < opponentHp && myHp < 50 && battle.status == "ACTIVE"
    val isWinner = battle.winnerId == currentUserId

    if (showResultDialog) {
        val rewardAmount = battle.betAmount * 2
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = {
                Text(
                    text = if (isWinner) stringResource(R.string.battle_victory_title) else stringResource(R.string.battle_defeat_title),
                    fontWeight = FontWeight.Bold,
                    color = if (isWinner) Color(0xFFD8912A) else Color(0xFFE24B4A)
                )
            },
            text = {
                Text(
                    text = if (isWinner) {
                        stringResource(R.string.battle_victory_message, opponentName, rewardAmount)
                    } else {
                        stringResource(R.string.battle_defeat_message, opponentName)
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        onAcknowledgeClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isWinner) Color(0xFFD8912A) else MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (isWinner) stringResource(R.string.battle_claim_reward) else stringResource(R.string.battle_understood),
                        color = Color.White
                    )
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikona wyzwania
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(getColorByName(battle.habitColorName).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = battle.habitIcon, fontSize = 18.sp)
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = battle.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when(battle.status) {
                        "ACTIVE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        "SURRENDERED" -> Color(0xFFE24B4A).copy(alpha = 0.15f)
                        else -> Color(0xFFD8912A).copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = when(battle.status) {
                            "ACTIVE" -> stringResource(R.string.battle_status_active)
                            "SURRENDERED" -> stringResource(R.string.battle_status_surrendered)
                            else -> stringResource(R.string.battle_status_completed)
                        },
                        color = when(battle.status) {
                            "ACTIVE" -> MaterialTheme.colorScheme.primary
                            "SURRENDERED" -> Color(0xFFE24B4A)
                            else -> Color(0xFFD8912A)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.battle_days_left, daysLeft),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Gracze VS
            PlayerVsRow(
                myDays = myDays,
                totalDays = battle.totalDays,
                myHp = myHp,
                myEmoji = myEmoji,
                myBgColor = myBgColor,
                opponentName = opponentName,
                opponentDays = opponentDays,
                opponentHp = opponentHp,
                opponentEmoji = opponentEmoji,
                opponentBgColor = opponentBgColor,
                opponentCompleted = opponentCompleted
            )

            Spacer(Modifier.height(12.dp))

            // Zaklad
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🪙", fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.battle_bet_amount, battle.betAmount),
                    color = Color(0xFFBA7517),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.weight(1f))
                if (isLosingWarning) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_scale"
                    )
                    val pulseColor by infiniteTransition.animateColor(
                        initialValue = Color(0xFFE24B4A),
                        targetValue = Color(0xFFFF8A80),
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_color"
                    )

                    Text(
                        text = stringResource(R.string.battle_losing_warning),
                        color = pulseColor,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.scale(pulseScale)
                    )
                } else if (battle.endDate != null) {
                    Text(
                        text = stringResource(R.string.battle_ends_on, battle.endDate),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Przyciski
            if (isFinished) {
                Button(
                    onClick = { showResultDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWinner) Color(0xFFD8912A) else Color(0xFFE24B4A)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isWinner) {
                            stringResource(R.string.battle_claim_reward_with_amount, battle.betAmount * 2)
                        } else {
                            stringResource(R.string.battle_see_summary)
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDoneClick(!isDoneToday)
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDoneToday) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isDoneToday) stringResource(R.string.battle_done_today) else stringResource(R.string.battle_done),
                            color = if (isDoneToday) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDetailsOrSurrenderClick()
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val labelText = if (isLosingWarning) stringResource(R.string.battle_give_up) else stringResource(R.string.details)
                        Text(
                            labelText,
                            color = if (isLosingWarning) Color(0xFFE24B4A) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
