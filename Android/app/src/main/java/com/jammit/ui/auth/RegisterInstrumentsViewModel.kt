package com.jammit.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jammit.data.SessionManager
import com.jammit.data.model.Instrument
import com.jammit.data.model.InstrumentWithLevel
import com.jammit.data.model.MusicianLevel
import com.jammit.network.RetrofitClient
import com.jammit.repository.AuthRepository
import com.jammit.repository.InstrumentsRepository

data class InstrumentRow(
    val instrument: Instrument? = null,
    val level: MusicianLevel? = null
)

class RegisterInstrumentsViewModel(
    private val email: String,
    private val password: String,
    private val username: String,
    private val context: Context? = null,
    private val authRepository: AuthRepository = AuthRepository(RetrofitClient.apiService),
    private val instrumentsRepository: InstrumentsRepository = InstrumentsRepository(RetrofitClient.apiService)
) : ViewModel() {
    private val _instrumentRows = MutableStateFlow<MutableList<InstrumentRow>>(mutableListOf(InstrumentRow()))
    val instrumentRows: StateFlow<List<InstrumentRow>> = _instrumentRows.asStateFlow()

    private val _availableInstruments = MutableStateFlow<List<Instrument>>(emptyList())
    val availableInstruments: StateFlow<List<Instrument>> = _availableInstruments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadInstruments()
    }

    private fun loadInstruments() {
        viewModelScope.launch {
            instrumentsRepository.getInstruments()
                .onSuccess { instruments ->
                    _availableInstruments.value = instruments
                    if (instruments.isEmpty()) {
                        _error.value = "No instruments found on server. Please try again."
                    }
                }
                .onFailure { exception ->
                    _availableInstruments.value = emptyList()
                    _error.value = exception.message ?: "Failed to load instruments"
                }
        }
    }

    fun addRow() {
        _instrumentRows.value = (_instrumentRows.value + InstrumentRow()).toMutableList()
    }

    fun removeRow(index: Int) {
        val updated = _instrumentRows.value.toMutableList()
        updated.removeAt(index)
        _instrumentRows.value = updated
    }

    fun updateInstrument(index: Int, instrument: Instrument) {
        val updated = _instrumentRows.value.toMutableList()
        updated[index] = updated[index].copy(instrument = instrument)
        _instrumentRows.value = updated
    }

    fun updateLevel(index: Int, level: MusicianLevel) {
        val updated = _instrumentRows.value.toMutableList()
        updated[index] = updated[index].copy(level = level)
        _instrumentRows.value = updated
    }

    fun completeRegistration(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Convert rows to InstrumentWithLevelRequest list (filter out incomplete rows)
            val instruments = _instrumentRows.value
                .filter { it.instrument != null && it.level != null }
                .map { 
                    com.jammit.network.InstrumentWithLevelRequest(
                        instrumentId = it.instrument!!.id,
                        level = it.level!!.name
                    )
                }

            // Complete registration - create user with all data
            authRepository.completeRegistration(email.trim(), password, username.trim(), instruments)
                .onSuccess { (token, userId) ->
                    context?.let {
                        // Store token and userId for authenticated requests
                        SessionManager.saveToken(it, token)
                        SessionManager.saveUserId(it, userId)
                    }
                    _isLoading.value = false
                    onSuccess(userId)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to complete registration"
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
