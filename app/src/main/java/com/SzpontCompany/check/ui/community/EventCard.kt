package com.SzpontCompany.check.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.social.Event
import com.SzpontCompany.check.R
import androidx.compose.ui.res.stringResource

@Composable
fun EventCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                event.themeColor.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Lewy, gruby pasek koloru
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(event.themeColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Nagłówek (Tytuł + Pigułka)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = event.subtitle,
                            color = if (event.isSubtitleColored) event.themeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = if (event.isSubtitleColored) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // Pigułka (Badge)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = event.themeColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, event.themeColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = event.badgeText,
                            color = event.themeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Opis wydarzenia (np. "Wiosenny sprint")
                if (event.description != null) {
                    Text(
                        text = event.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Pasek postępu
                if (event.progress != null && event.progressText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(event.progress)
                                .fillMaxHeight()
                                .background(event.themeColor, RoundedCornerShape(4.dp))
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event.progressText,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (event.timeText != null) {
                            Text(
                                text = event.timeText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Blok wewnętrzny wyróżnienia nagrody (np. "Tydzień Czytania")
                if (event.rewardHighlight != null && event.description == null) {
                    Spacer(Modifier.height(8.dp)) // Lekko obniżony blok
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        val rewardLabel = stringResource(R.string.event_reward_label)
                        val rewardText = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                append(rewardLabel)
                            }
                            withStyle(style = SpanStyle(color = Color(0xFFBA7517), fontWeight = FontWeight.Medium)) {
                                append(event.rewardHighlight)
                            }
                        }
                        Text(text = rewardText, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Dolna sekcja ze społecznością i akcją / przyciskiem
                if (event.participantsCount != null || event.buttonText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (event.participantsCount == null) Arrangement.Start else Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (event.participantsCount != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Awatary znajomych / uczestników
                                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                                    AvatarPlaceholder("+", event.themeColor.copy(alpha = 0.2f), event.themeColor)
                                    AvatarPlaceholder("MK", MaterialTheme.colorScheme.primary, Color.White)
                                    AvatarPlaceholder("+8k", event.themeColor.copy(alpha = 0.2f), event.themeColor)
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = event.participantsCount,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (event.buttonText != null) {
                            OutlinedButton(
                                onClick = { /* W to miejsce trafi nawigacja dołączenia */ },
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = event.themeColor),
                                border = BorderStroke(1.dp, event.themeColor),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text(event.buttonText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarPlaceholder(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}
