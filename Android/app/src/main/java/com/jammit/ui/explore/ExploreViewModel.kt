package com.jammit.ui.explore

import com.jammit.data.model.Instrument
import com.jammit.data.model.MusicianLevel
import com.jammit.data.model.User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jammit.network.RetrofitClient
import com.jammit.repository.ExploreRepository
import com.jammit.repository.InstrumentsRepository
import com.jammit.repository.UserRepository
import com.jammit.repository.ExploreFilters as RepoExploreFilters

data class ExploreFilters(
    val selectedInstruments: Set<Instrument> = emptySet(),
    val selectedLevel: MusicianLevel? = null,
    val searchRadiusKm: Float = 10f
)

class ExploreViewModel(
    private val exploreRepository: ExploreRepository = ExploreRepository(RetrofitClient.apiService),
    private val instrumentsRepository: InstrumentsRepository = InstrumentsRepository(RetrofitClient.apiService),
    private val userRepository: UserRepository = UserRepository(RetrofitClient.apiService),
    private val currentUserId: String
) : ViewModel() {
    private val _filters = MutableStateFlow(ExploreFilters())
    val filters: StateFlow<ExploreFilters> = _filters.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<Pair<User, Double>>>(emptyList())
    val filteredUsers: StateFlow<List<Pair<User, Double>>> = _filteredUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _availableInstruments = MutableStateFlow<List<Instrument>>(emptyList())
    val availableInstruments: StateFlow<List<Instrument>> = _availableInstruments.asStateFlow()

    init {
        loadUsers()
        loadInstruments()
    }

    private fun loadInstruments() {
        viewModelScope.launch {
            instrumentsRepository.getInstruments()
                .onSuccess { instruments ->
                    _availableInstruments.value = instruments
                    if (instruments.isEmpty()) {
                        _error.value = "No instruments found on server"
                    }
                }
                .onFailure { exception ->
                    _availableInstruments.value = emptyList()
                    _error.value = exception.message ?: "Failed to load instruments"
                }
        }
    }

    fun updateFilters(filters: ExploreFilters) {
        _filters.value = filters
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val repoFilters = RepoExploreFilters(
                instrumentIds = _filters.value.selectedInstruments.map { it.id }.takeIf { it.isNotEmpty() },
                level = _filters.value.selectedLevel,
                searchRadiusKm = _filters.value.searchRadiusKm
            )

            exploreRepository.exploreUsers(currentUserId, repoFilters)
                .onSuccess { usersWithDistance ->
                    _filteredUsers.value = usersWithDistance.map { Pair(it.user, it.distance) }
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load users"
                    _isLoading.value = false
                }
        }
    }

    fun updateMyLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            userRepository.getUser(currentUserId)
                .onSuccess { user ->
                    userRepository.updateUser(
                        currentUserId,
                        user.copy(latitude = latitude, longitude = longitude),
                    )
                }
            // Always reload explore results after attempting location update (backend uses current user location)
            loadUsers()
        }
    }

    fun clearError() {
        _error.value = null
    }
}

