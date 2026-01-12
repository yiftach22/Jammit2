package com.jammit.ui.chatdetail

import com.jammit.data.model.Message
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatDetailViewModel(private val chatId: String) : ViewModel() {
    private val currentUserId = "current_user"

    // Mock messages for the chat
    private val _messages = MutableStateFlow<List<Message>>(
        listOf(
            Message(
                id = "msg1",
                chatId = chatId,
                senderId = "user1",
                content = "Hey! Would you like to jam sometime?",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            Message(
                id = "msg2",
                chatId = chatId,
                senderId = currentUserId,
                content = "Sure, sounds great! When are you free?",
                timestamp = System.currentTimeMillis() - 3300000
            ),
            Message(
                id = "msg3",
                chatId = chatId,
                senderId = "user1",
                content = "How about this weekend?",
                timestamp = System.currentTimeMillis() - 3000000
            )
        )
    )
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val newMessage = Message(
                id = "msg_${System.currentTimeMillis()}",
                chatId = chatId,
                senderId = currentUserId,
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + newMessage
        }
    }

    fun isCurrentUser(senderId: String): Boolean = senderId == currentUserId
}

