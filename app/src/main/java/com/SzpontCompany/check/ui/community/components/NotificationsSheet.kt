package com.SzpontCompany.check.ui.community.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.SzpontCompany.check.data.social.NotificationItem
import com.SzpontCompany.check.data.social.NotificationType
import com.SzpontCompany.check.ui.community.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    viewModel: CommunityViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val context = LocalContext.current

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
                TextButton(onClick = { viewModel.markAllAsRead() }) {
                    Text("Oznacz przeczytane", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Brak nowych powiadomień", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.declineAction(item.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color by animateColorAsState(
                                    targetValue = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
                                        else -> Color(0xFFE24B4A)
                                    },
                                    label = "bg_color"
                                )
                                val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd

                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Usuń powiadomienie",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            content = {
                                NotificationCard(
                                    item = item,
                                    onAccept = {
                                        viewModel.acceptAction(item.id)
                                        if (item.type == NotificationType.FRIEND) {
                                            Toast.makeText(context, "Dodano do znajomych!", Toast.LENGTH_SHORT).show()
                                        } else if (item.type == NotificationType.CHALLENGE) {
                                            Toast.makeText(context, "Zaakceptowano wyzwanie!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onDecline = { viewModel.declineAction(item.id) },
                                    onClick = { viewModel.markAsRead(item.id) }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    item: NotificationItem,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onClick: () -> Unit = {}
) {
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
            iconTint = Color(0xFF42A5F5)
            iconBg = Color(0xFF42A5F5).copy(alpha = 0.2f)
        }
        NotificationType.REWARD -> {
            icon = Icons.Default.EmojiEvents
            iconTint = Color(0xFFFFD700)
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
            .clickable { onClick() }
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

            if (item.requiresAction) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Akceptuj", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onDecline,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(38.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Odrzuć", fontSize = 13.sp)
                    }
                }
            }
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
