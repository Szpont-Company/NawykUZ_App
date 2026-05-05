package com.SzpontCompany.check.ui.community.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.SzpontCompany.check.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.social.Friend
import com.SzpontCompany.check.ui.theme.getColorByName
import java.util.Locale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource

@Composable
fun FriendListItem(
    friend: Friend,
    isSuggested: Boolean = false,
    onAction: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {}
) {
    val formattedXp = if (friend.xp > 0) String.format(Locale.US, "%,d", friend.xp).replace(',', ' ') else ""
    val haptic = LocalHapticFeedback.current

    val avatarBgColor = getColorByName(friend.bgColor)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (friend.online) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(avatarBgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (friend.avatarEmoji.isNotEmpty()) {
                    Text(friend.avatarEmoji, fontSize = 24.sp)
                } else {
                    Text(friend.initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(friend.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (friend.online) {
                        Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.friend_online_xp, formattedXp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (friend.lastActive.isNotEmpty()) {
                        Box(modifier = Modifier.size(8.dp).background(Color.Gray.copy(alpha = 0.5f), CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text(friend.lastActive, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (isSuggested) {
                        Text(
                            if (friend.mutuals > 0)
                                stringResource(R.string.friend_mutuals_count, friend.mutuals)
                            else
                                stringResource(R.string.friend_from_contacts),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!isSuggested) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { /* Battle */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚔️")
                }

                Spacer(Modifier.width(12.dp))

                var expanded by remember { mutableStateOf(false) }
                Box {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .clickable { expanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.friend_options_desc), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.friend_show_profile), fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                expanded = false
                                onProfileClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.friend_send_message), fontWeight = FontWeight.Medium, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                expanded = false
                                onMessageClick()
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.friend_remove), fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFFFF5252)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                expanded = false 
                                onRemoveClick()
                            }
                        )
                    }
                }
            } else {
                if (friend.status.isNotEmpty()) {
                    val displayStatus = when (friend.status) {
                        "STATUS_FRIEND" -> stringResource(R.string.friend_status_friend)
                        "STATUS_SENT" -> stringResource(R.string.friend_status_sent)
                        "STATUS_WAITING" -> stringResource(R.string.friend_status_waiting)
                        else -> friend.status
                    }
                    OutlinedButton(
                        onClick = {},
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(displayStatus, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAction() 
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) { Text(stringResource(R.string.friend_invite_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
