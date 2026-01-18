package com.jammit.ui.profile

import com.jammit.data.model.Instrument
import com.jammit.data.model.InstrumentWithLevel
import com.jammit.data.model.User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jammit.network.RetrofitClient
import com.jammit.repository.UserRepository
import com.jammit.repository.InstrumentsRepository

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(RetrofitClient.apiService),
    private val instrumentsRepository: InstrumentsRepository = InstrumentsRepository(RetrofitClient.apiService),
    private val currentUserId: String
) : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _availableInstruments = MutableStateFlow<List<Instrument>>(emptyList())
    val availableInstruments: StateFlow<List<Instrument>> = _availableInstruments.asStateFlow()

    init {
        loadUser()
        loadInstruments()
    }

    private fun loadInstruments() {
        viewModelScope.launch {
            instrumentsRepository.getInstruments()
                .onSuccess { instruments ->
                    _availableInstruments.value = instruments
                }
                .onFailure { exception ->
                    // Fallback to default instruments if API fails
                    _availableInstruments.value = Instrument.ALL_INSTRUMENTS
                }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            userRepository.getUser(currentUserId)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load user"
                    _isLoading.value = false
                }
        }
    }

    fun updateUsername(username: String) {
        _currentUser.value = _currentUser.value?.copy(username = username)
    }

    fun updateInstruments(instruments: List<InstrumentWithLevel>) {
        _currentUser.value = _currentUser.value?.copy(instruments = instruments)
    }

    fun saveProfile(onSuccess: () -> Unit = {}) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            userRepository.updateUser(user.id, user)
                .onSuccess { updatedUser ->
                    _currentUser.value = updatedUser
                    _isSaving.value = false
                    onSuccess()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to save profile"
                    _isSaving.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

