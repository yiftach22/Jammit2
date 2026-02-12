package com.jammit.ui.userprofile

import com.jammit.data.model.Chat
import com.jammit.data.model.User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*
import com.jammit.network.RetrofitClient
import com.jammit.repository.UserRepository
import com.jammit.repository.ExploreRepository
import com.jammit.repository.ChatsRepository

class UserProfileViewModel(
    private val userId: String,
    private val userRepository: UserRepository = UserRepository(RetrofitClient.apiService),
    private val exploreRepository: ExploreRepository = ExploreRepository(RetrofitClient.apiService),
    private val chatsRepository: ChatsRepository = ChatsRepository(RetrofitClient.apiService),
    private val currentUserId: String
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _distance = MutableStateFlow<Double?>(null)
    val distance: StateFlow<Double?> = _distance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            userRepository.getUser(userId)
                .onSuccess { loadedUser ->
                    _user.value = loadedUser
                    calculateDistance(loadedUser)
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load user"
                    _isLoading.value = false
                }
        }
    }

    private suspend fun calculateDistance(targetUser: User) {
        // Get current user location
        userRepository.getUser(currentUserId)
            .onSuccess { currentUser ->
                // Check if both users have valid coordinates (not 0.0, which is the default)
                if (currentUser.latitude != 0.0 && currentUser.longitude != 0.0 &&
                    targetUser.latitude != 0.0 && targetUser.longitude != 0.0
                ) {
                    _distance.value = calculateDistance(
                        currentUser.latitude,
                        currentUser.longitude,
                        targetUser.latitude,
                        targetUser.longitude
                    )
                }
            }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val lat1Rad = lat1 * PI / 180.0
        val lat2Rad = lat2 * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun clearError() {
        _error.value = null
    }

    suspend fun findOrCreateChat(): Result<Chat> {
        val result = chatsRepository.findOrCreateChat(currentUserId, userId)
        result.onFailure { _error.value = it.message ?: "Failed to open chat" }
        return result
    }
}

