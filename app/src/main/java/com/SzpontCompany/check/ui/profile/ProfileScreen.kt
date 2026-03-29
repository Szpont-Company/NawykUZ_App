package com.SzpontCompany.check.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import com.SzpontCompany.check.data.BadgeProvider

@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit = {},
    onRewardsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        ProfileTopBar()
        Spacer(modifier = Modifier.height(24.dp))

        UserHeaderSection()
        Spacer(modifier = Modifier.height(24.dp))

        LevelAndXpBar()
        Spacer(modifier = Modifier.height(24.dp))

        StatsGridSection()
        Spacer(modifier = Modifier.height(24.dp))

        BadgesSection()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.profile_settings_header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        SettingsSection(
            onSettingsClick = onSettingsClick,
            onRewardsClick = onRewardsClick
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ProfileTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Outlined.FilterAlt, contentDescription = "Filtruj", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Outlined.PersonOutline, contentDescription = "Profil", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun UserHeaderSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "MK", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 4.dp)
                    .background(Color(0xFFBA7517), RoundedCornerShape(12.dp))
                    .border(2.dp, MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Lvl 8", color = MaterialTheme.colorScheme.background, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = "Marek Kowalski", // zostawiawmy hardcored, jak bedzie baza zmienimy
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "@marekk • Check.", // Tez hardcored
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("🔥 21-dniowy streak", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFBA7517).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFBA7517), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Top 14", color = Color(0xFFBA7517), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun LevelAndXpBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${stringResource(R.string.profile_level)} 8",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "1 240 / 1 600 XP",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "→ ${stringResource(R.string.profile_level)} 9",
                color = Color(0xFFBA7517),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.77f)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFD85A30), Color(0xFFBA7517))
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun StatsGridSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(modifier = Modifier.weight(1f), value = "34", label = stringResource(R.string.profile_habits))
            StatCard(modifier = Modifier.weight(1f), value = "21", label = stringResource(R.string.profile_streak_days), valueColor = MaterialTheme.colorScheme.primary)
            StatCard(modifier = Modifier.weight(1f), value = "850", label = stringResource(R.string.profile_coins), valueColor = Color(0xFFBA7517))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(modifier = Modifier.weight(1f), value = "7", label = stringResource(R.string.profile_battles_won))
            StatCard(modifier = Modifier.weight(1f), value = "78%", label = stringResource(R.string.profile_effectiveness))
            StatCard(modifier = Modifier.weight(1f), value = "12", label = stringResource(R.string.profile_friends))
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, valueColor: Color = MaterialTheme.colorScheme.onBackground) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SettingsSection(
    onSettingsClick: () -> Unit,
    onRewardsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
    ) {
        SettingsItem(
            icon = Icons.Default.Person,
            iconTint = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.profile_edit),
            onClick = { /* TODO */ }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

        SettingsItem(
            icon = Icons.Default.EmojiEvents,
            iconTint = Color(0xFFBA7517),
            title = stringResource(R.string.profile_rewards),
            onClick = onRewardsClick
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.background, thickness = 2.dp)

        SettingsItem(
            icon = Icons.Default.Settings,
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            title = stringResource(R.string.profile_settings),
            onClick = onSettingsClick
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp))
            .clickable { /* TODO: Wyloguj */ }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.profile_logout),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SettingsItem(icon: ImageVector, iconTint: Color, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun BadgesSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.profile_badges_header),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val sortedBadges = BadgeProvider.allBadges.sortedByDescending { it.isUnlocked }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(sortedBadges) { badge ->
                BadgeItem(
                    emoji = badge.emoji,
                    label = badge.name,
                    isActive = badge.isUnlocked
                )
            }
        }
    }
}

@Composable
fun BadgeItem(emoji: String, label: String, isActive: Boolean) {
    // aktywna -> obramowanie w kolorze primary,  nie -> przezroczyste
    val borderColor = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent

    // wypełnienie (15% akcentu dla aktywnych, szare dla nieaktywnych)
    val bgColor = if (isActive) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    // nieaktywna -> lekko przezroczysta cala odznaka
    val alpha = if (isActive) 1f else 0.4f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .alpha(alpha)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(bgColor, RoundedCornerShape(16.dp))
                .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            //narazie emoji , potem ewentualnie jakies image/icon
            Text(text = emoji, fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}



@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        ProfileScreen()
    }
}