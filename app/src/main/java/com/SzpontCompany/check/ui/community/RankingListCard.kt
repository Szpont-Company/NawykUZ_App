package com.SzpontCompany.check.ui.community

import androidx.compose.foundation.background
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
import com.SzpontCompany.check.data.social.RankingEntry
import java.util.Locale

@Composable
fun RankingListCard(
    entries: List<RankingEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            entries.forEachIndexed { index, entry ->
                val formattedXp = String.format(Locale.US, "%,d", entry.xp).replace(',', ' ')
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Miejsce (medale lub liczba)
                    Box(modifier = Modifier.width(30.dp), contentAlignment = Alignment.Center) {
                        when (entry.rank) {
                            1 -> Text("🥇", fontSize = 18.sp)
                            2 -> Text("🥈", fontSize = 18.sp)
                            3 -> Text("🥉", fontSize = 18.sp)
                            else -> Text(
                                "${entry.rank}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Awatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(entry.avatarColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = entry.initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // Imię/nick
                    Text(
                        text = entry.name,
                        color = if (entry.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )

                    // XP (w jednej linii, ze spacją jako separator tysięcy)
                    Text(
                        text = "$formattedXp XP",
                        color = if (entry.isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Separator wewnątrz listy oprócz ostatniego elementu
                if (index < entries.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.background,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

