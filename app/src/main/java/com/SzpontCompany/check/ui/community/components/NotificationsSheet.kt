package com.SzpontCompany.check.ui.community.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.social.NotificationItem
import com.SzpontCompany.check.data.social.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    onDismiss: () -> Unit
) {
    // Hardcoded dane na start
    val mockNotifications = listOf(
        NotificationItem(
            id = "1",
            title = "Nowe wyzwanie!",
            message = "Tomek K. zaprasza Cię do bitwy: Bieganie 30 min.",
            timeAgo = "5 min temu",
            type = NotificationType.CHALLENGE,
            isRead = false
        ),
        NotificationItem(
            id = "4",
            title = "Nowy znajomy",
            message = "Ania W. zaakceptowała Twoje zaproszenie do znajomych. Możecie teraz rywalizować!",
            timeAgo = "1 godz. temu",
            type = NotificationType.FRIEND,
            isRead = false
        ),
        NotificationItem(
            id = "2",
            title = "Zdobyto odznakę!",
            message = "Zdobyto odznakę \"Streak 21 dni\" 🔥. Wymóg: Utrzymaj passę 21 dni.",
            timeAgo = "2 godz. temu",
            type = NotificationType.REWARD,
            isRead = true
        ),
        NotificationItem(
            id = "3",
            title = "Globalny event",
            message = "Rozpoczął się nowy Globalny Marsz. Dołącz do reszty społeczności!",
            timeAgo = "1 dzień temu",
            type = NotificationType.SYSTEM,
            isRead = true
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Powiadomienia",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { /* TODO: Oznacz jako przeczytane */ }) {
                    Text("Oznacz przeczytane", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (mockNotifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Brak nowych powiadomień", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mockNotifications.size) { i ->
                        NotificationCard(item = mockNotifications[i])
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationItem) {
    val icon: ImageVector
    val iconTint: Color
    val iconBg: Color

    when (item.type) {
        NotificationType.CHALLENGE -> {
            icon = Icons.Default.FlashOn
            iconTint = MaterialTheme.colorScheme.primary
            iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        }
        NotificationType.FRIEND -> {
            icon = Icons.Default.PersonAdd
            iconTint = Color(0xFF42A5F5) // Jasny niebieski dla znajomych
            iconBg = Color(0xFF42A5F5).copy(alpha = 0.2f)
        }
        NotificationType.REWARD -> {
            icon = Icons.Default.EmojiEvents
            iconTint = Color(0xFFFFD700) // Złoty
            iconBg = Color(0xFFFFD700).copy(alpha = 0.2f)
        }
        NotificationType.SYSTEM -> {
            icon = Icons.Default.Notifications
            iconTint = MaterialTheme.colorScheme.tertiary
            iconBg = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (item.isRead) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { /* TODO: Akcja po kliknięciu powiadomienia */ }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.timeAgo,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!item.isRead) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
            }
        }
    }
}
