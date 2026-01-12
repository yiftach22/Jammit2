package com.jammit.ui.profile

import com.jammit.data.model.Instrument
import com.jammit.data.model.InstrumentWithLevel
import com.jammit.data.model.MusicianLevel
import com.jammit.data.model.User
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    // Mock current user data
    private val _currentUser = MutableStateFlow<User>(
        User(
            id = "current_user",
            username = "John Doe",
            instruments = listOf(
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[0], MusicianLevel.INTERMEDIATE), // Guitar
                InstrumentWithLevel(Instrument.ALL_INSTRUMENTS[1], MusicianLevel.BEGINNER)  // Piano
            ),
            latitude = 40.7128,
            longitude = -74.0060
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun updateUsername(username: String) {
        _currentUser.value = _currentUser.value.copy(username = username)
    }

    fun updateInstruments(instruments: List<InstrumentWithLevel>) {
        _currentUser.value = _currentUser.value.copy(instruments = instruments)
    }

    fun saveProfile(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            delay(500) // Simulate network call
            _isSaving.value = false
            onSuccess()
        }
    }
}

