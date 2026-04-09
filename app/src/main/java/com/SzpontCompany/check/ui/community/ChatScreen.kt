package com.SzpontCompany.check.ui.community

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.ui.components.CheckBackButton
import com.SzpontCompany.check.ui.theme.CheckTheme
import com.SzpontCompany.check.ui.theme.Mint
import com.SzpontCompany.check.ui.theme.Rose
import com.SzpontCompany.check.ui.theme.getColorByName
import java.text.SimpleDateFormat
import java.util.*

enum class MessageType {
    SENT, RECEIVED, SYSTEM_HABIT, DATE_SEPARATOR
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType,
    val isRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBackClick: () -> Unit = {},
    friendName: String = "Anna Nowak",
    friendEmoji: String = "🦊",
    friendBgColor: String = "Lavender"
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Fake messages
    val messages = remember {
        mutableStateListOf(
            ChatMessage(text = "Wczoraj", type = MessageType.DATE_SEPARATOR),
            ChatMessage(text = "Hej, świetnie Ci idzie z tym bieganiem! \uD83D\uDCAA", type = MessageType.RECEIVED, timestamp = System.currentTimeMillis() - 86400000),
            ChatMessage(text = "Dzięki! Staram się trzymać rytm 🏃\u200D♂️", type = MessageType.SENT, isRead = true, timestamp = System.currentTimeMillis() - 82400000),
            ChatMessage(text = "Dzisiaj", type = MessageType.DATE_SEPARATOR),
            ChatMessage(text = "$friendName właśnie odhaczyła nawyk: \uD83D\uDCA7 Piję wodę! Dzień 24 z rzędu \uD83D\uDD25", type = MessageType.SYSTEM_HABIT),
            ChatMessage(text = "Wow, 24 dni to niezły wynik!", type = MessageType.SENT, isRead = true, timestamp = System.currentTimeMillis() - 3600000)
        )
    }

    var showQuickReactions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ChatTopBar(
                onBackClick = onBackClick,
                friendName = friendName,
                friendEmoji = friendEmoji,
                friendBgColor = friendBgColor
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                AnimatedVisibility(visible = showQuickReactions) {
                    QuickReactionsRow(onReactionSelected = { reaction ->
                        messages.add(ChatMessage(text = reaction, type = MessageType.SENT))
                        showQuickReactions = false
                    })
                }
                ChatInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSendClick = {
                        if (messageText.isNotBlank()) {
                            messages.add(ChatMessage(text = messageText, type = MessageType.SENT))
                            messageText = ""
                        }
                    },
                    onQuickReactionClick = { showQuickReactions = !showQuickReactions }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
}

@Composable
fun ChatTopBar(
    onBackClick: () -> Unit,
    friendName: String,
    friendEmoji: String,
    friendBgColor: String
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CheckBackButton(onClick = onBackClick)
            Spacer(modifier = Modifier.width(16.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(getColorByName(friendBgColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = friendEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and Status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friendName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Aktywna teraz",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = if (message.timestamp > 0) timeFormat.format(Date(message.timestamp)) else ""

    when (message.type) {
        MessageType.DATE_SEPARATOR -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        MessageType.SYSTEM_HABIT -> {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFBA7517).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        MessageType.RECEIVED -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(text = message.text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = timeString,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                        )
                    }
                }
            }
        }
        MessageType.SENT -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 4.dp, bottomStart = 16.dp),
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(text = message.text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onPrimary)
                        Row(modifier = Modifier.align(Alignment.End).padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = timeString, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.width(4.dp))
                            if (message.isRead) {
                                Text(text = "✓✓", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            } else {
                                Text(text = "✓", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickReactionsRow(onReactionSelected: (String) -> Unit) {
    val reactions = listOf("👋", "💪", "🏆", "🔥")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                reactions.forEach { reaction ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onReactionSelected(reaction) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = reaction, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onQuickReactionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Quick reaction button
        IconButton(
            onClick = onQuickReactionClick,
            modifier = Modifier
                .padding(end = 8.dp, bottom = 4.dp)
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Szybkie reakcje",
                tint = Color(0xFFBA7517)
            )
        }

        // Text input
        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp),
            placeholder = { Text("Napisz wiadomość...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            maxLines = 4
        )

        // Send button
        AnimatedVisibility(
            visible = messageText.isNotBlank(),
            enter = fadeIn() + scaleIn() + expandHorizontally(),
            exit = fadeOut() + scaleOut() + shrinkHorizontally()
        ) {
            IconButton(
                onClick = onSendClick,
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 4.dp)
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Wyślij",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    CheckTheme(darkTheme = true, accent = Rose) {
        ChatScreen()
    }
}

