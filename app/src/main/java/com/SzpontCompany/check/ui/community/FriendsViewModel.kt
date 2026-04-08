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
    val suggestedFriends: List<Friend> = emptyList(),
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

    private val _outgoingRequests = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            repository.updatePresence(true)
        }

        viewModelScope.launch {
            val suggested = repository.getSuggestedFriends()
            _uiState.update { it.copy(suggestedFriends = mapStatuses(suggested)) }
        }

        viewModelScope.launch {
            repository.getIncomingRequests().collect { requests ->
                _uiState.update { it.copy(incomingRequests = requests) }

                val currentResults = _uiState.value.searchResults
                if (currentResults.isNotEmpty()) {
                    _uiState.update { it.copy(searchResults = mapStatuses(currentResults)) }
                }

                val currentSuggested = _uiState.value.suggestedFriends
                if (currentSuggested.isNotEmpty()) {
                    _uiState.update { it.copy(suggestedFriends = mapStatuses(currentSuggested)) }
                }
            }
        }

        viewModelScope.launch {
            repository.getOutgoingRequests().collect { outIds ->
                _outgoingRequests.value = outIds

                val currentResults = _uiState.value.searchResults
                if (currentResults.isNotEmpty()) {
                    _uiState.update { it.copy(searchResults = mapStatuses(currentResults)) }
                }

                val currentSuggested = _uiState.value.suggestedFriends
                if (currentSuggested.isNotEmpty()) {
                    _uiState.update { it.copy(suggestedFriends = mapStatuses(currentSuggested)) }
                }
            }
        }

        viewModelScope.launch {
            repository.getMyFriends().collect { friends ->
                val online = friends.filter { it.online }
                val offline = friends.filter { !it.online }
                _uiState.update { it.copy(activeFriends = online, offlineFriends = offline) }

                val currentResults = _uiState.value.searchResults
                if (currentResults.isNotEmpty()) {
                    _uiState.update { it.copy(searchResults = mapStatuses(currentResults)) }
                }

                val currentSuggested = _uiState.value.suggestedFriends
                if (currentSuggested.isNotEmpty()) {
                    _uiState.update { it.copy(suggestedFriends = mapStatuses(currentSuggested)) }
                }
            }
        }

        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .filter { it.length >= 3 }
                .collectLatest { query ->
                    _uiState.update { it.copy(isSearching = true) }
                    val results = repository.searchUsers(query)
                    val mappedResults = mapStatuses(results)
                    _uiState.update { it.copy(searchResults = mappedResults, isSearching = false) }
                }
        }
    }

    private fun mapStatuses(users: List<Friend>): List<Friend> {
        val friendsIds = _uiState.value.activeFriends.map { it.uid } + _uiState.value.offlineFriends.map { it.uid }
        val incomingIds = _uiState.value.incomingRequests.map { it.senderId }
        val outgoingIds = _outgoingRequests.value

        return users.map { friend ->
            val newStatus = when {
                friend.uid in friendsIds -> "Znajomy"
                friend.uid in outgoingIds -> "Wysłano ✓"
                friend.uid in incomingIds -> "Czeka na odpowiedź"
                else -> ""
            }
            friend.copy(status = newStatus)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isEmpty() || query.length < 3) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
        } else {
            _uiState.update { it.copy(isSearching = true) }
        }
    }

    fun sendFriendRequest(receiverId: String) {
        val updatedSearchResults = _uiState.value.searchResults.map {
            if (it.uid == receiverId) it.copy(status = "Wysłano ✓") else it
        }
        val updatedSuggestedResults = _uiState.value.suggestedFriends.map {
            if (it.uid == receiverId) it.copy(status = "Wysłano ✓") else it
        }
        _uiState.update { it.copy(
            searchResults = updatedSearchResults,
            suggestedFriends = updatedSuggestedResults
        ) }

        viewModelScope.launch {
            repository.sendFriendRequest(receiverId)
        }
    }

    fun respondToRequest(requestId: String, accept: Boolean, senderId: String) {
        viewModelScope.launch {
            repository.respondToRequest(requestId, accept, senderId)
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            repository.removeFriend(friendId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable).launch {
            repository.updatePresence(false)
        }
    }
}
