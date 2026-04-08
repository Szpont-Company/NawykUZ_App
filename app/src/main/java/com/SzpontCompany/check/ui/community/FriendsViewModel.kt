package com.SzpontCompany.check.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.social.Friend
import com.SzpontCompany.check.data.social.FriendRepository
import com.SzpontCompany.check.data.social.FriendRequest
import com.SzpontCompany.check.data.user.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview

data class FriendsUiState(
    val activeFriends: List<Friend> = emptyList(),
    val offlineFriends: List<Friend> = emptyList(),
    val searchResults: List<Friend> = emptyList(),
    val incomingRequests: List<FriendRequest> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class)
class FriendsViewModel(
    private val repository: FriendRepository = FriendRepository(),
    private val userRepository: UserRepository? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        // Stream na moje przychodzące zaproszenia
        viewModelScope.launch {
            repository.getIncomingRequests().collect { requests ->
                _uiState.update { it.copy(incomingRequests = requests) }
            }
        }

        // Stream na moich znajomych
        viewModelScope.launch {
            repository.getMyFriends().collect { friends ->
                val online = friends.filter { it.online }
                val offline = friends.filter { !it.online }
                _uiState.update { it.copy(activeFriends = online, offlineFriends = offline) }
            }
        }

        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.length >= 3 }
                .collectLatest { query ->
                    _uiState.update { it.copy(isSearching = true) }
                    val results = repository.searchUsers(query)
                    _uiState.update { it.copy(searchResults = results, isSearching = false) }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isEmpty() || query.length < 3) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        }
    }

    fun sendFriendRequest(receiverId: String, currentName: String, currentAvatar: String, currentBg: String) {
        viewModelScope.launch {
            repository.sendFriendRequest(receiverId, currentName, currentAvatar, currentBg)
            val updatedSearchResults = _uiState.value.searchResults.map {
                if (it.uid == receiverId) it.copy(status = "Wysłano ✓") else it
            }
            _uiState.update { it.copy(searchResults = updatedSearchResults) }
        }
    }

    fun respondToRequest(requestId: String, accept: Boolean, senderId: String) {
        viewModelScope.launch {
            repository.respondToRequest(requestId, accept, senderId)
        }
    }
}

