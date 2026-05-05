package com.SzpontCompany.check.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.chat.ChatMessage
import com.SzpontCompany.check.data.chat.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<UiChatMessage>>(emptyList())
    val messages: StateFlow<List<UiChatMessage>> = _messages.asStateFlow()

    private val currentUserId = repository.currentUserId ?: ""
    private var currentFriendId: String? = null

    fun startChat(friendId: String) {
        if (currentFriendId == friendId) return
        currentFriendId = friendId

        viewModelScope.launch {
            repository.getMessages(friendId).collect { rawMessages ->
                _messages.value = processMessages(rawMessages)
            }
        }
    }

    fun sendMessage(text: String, type: String = "TEXT") {
        val friendId = currentFriendId ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(friendId, text, type)
        }
    }

    private fun processMessages(rawMessages: List<ChatMessage>): List<UiChatMessage> {
        if (rawMessages.isEmpty()) return emptyList()

        val result = mutableListOf<UiChatMessage>()
        var lastDateString = ""

        // Process dates and message types
        for (msg in rawMessages) {
            val dateString = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(msg.timestamp))

            if (dateString != lastDateString) {
                result.add(UiChatMessage(
                    id = "date_$dateString",
                    text = if (isToday(msg.timestamp)) "TODAY" else if (isYesterday(msg.timestamp)) "YESTERDAY" else dateString,
                    timestamp = msg.timestamp,
                    type = MessageType.DATE_SEPARATOR
                ))
                lastDateString = dateString
            }

            val msgType = if (msg.type == "SYSTEM_HABIT") {
                MessageType.SYSTEM_HABIT
            } else if (msg.senderId == currentUserId) {
                MessageType.SENT
            } else {
                MessageType.RECEIVED
            }

            result.add(
                UiChatMessage(
                    id = msg.id,
                    text = msg.text,
                    timestamp = msg.timestamp,
                    type = msgType,
                    isRead = msg.isRead
                )
            )
        }

        return result
    }

    private fun isToday(timestamp: Long): Boolean {
        val now = java.util.Calendar.getInstance()
        val time = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(java.util.Calendar.YEAR) == time.get(java.util.Calendar.YEAR) &&
               now.get(java.util.Calendar.DAY_OF_YEAR) == time.get(java.util.Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val now = java.util.Calendar.getInstance()
        now.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val time = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(java.util.Calendar.YEAR) == time.get(java.util.Calendar.YEAR) &&
               now.get(java.util.Calendar.DAY_OF_YEAR) == time.get(java.util.Calendar.DAY_OF_YEAR)
    }
}

