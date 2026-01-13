package com.jammit.repository

import com.jammit.data.mapper.ChatMapper
import com.jammit.data.model.Chat
import com.jammit.network.ApiService
import com.jammit.network.CreateChatRequest

class ChatsRepository(private val apiService: ApiService) {
    suspend fun getChats(userId: String): Result<List<Chat>> {
        return try {
            val response = apiService.getChats(userId)
            if (response.isSuccessful) {
                val chats = response.body()?.map { ChatMapper.toChat(it, userId) } ?: emptyList()
                Result.success(chats)
            } else {
                Result.failure(Exception("Failed to fetch chats: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChat(userId: String, chatId: String): Result<Chat> {
        return try {
            val response = apiService.getChat(userId, chatId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(ChatMapper.toChat(response.body()!!, userId))
            } else {
                Result.failure(Exception("Chat not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findOrCreateChat(user1Id: String, user2Id: String): Result<Chat> {
        return try {
            val response = apiService.findOrCreateChat(CreateChatRequest(user1Id, user2Id))
            if (response.isSuccessful && response.body() != null) {
                Result.success(ChatMapper.toChat(response.body()!!, user1Id))
            } else {
                Result.failure(Exception("Failed to create chat: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
