package com.SzpontCompany.check.ui.community.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerVsRow(
    myDays: Int,
    totalDays: Int,
    myHp: Int,
    opponentName: String,
    opponentDays: Int,
    opponentHp: Int,
    opponentCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ty (lewa strona)
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ty", color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("$myDays/$totalDays dni", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(Modifier.height(4.dp))
            HpBar(hp = myHp, color = MaterialTheme.colorScheme.primary)
        }

        // VS
        Text(
            text = "VS",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Przeciwnik
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(opponentName, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    if (opponentCompleted) "Zrobione" else "$opponentDays/$totalDays dni",
                    color = if (opponentCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(4.dp))
            HpBar(hp = opponentHp, color = Color(0xFFE24B4A), alignEnd = true)
        }
    }
}