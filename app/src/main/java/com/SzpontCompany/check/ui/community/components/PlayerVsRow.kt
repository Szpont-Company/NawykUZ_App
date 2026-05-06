package com.SzpontCompany.check.ui.community.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.ui.theme.Crimson
import com.SzpontCompany.check.R
import androidx.compose.ui.res.stringResource
import com.SzpontCompany.check.ui.components.UserAvatar

@Composable
fun PlayerVsRow(
    myDays: Int,
    totalDays: Int,
    myHp: Int,
    myEmoji: String = "",
    myBgColor: String = "Mint",
    opponentName: String,
    opponentDays: Int,
    opponentHp: Int,
    opponentEmoji: String = "",
    opponentBgColor: String = "Mint",
    opponentCompleted: Boolean,
    modifier: Modifier = Modifier
) {

    val myNameStr = stringResource(R.string.you)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ty (lewa strona)
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            UserAvatar(
                avatarEmoji = myEmoji,
                initials = myNameStr,
                bgColor = myBgColor,
                size = 36.dp,
                emojiSize = 18f,
                initialsSize = 14f
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(myNameStr, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("$myDays/$totalDays", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))
            HpBar(hp = myHp, color = MaterialTheme.colorScheme.primary, showValue = true)
        }

        // VS
        Text(
            text = stringResource(R.string.battle_vs),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Przeciwnik
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            val opponentInitials = opponentName.trim().split("\\s+".toRegex())
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .take(2).joinToString("")
                .ifEmpty { "??" }

            UserAvatar(
                avatarEmoji = opponentEmoji,
                initials = opponentInitials,
                bgColor = opponentBgColor,
                size = 36.dp,
                emojiSize = 18f,
                initialsSize = 14f
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(opponentName, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(
                    text = "$opponentDays/$totalDays" + (if (opponentCompleted) " ✓" else ""),
                    color = if (opponentCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            HpBar(hp = opponentHp, color = Crimson, alignEnd = true, showValue = true)
        }
    }
}

@Composable
fun HpBar(hp: Int, color: Color, alignEnd: Boolean = false, showValue: Boolean = false) {
    val displayHp = hp.coerceIn(0, 100)
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayHp / 100f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color)
                    .align(if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart)
            )
        }
        if (showValue) {
            Text(
                text = stringResource(R.string.battle_hp_format, displayHp),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
