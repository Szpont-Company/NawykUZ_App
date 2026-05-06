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
import com.SzpontCompany.check.R
import com.SzpontCompany.check.data.social.Friend
import com.SzpontCompany.check.ui.community.components.FriendListItem
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import com.SzpontCompany.check.ui.theme.getColorByName
import androidx.lifecycle.viewmodel.compose.viewModel
import com.SzpontCompany.check.data.social.FriendRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsCard(
    selectedSubTab: Int,
    onSubTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onFriendProfileClick: (Friend) -> Unit = {},
    onMessageClick: (Friend) -> Unit = {},
    viewModel: FriendsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val subTabs = listOf(
        stringResource(
            R.string.friends_tab_friends_count,
            uiState.activeFriends.size + uiState.offlineFriends.size
        ),
        stringResource(R.string.friends_tab_invites_count, uiState.incomingRequests.size),
        stringResource(R.string.friends_tab_search)
    )

    val haptic = LocalHapticFeedback.current

    if (uiState.isLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.friends_loading),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
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
                            onSubTabSelected(index)
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
            0 -> FriendsListSection(
                activeFriends = uiState.activeFriends,
                offlineFriends = uiState.offlineFriends,
                onFriendProfileClick = onFriendProfileClick,
                onMessageClick = onMessageClick,
                onRemoveClick = { friend -> viewModel.removeFriend(friend.uid) }
            )

            1 -> FriendsInvitesSection(
                uiState.incomingRequests,
                onAccept = { req -> viewModel.respondToRequest(req.requestId, true, req.senderId) },
                onReject = { req ->
                    viewModel.respondToRequest(
                        req.requestId,
                        false,
                        req.senderId
                    )
                })

            2 -> FriendsSearchSection(
                suggestedFriends = uiState.suggestedFriends,
                searchResults = uiState.searchResults,
                searchQuery = searchQuery,
                isSearching = uiState.isSearching,
                onSearchQueryChange = viewModel::onSearchQueryChanged,
                onSendInviteClick = { friend -> viewModel.sendFriendRequest(friend.uid) },
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
    onMessageClick: (Friend) -> Unit = {},
    onRemoveClick: (Friend) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var friendToRemove by remember { mutableStateOf<Friend?>(null) }
    val haptic = LocalHapticFeedback.current

    val filteredActive = activeFriends.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val filteredOffline = offlineFriends.filter { it.name.contains(searchQuery, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text(stringResource(R.string.search_in_list), fontSize = 14.sp) },
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
            Text(
                stringResource(R.string.active_now),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filteredActive.size, key = { "active_${filteredActive[it].name}" }) { i ->
            Box(Modifier.animateItem()) {
                FriendListItem(
                    friend = filteredActive[i],
                    onProfileClick = { onFriendProfileClick(filteredActive[i]) },
                    onMessageClick = { onMessageClick(filteredActive[i]) },
                    onRemoveClick = { friendToRemove = filteredActive[i] }
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.recently_active),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(filteredOffline.size, key = { "offline_${filteredOffline[it].name}" }) { i ->
            Box(Modifier.animateItem()) {
                FriendListItem(
                    friend = filteredOffline[i],
                    onProfileClick = { onFriendProfileClick(filteredOffline[i]) },
                    onMessageClick = { onMessageClick(filteredOffline[i]) },
                    onRemoveClick = { friendToRemove = filteredOffline[i] }
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.invite_friends_outside),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.invite_friends_description),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                stringResource(R.string.friends_invite_link_mock),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) /* Kopiuj */ },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                stringResource(R.string.action_copy),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (friendToRemove != null) {
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title = {
                Text(
                    stringResource(R.string.friend_remove_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.friend_remove_message,
                        friendToRemove?.name ?: ""
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        friendToRemove?.let { onRemoveClick(it) }
                        friendToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        stringResource(R.string.friend_remove_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { friendToRemove = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
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
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("📭", fontSize = 40.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.no_invitations),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.no_pending_invitations),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.friends_pending_invites_header),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(requests.size, key = { requests[it].requestId }) { i ->
                val req = requests[i]
                val friendMock = Friend(
                    uid = req.senderId,
                    name = req.senderName,
                    initials = req.senderName.trim().split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                        .uppercase(),
                    avatarEmoji = req.senderAvatar,
                    bgColor = req.senderBgColor,
                    status = stringResource(R.string.friends_status_waiting)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(getColorByName(friendMock.bgColor), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (friendMock.avatarEmoji.isNotEmpty()) {
                                    Text(friendMock.avatarEmoji, fontSize = 24.sp)
                                } else {
                                    Text(
                                        friendMock.initials,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    friendMock.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(R.string.friend_wants_to_add),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { onAccept(req) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text(stringResource(R.string.accept), fontSize = 12.sp) }

                            OutlinedButton(
                                onClick = { onReject(req) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text(stringResource(R.string.decline), fontSize = 12.sp) }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.search_by_name_or_nickname),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.friends_clear_search)
                            )
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
                Text(
                    stringResource(R.string.suggested_you_may_know),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(suggestedFriends.size, key = { "suggested_${suggestedFriends[it].name}" }) { i ->
                Box(Modifier.animateItem()) {
                    FriendListItem(
                        friend = suggestedFriends[i],
                        isSuggested = true,
                        onAction = {
                            if (suggestedFriends[i].status.isEmpty()) onSendInviteClick(
                                suggestedFriends[i]
                            )
                        },
                        onProfileClick = { onFriendProfileClick(suggestedFriends[i]) },
                        onMessageClick = { onMessageClick(suggestedFriends[i]) }
                    )
                }
            }
        } else {
            if (isSearching) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
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
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.no_results),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.user_not_found, searchQuery),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item {
                    Text(
                        stringResource(R.string.friends_search_results_header),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(searchResults.size, key = { searchResults[it].uid }) { i ->
                    Box(Modifier.animateItem()) {
                        FriendListItem(
                            friend = searchResults[i],
                            isSuggested = true,
                            onAction = {
                                if (searchResults[i].status.isEmpty()) onSendInviteClick(
                                    searchResults[i]
                                )
                            },
                            onProfileClick = { onFriendProfileClick(searchResults[i]) },
                            onMessageClick = { onMessageClick(searchResults[i]) }
                        )
                    }
                }
            }
        }
    }
}
