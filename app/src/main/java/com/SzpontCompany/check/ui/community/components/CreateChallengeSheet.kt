package com.SzpontCompany.check.ui.community.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.social.Friend
import com.SzpontCompany.check.ui.theme.*

// 1. Prosty model szablonu wyzwania
data class ChallengeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val color: Color
)

// Gotowe szablony do wyboru
val PredefinedChallenges = listOf(
    ChallengeTemplate("1", "Królowie Kroków", "Kto zrobi więcej kroków w 3 dni?", "👟", Sky),
    ChallengeTemplate("2", "Wodny Pojedynek", "Pij minimum 2L wody przez 5 dni.", "💧", Mint),
    ChallengeTemplate("3", "Cukrowy Odwyk", "Zero słodyczy przez cały tydzień!", "🚫", Coral),
    ChallengeTemplate("4", "Ranny Ptaszek", "Wstawanie przed 7:00 przez 3 dni.", "🌅", Amber)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeSheet(
    friendsList: List<Friend>, // Lista Twoich znajomych
    onDismiss: () -> Unit,
    onSendChallenge: (Friend, ChallengeTemplate, Int) -> Unit // Akcja "Wyślij"
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Stany (co użytkownik aktualnie wybrał)
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    var selectedTemplate by remember { mutableStateOf<ChallengeTemplate?>(null) }
    var betAmount by remember { mutableStateOf(50) } // Domyślnie 50 monet

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()) // Pozwala przewijać zawartość, jeśli ekran jest mały
        ) {
            Text(
                text = "Rzuć wyzwanie",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- SEKCJA 1: Wybór Znajomego ---
            Text(
                text = "Kto podejmie rękawicę?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(friendsList) { friend ->
                    SelectableFriendItem(
                        friend = friend,
                        isSelected = selectedFriend == friend,
                        onClick = { selectedFriend = friend }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- SEKCJA 2: Wybór Szablonu ---
            Text(
                text = "O co walczycie?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PredefinedChallenges.forEach { template ->
                    ChallengeTemplateCard(
                        template = template,
                        isSelected = selectedTemplate == template,
                        onClick = { selectedTemplate = template }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- SEKCJA 3: Stawka (Monety) ---
            Text(
                text = "Stawka (Check Coins)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(10, 50, 100, 500).forEach { amount ->
                    BetButton(
                        amount = amount,
                        isSelected = betAmount == amount,
                        onClick = { betAmount = amount },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // --- PRZYCISK WYŚLIJ ---
            val canSend = selectedFriend != null && selectedTemplate != null
            Button(
                onClick = {
                    if (canSend) {
                        onSendChallenge(selectedFriend!!, selectedTemplate!!, betAmount)
                    }
                },
                enabled = canSend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Coral, // Koralowy kolor akcji
                    disabledContainerColor = Coral.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Wyślij wyzwanie",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SelectableFriendItem(friend: Friend, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) Coral else Color.Transparent
    val alpha = if (isSelected) 1f else 0.6f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.2f)) // Używamy Mint jako domyślnego tła awatara
                .border(2.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = friend.avatarEmoji, fontSize = 28.sp, modifier = Modifier.alpha(alpha))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = friend.name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ChallengeTemplateCard(template: ChallengeTemplate, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) template.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) template.color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(template.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = template.emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = template.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = template.color,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun BetButton(amount: Int, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (isSelected) Amber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) Amber else MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🪙", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = amount.toString(),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Amber else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}