package com.jammit.repository

import com.jammit.data.mapper.ChatMapper
import com.jammit.data.model.Chat
import com.jammit.data.model.Message
import com.jammit.network.ApiService
import com.jammit.network.CreateChatRequest
import com.jammit.network.MessageResponse

class ChatsRepository(private val apiService: ApiService) {

    private fun MessageResponse.toMessage(): Message {
        val ts = try {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.parse(createdAt)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
        return Message(id = id, chatId = chatId, senderId = senderId, content = content, timestamp = ts)
    }
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

    suspend fun getMessages(userId: String, chatId: String): Result<List<Message>> {
        return try {
            val response = apiService.getMessages(userId, chatId)
            if (response.isSuccessful) {
                val list = response.body()?.map { it.toMessage() } ?: emptyList()
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to load messages: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
