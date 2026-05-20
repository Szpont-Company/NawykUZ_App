package com.SzpontCompany.check.ui.rewards

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import com.SzpontCompany.check.data.badges.BadgeProvider
import com.SzpontCompany.check.R
import com.SzpontCompany.check.ads.RewardedAdManager
import com.SzpontCompany.check.ui.components.CheckBackButton
import androidx.compose.runtime.getValue

val PremiumGold = Color(0xFFC78C18)
val DarkGoldBackground = Color(0x33C78C18)

@Composable
fun RewardsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val viewModel : RewardsViewModel = viewModel(
        factory = RewardsViewmodelFactory(context)
    )

    LaunchedEffect(Unit) {
        RewardedAdManager.load(context.applicationContext)
    }

    val coins by viewModel.currentCoins.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            CheckBackButton(onClick = onBackClick)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.rewards_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        CoinsCard(currentCoins = coins, totalCoins = coins) // TODO: powinny byc 2 pola ale nie ma jeszcze wydawania wiec W/E

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(stringResource(R.string.rewards_section_badges))
            BadgesGrid()
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(stringResource(R.string.rewards_section_store))
            Hero(stringResource(R.string.shop_hero), stringResource(R.string.shop_hero_desc))
            StoreItem(
                emoji = "🎨",
                title = stringResource(R.string.store_item_accent_title),
                subtitle = stringResource(R.string.store_item_accent_desc),
                price = "200 C",
                type = StoreItemType.PURCHASE,
                onRedeem = { /*TODO: not implemented yet */}
            )
            StoreItem(
                emoji = "🛡️",
                title = stringResource(R.string.store_item_shield_title),
                subtitle = stringResource(R.string.store_item_shield_desc),
                price = "150 C",
                type = StoreItemType.PURCHASE,
                onRedeem = { /*TODO: not implemented yet */}
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle(stringResource(R.string.rewards_earn))
            Hero(stringResource(R.string.rewards_earn_desc1), stringResource(R.string.rewards_earn_desc2))
            StoreItem(
                emoji = "💰",
                title = stringResource(R.string.store_item_coins_title),
                subtitle = stringResource(R.string.store_item_coins_desc),
                price = "+10 C",
                type = StoreItemType.EARN,
                onRedeem = {
                    if(activity != null) {
                        RewardedAdManager.show(activity) {reward ->
                            viewModel.onAdWatched(reward)
                        }
                    }
                }
            )
            StoreItem(
                emoji = "⚔️",
                title = stringResource(R.string.rewards_battle),
                subtitle = stringResource(R.string.rewards_battle_desc),
                price = "+? C",
                type = StoreItemType.EARN,
                onRedeem = {
                    // TODO: tutaj przekieruje sie do sekcji battli
                    Toast.makeText(context, "Ta funkcja nie jest jeszcze dostępna", Toast.LENGTH_SHORT).show()
                }
            )
            StoreItem(
                emoji = "📅",
                title = stringResource(R.string.rewards_events),
                subtitle = stringResource(R.string.rewards_events_desc),
                price = "+? C",
                type = StoreItemType.EARN,
                onRedeem = {
                    // TODO: tutaj przekieruje do eventow
                    Toast.makeText(context, "Ta funkcja nie jest jeszcze dostępna", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun Hero(string1: String, string2: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGoldBackground)
            .border(1.5.dp, PremiumGold, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = string1,
                color = PremiumGold,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = string2,
                color = PremiumGold,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}
@Composable
fun CoinsCard(currentCoins: Int, totalCoins: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGoldBackground)
            .border(1.5.dp, PremiumGold, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val animatedCoins by animateIntAsState(
                targetValue = currentCoins,
                animationSpec = tween(durationMillis = 1000),
                label = "CoinsAnimation"
            )
            Column {
                Text(text = stringResource(R.string.rewards_your_coins), color = PremiumGold, fontSize = 14.sp)
                Text(text = "$animatedCoins C", color = PremiumGold, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = stringResource(R.string.rewards_total_earned), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text(text = totalCoins.toString(), color = PremiumGold, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun BadgesGrid() {
    val sortedBadges = BadgeProvider.allBadges.sortedByDescending { it.isUnlocked }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sortedBadges.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { badge ->
                    BadgeCard(
                        modifier = Modifier.weight(1f),
                        emoji = badge.emoji,
                        title = stringResource(badge.nameResId),
                        isUnlocked = badge.isUnlocked,
                        requirement = stringResource(badge.requirementResId)
                    )
                }

                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun BadgeCard(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    isUnlocked: Boolean,
    requirement: String = ""
) {
    val bgColor = if (isUnlocked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Transparent
    val alpha = if (isUnlocked) 1f else 0.4f

    Column(
        modifier = modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 32.sp)

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (isUnlocked) {
            Text(text = stringResource(R.string.rewards_unlocked), color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(
                text = requirement,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}

enum class StoreItemType {
    PURCHASE, EARN
}

@Composable
fun StoreItem(emoji: String, title: String, subtitle: String, price: String, type: StoreItemType, onRedeem: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 28.sp)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = {
                onRedeem()
            },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
        ) {
            Text(
                text = price,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RewardsScreenPreview() {
    CheckTheme(darkTheme = true, accent = Mint) {
        RewardsScreen{}
    }
}