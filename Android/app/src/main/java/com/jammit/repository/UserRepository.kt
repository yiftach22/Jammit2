package com.jammit.repository

import com.jammit.data.mapper.UserMapper
import com.jammit.data.model.User
import com.jammit.network.ApiService
import com.jammit.network.CreateUserRequest
import com.jammit.network.UpdateUserRequest

class UserRepository(private val apiService: ApiService) {
    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful) {
                val users = response.body()?.map { UserMapper.toUser(it) } ?: emptyList()
                Result.success(users)
            } else {
                Result.failure(Exception("Failed to fetch users: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(id: String): Result<User> {
        return try {
            val response = apiService.getUser(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(UserMapper.toUser(response.body()!!))
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(user: User): Result<User> {
        return try {
            val request = UserMapper.toCreateUserRequest(user)
            val response = apiService.createUser(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(UserMapper.toUser(response.body()!!))
            } else {
                Result.failure(Exception("Failed to create user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(userId: String, fcmToken: String): Result<Unit> {
        return try {
            val response = apiService.updateUser(userId, UpdateUserRequest(fcmToken = fcmToken))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Failed to register for notifications: ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(id: String, user: User): Result<User> {
        return try {
            val request = UserMapper.toUpdateUserRequest(user)
            val response = apiService.updateUser(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(UserMapper.toUser(response.body()!!))
            } else {
                val errorMessage = extractErrorMessage(response.errorBody())
                Result.failure(Exception(errorMessage ?: "Failed to update user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun extractErrorMessage(errorBody: okhttp3.ResponseBody?): String? {
        if (errorBody == null) return null
        return try {
            val errorString = errorBody.string()
            if (errorString.isBlank()) return null
            
            val json = com.google.gson.Gson().fromJson(errorString, com.google.gson.JsonObject::class.java)
            when {
                json.has("message") -> json.get("message").asString
                json.has("error") -> json.get("error").asString
                else -> errorString
            }
        } catch (e: Exception) {
            null
        }
    }
}
