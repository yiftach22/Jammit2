package com.jammit.network

import com.jammit.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/check-username")
    suspend fun checkUsername(@Query("username") username: String): Response<UsernameAvailabilityResponse>

    @POST("auth/complete-registration")
    suspend fun completeRegistration(@Body request: CompleteRegistrationRequest): Response<AuthResponse>

    @POST("auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<AuthResponse>

    // Users
    @GET("users")
    suspend fun getUsers(): Response<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserResponse>

    @POST("users")
    suspend fun createUser(@Body user: CreateUserRequest): Response<UserResponse>

    @PATCH("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: UpdateUserRequest): Response<UserResponse>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>

    // Instruments
    @GET("instruments")
    suspend fun getInstruments(): Response<List<InstrumentResponse>>

    @GET("instruments/{id}")
    suspend fun getInstrument(@Path("id") id: String): Response<InstrumentResponse>

    // Explore
    @GET("explore")
    suspend fun exploreUsers(
        @Query("currentUserId") currentUserId: String,
        @Query("instrumentIds") instrumentIds: List<String>? = null,
        @Query("level") level: String? = null,
        @Query("searchRadiusKm") searchRadiusKm: Float? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
    ): Response<List<UserWithDistanceResponse>>

    // Chats
    @GET("chats/{userId}")
    suspend fun getChats(@Path("userId") userId: String): Response<List<ChatResponse>>

    @GET("chats/{userId}/{chatId}")
    suspend fun getChat(
        @Path("userId") userId: String,
        @Path("chatId") chatId: String,
    ): Response<ChatResponse>

    @POST("chats")
    suspend fun findOrCreateChat(@Body request: CreateChatRequest): Response<ChatResponse>
}

// Request DTOs
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String)
data class CompleteRegistrationRequest(
    val email: String,
    val password: String,
    val username: String,
    val instruments: List<InstrumentWithLevelRequest> = emptyList()
)
data class GoogleLoginRequest(val token: String)
data class CreateChatRequest(val user1Id: String, val user2Id: String)

data class InstrumentWithLevelRequest(
    val instrumentId: String,
    val level: String
)

data class CreateUserRequest(
    val username: String,
    val email: String? = null,
    val instruments: List<InstrumentWithLevelRequest>,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class UpdateUserRequest(
    val username: String? = null,
    val instruments: List<InstrumentWithLevelRequest>? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

// Response DTOs
data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val userId: String? = null
)

data class UsernameAvailabilityResponse(
    val available: Boolean
)

data class InstrumentResponse(
    val id: String,
    val name: String
)

data class InstrumentWithLevelResponse(
    val id: String,
    val instrument: InstrumentResponse,
    val level: String
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String? = null,
    val profilePictureUrl: String? = null,
    val instruments: List<InstrumentWithLevelResponse>,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String,
    val updatedAt: String
)

data class UserWithDistanceResponse(
    val user: UserResponse,
    val distance: Double
)

data class ChatResponse(
    val id: String,
    val otherUser: UserResponse,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long? = null,
    val createdAt: String,
    val updatedAt: String
)
