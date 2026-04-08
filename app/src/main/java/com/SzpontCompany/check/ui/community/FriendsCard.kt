package com.SzpontCompany.check.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.SzpontCompany.check.data.social.Friend
import com.SzpontCompany.check.ui.community.components.FriendListItem
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.data.social.FriendRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsCard(
    modifier: Modifier = Modifier,
    onFriendProfileClick: (Friend) -> Unit = {},
    onMessageClick: (Friend) -> Unit = {},
    viewModel: FriendsViewModel = viewModel()
) {
    var selectedSubTab by remember { mutableStateOf(0) }
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val subTabs = listOf("Znajomi (${uiState.activeFriends.size + uiState.offlineFriends.size})", "Zaproszenia (${uiState.incomingRequests.size})", "Szukaj")
    val haptic = LocalHapticFeedback.current


    Column(modifier = modifier.fillMaxSize()) {
        // Pod-menu (Sub-tabs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            subTabs.forEachIndexed { index, title ->
                val isSelected = selectedSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedSubTab = index
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        when (selectedSubTab) {
            0 -> FriendsListSection(uiState.activeFriends, uiState.offlineFriends, onFriendProfileClick, onMessageClick)
            1 -> FriendsInvitesSection(uiState.incomingRequests, onAccept = { req -> viewModel.respondToRequest(req.requestId, true, req.senderId) }, onReject = { req -> viewModel.respondToRequest(req.requestId, false, req.senderId) })
            2 -> FriendsSearchSection(
                suggestedFriends = emptyList(), // Można tu dodać logikę sugierowanych znajomych
                searchResults = uiState.searchResults,
                searchQuery = searchQuery,
                isSearching = uiState.isSearching,
                onSearchQueryChange = viewModel::onSearchQueryChanged,
                onSendInviteClick = { friend -> viewModel.sendFriendRequest(friend.uid, "Ja (Test)", "😎", "Mint") },
                onFriendProfileClick = onFriendProfileClick,
                onMessageClick = onMessageClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListSection(
    activeFriends: List<Friend>,
    offlineFriends: List<Friend>,
    onFriendProfileClick: (Friend) -> Unit = {},
    onMessageClick: (Friend) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text("Szukaj na liście...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Text("AKTYWNI TERAZ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(activeFriends.size, key = { "active_${activeFriends[it].name}" }) { i ->
            Box(Modifier.animateItem()) {
                FriendListItem(
                    friend = activeFriends[i],
                    onProfileClick = { onFriendProfileClick(activeFriends[i]) },
                    onMessageClick = { onMessageClick(activeFriends[i]) }
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("OSTATNIO AKTYWNI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(offlineFriends.size, key = { "offline_${offlineFriends[it].name}" }) { i ->
            Box(Modifier.animateItem()) {
                FriendListItem(
                    friend = offlineFriends[i],
                    onProfileClick = { onFriendProfileClick(offlineFriends[i]) },
                    onMessageClick = { onMessageClick(offlineFriends[i]) }
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            // Zgarnij nagrodę card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Zaproś znajomych spoza aplikacji", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(4.dp))
                    Text("Wyślij link i zyskaj 50 monet za każdego dołączonego znajomego", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text("check.app/invite/marekk", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) /* Kopiuj */ },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Kopiuj", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendsInvitesSection(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit
) {
    if (requests.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("📭", fontSize = 40.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("Brak zaproszeń", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Text("Nie masz żadnych oczekujących zaproszeń", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text("OCZEKUJĄCE ZAPROSZENIA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(requests.size, key = { requests[it].requestId }) { i ->
                val req = requests[i]
                val friendMock = Friend(
                    uid = req.senderId,
                    name = req.senderName,
                    initials = req.senderName.take(2).uppercase(),
                    avatarEmoji = req.senderAvatar,
                    bgColor = req.senderBgColor,
                    status = "Czeka na odpowiedź"
                )

                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(friendMock.initials, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(friendMock.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Chce zostać Twoim znajomym", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onAccept(req) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Akceptuj", fontSize = 12.sp) }

                            OutlinedButton(
                                onClick = { onReject(req) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Odrzuć", fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsSearchSection(
    suggestedFriends: List<Friend>,
    searchResults: List<Friend>,
    searchQuery: String,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSendInviteClick: (Friend) -> Unit,
    onFriendProfileClick: (Friend) -> Unit = {},
    onMessageClick: (Friend) -> Unit = {}
) {
    val searched = searchQuery.length > 2

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                placeholder = { Text("Szukaj po nazwie lub @nicku...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Wyczyść")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        if (!searched) {
            item {
                Text("SUGEROWANE — MOŻESz ZNAĆ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(suggestedFriends.size, key = { "suggested_${suggestedFriends[it].name}" }) { i ->
                Box(Modifier.animateItem()) {
                    FriendListItem(
                        friend = suggestedFriends[i],
                        isSuggested = true,
                        onProfileClick = { onFriendProfileClick(suggestedFriends[i]) },
                        onMessageClick = { onMessageClick(suggestedFriends[i]) }
                    )
                }
            }
        } else {
            if (isSearching) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (searchResults.isEmpty()) {
                item {
                    Spacer(Modifier.height(32.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Brak wyników", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(8.dp))
                        Text("Nie znaleziono użytkownika \"$searchQuery\"", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                item {
                    Text("WYNIKI WYSZUKIWANIA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(searchResults.size, key = { searchResults[it].uid }) { i ->
                    Box(Modifier.animateItem()) {
                        FriendListItem(
                            friend = searchResults[i],
                            isSuggested = true,
                            onAction = { onSendInviteClick(searchResults[i]) },
                            onProfileClick = { onFriendProfileClick(searchResults[i]) },
                            onMessageClick = { onMessageClick(searchResults[i]) }
                        )
                    }
                }
            }
        }
    }
}
