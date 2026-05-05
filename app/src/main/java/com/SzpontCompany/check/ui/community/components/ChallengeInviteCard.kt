package com.SzpontCompany.check.ui.community.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.social.ChallengeInvite
import androidx.compose.foundation.background
import com.SzpontCompany.check.ui.theme.getColorByName
import com.SzpontCompany.check.R
import androidx.compose.ui.res.stringResource

@Composable
fun ChallengeInviteCard(
    invite: ChallengeInvite,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zastępczy awatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(getColorByName(invite.senderBgColor), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val displayContent = when {
                        invite.senderEmoji.isNotEmpty() -> invite.senderEmoji
                        invite.senderInitials.isNotEmpty() -> invite.senderInitials
                        invite.senderName.isNotEmpty() -> invite.senderName.take(1).uppercase()
                        else -> "?"
                    }
                    Text(
                        text = displayContent,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = invite.senderName.ifEmpty { stringResource(R.string.challenge_invite_stranger) },
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.challenge_invite_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Info o wyzwaniu - zaktualizowane do nowego modelu Firebase
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.challenge_invite_habit_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text(invite.habitName, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.challenge_invite_stake_label), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text(
                        stringResource(R.string.challenge_invite_coins, invite.stake),
                        color = Color(0xFFBA7517),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Akcje
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE24B4A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE24B4A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.decline), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.accept), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}