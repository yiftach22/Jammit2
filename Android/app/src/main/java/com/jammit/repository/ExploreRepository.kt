package com.jammit.repository

import com.jammit.data.mapper.UserMapper
import com.jammit.data.model.MusicianLevel
import com.jammit.data.model.User
import com.jammit.network.ApiService

data class ExploreFilters(
    val instrumentIds: List<String>? = null,
    val level: MusicianLevel? = null,
    val searchRadiusKm: Float? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class UserWithDistance(
    val user: User,
    val distance: Double
)

class ExploreRepository(private val apiService: ApiService) {
    suspend fun exploreUsers(
        currentUserId: String,
        filters: ExploreFilters
    ): Result<List<UserWithDistance>> {
        return try {
            val response = apiService.exploreUsers(
                currentUserId = currentUserId,
                instrumentIds = filters.instrumentIds,
                level = filters.level?.name,
                searchRadiusKm = filters.searchRadiusKm,
                latitude = filters.latitude,
                longitude = filters.longitude
            )
            if (response.isSuccessful) {
                val usersWithDistance = response.body()?.map {
                    UserWithDistance(
                        user = UserMapper.toUser(it.user),
                        distance = it.distance
                    )
                } ?: emptyList()
                Result.success(usersWithDistance)
            } else {
                Result.failure(Exception("Failed to explore users: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
