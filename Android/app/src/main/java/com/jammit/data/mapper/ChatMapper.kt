package com.jammit.data.mapper

import com.jammit.data.model.Chat
import com.jammit.network.ChatResponse

object ChatMapper {
    fun toChat(chatResponse: ChatResponse, currentUserId: String): Chat {
        return Chat(
            id = chatResponse.id,
            otherUserId = chatResponse.otherUser.id,
            otherUsername = chatResponse.otherUser.username,
            otherUserProfilePictureUrl = chatResponse.otherUser.profilePictureUrl,
            lastMessage = chatResponse.lastMessage,
            lastMessageTimestamp = chatResponse.lastMessageTimestamp ?: 0L
        )
    }
}
