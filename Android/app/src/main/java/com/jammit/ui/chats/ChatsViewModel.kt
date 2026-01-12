package com.jammit.ui.chats

import com.jammit.data.model.Chat
import com.jammit.data.model.User
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatsViewModel : ViewModel() {
    // Mock chats data
    private val _chats = MutableStateFlow<List<Chat>>(
        listOf(
            Chat(
                id = "chat1",
                otherUserId = "user1",
                otherUsername = "Alice Johnson",
                lastMessage = "Hey! Would you like to jam sometime?",
                lastMessageTimestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
            ),
            Chat(
                id = "chat2",
                otherUserId = "user2",
                otherUsername = "Bob Smith",
                lastMessage = "Sure, sounds great!",
                lastMessageTimestamp = System.currentTimeMillis() - 7200000 // 2 hours ago
            ),
            Chat(
                id = "chat3",
                otherUserId = "user3",
                otherUsername = "Charlie Brown",
                lastMessage = "Thanks for the session yesterday!",
                lastMessageTimestamp = System.currentTimeMillis() - 86400000 // 1 day ago
            ),
            Chat(
                id = "chat4",
                otherUserId = "user4",
                otherUsername = "Diana Prince",
                lastMessage = "See you next week!",
                lastMessageTimestamp = System.currentTimeMillis() - 172800000 // 2 days ago
            )
        )
    )
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()
}

