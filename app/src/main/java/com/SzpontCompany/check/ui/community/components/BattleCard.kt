package com.SzpontCompany.check.ui.community.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.Battle

@Composable
fun BattleCard(
    battle: Battle,
    onDoneClick: () -> Unit,
    onDetailsOrSurrenderClick: () -> Unit,
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
                Text(
                    text = battle.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Aktywna",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text("${battle.daysLeft} dni", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Gracze VS
            PlayerVsRow(
                myDays = battle.myDays,
                totalDays = battle.totalDays,
                myHp = battle.myHp,
                opponentName = battle.opponentName,
                opponentDays = battle.opponentDays,
                opponentHp = battle.opponentHp,
                opponentCompleted = battle.opponentCompleted
            )

            Spacer(Modifier.height(12.dp))

            // Zakład
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🪙", fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    "${battle.betAmount} monet · zakład",
                    color = Color(0xFFBA7517),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.weight(1f))
                if (battle.isLosingWarning) {
                    Text("Przegrywasz!", color = Color(0xFFE24B4A), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                } else if (battle.endDate != null) {
                    Text("kończy się ${battle.endDate}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Przyciski
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onDoneClick,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (battle.isDoneToday) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (battle.isDoneToday) "✓ Zrobione dziś" else "✓ Zrobione!",
                        color = if (battle.isDoneToday) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Button(
                    onClick = onDetailsOrSurrenderClick,
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (battle.isLosingWarning) "Poddaj się" else "Szczegóły",
                        color = if (battle.isLosingWarning) Color(0xFFE24B4A) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}