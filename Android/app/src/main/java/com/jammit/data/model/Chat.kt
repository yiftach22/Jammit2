package com.jammit.data.model

data class Chat(
    val id: String,
    val otherUserId: String,
    val otherUsername: String,
    val otherUserProfilePictureUrl: String? = null,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = 0L
)

