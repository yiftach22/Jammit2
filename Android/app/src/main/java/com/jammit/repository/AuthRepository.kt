package com.jammit.repository

import com.jammit.network.ApiService
import com.jammit.network.LoginRequest
import com.jammit.network.RegisterRequest
import com.jammit.network.GoogleLoginRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import java.io.IOException

class AuthRepository(private val apiService: ApiService) {
    private val gson = Gson()

    suspend fun login(email: String, password: String): Result<Pair<String, String>> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val token = response.body()?.token ?: ""
                val userId = response.body()?.userId ?: ""
                Result.success(Pair(token, userId))
            } else {
                val errorMessage = extractErrorMessage(response.errorBody())
                Result.failure(Exception(errorMessage ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.register(RegisterRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = extractErrorMessage(response.errorBody())
                Result.failure(Exception(errorMessage ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkUsername(username: String): Result<Boolean> {
        return try {
            val response = apiService.checkUsername(username)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.available)
            } else {
                val errorMessage = extractErrorMessage(response.errorBody())
                Result.failure(Exception(errorMessage ?: "Failed to check username"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeRegistration(
        email: String,
        password: String,
        username: String,
        instruments: List<com.jammit.network.InstrumentWithLevelRequest>
    ): Result<Pair<String, String>> {
        return try {
            val response = apiService.completeRegistration(
                com.jammit.network.CompleteRegistrationRequest(email, password, username, instruments)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val token = response.body()?.token ?: throw Exception("Token not received")
                val userId = response.body()?.userId ?: throw Exception("User ID not received")
                Result.success(Pair(token, userId))
            } else {
                val errorMessage = extractErrorMessage(response.errorBody())
                Result.failure(Exception(errorMessage ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithGoogle(token: String): Result<Pair<String, String>> {
        return try {
            val response = apiService.loginWithGoogle(GoogleLoginRequest(token))
            if (response.isSuccessful && response.body()?.success == true) {
                val authToken = response.body()?.token ?: throw Exception("Token not received")
                val userId = response.body()?.userId ?: throw Exception("User ID not received")
                Result.success(Pair(authToken, userId))
            } else {
                val errorMessage = extractErrorMessage(response.errorBody())
                Result.failure(Exception(errorMessage ?: "Google login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractErrorMessage(errorBody: ResponseBody?): String? {
        if (errorBody == null) return null
        return try {
            val errorString = errorBody.string()
            if (errorString.isBlank()) return null
            
            val json = gson.fromJson(errorString, JsonObject::class.java)
            when {
                json.has("message") -> json.get("message").asString
                json.has("error") -> json.get("error").asString
                else -> errorString
            }
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}
