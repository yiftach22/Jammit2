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
import com.jammit.data.model.MusicianLevel
import com.jammit.network.RetrofitClient
import com.jammit.repository.AuthRepository
import com.jammit.repository.InstrumentsRepository
import com.jammit.ui.instruments.UserInstrument

data class RegisterInstrumentsUiState(
    val catalog: List<Instrument> = emptyList(),
    val selected: List<UserInstrument> = emptyList(),
    val searchQuery: String = "",
    val showPicker: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showValidationError: Boolean = false,
)

class RegisterInstrumentsViewModel(
    private val email: String,
    private val password: String,
    private val username: String,
    private val context: Context? = null,
    private val authRepository: AuthRepository = AuthRepository(RetrofitClient.apiService),
    private val instrumentsRepository: InstrumentsRepository = InstrumentsRepository(RetrofitClient.apiService)
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterInstrumentsUiState())
    val uiState: StateFlow<RegisterInstrumentsUiState> = _uiState.asStateFlow()

    init {
        loadInstruments()
    }

    private fun loadInstruments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            instrumentsRepository.getInstruments()
                .onSuccess { instruments ->
                    _uiState.value =
                        _uiState.value.copy(
                            catalog = instruments,
                            isLoading = false,
                            error = if (instruments.isEmpty()) "No instruments found on server. Please try again." else null,
                        )
                }
                .onFailure { exception ->
                    _uiState.value =
                        _uiState.value.copy(
                            catalog = emptyList(),
                            isLoading = false,
                            error = exception.message ?: "Failed to load instruments",
                        )
                }
        }
    }

    fun openPicker() {
        _uiState.value = _uiState.value.copy(showPicker = true, showValidationError = false)
    }

    fun closePicker() {
        _uiState.value = _uiState.value.copy(showPicker = false, searchQuery = "")
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun addInstrument(instrumentId: String, level: MusicianLevel) {
        val current = _uiState.value
        if (current.selected.any { it.instrumentId == instrumentId }) return
        _uiState.value =
            current.copy(
                selected = current.selected + UserInstrument(instrumentId, level),
                showValidationError = false,
            )
    }

    fun removeInstrument(instrumentId: String) {
        val current = _uiState.value
        _uiState.value = current.copy(selected = current.selected.filterNot { it.instrumentId == instrumentId })
    }

    fun completeRegistration(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selected.isEmpty()) {
                _uiState.value = state.copy(showValidationError = true)
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null)

            val instruments =
                state.selected.map {
                    com.jammit.network.InstrumentWithLevelRequest(
                        instrumentId = it.instrumentId,
                        level = it.level.name,
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
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(userId)
                }
                .onFailure { exception ->
                    _uiState.value =
                        _uiState.value.copy(
                            error = exception.message ?: "Failed to complete registration",
                            isLoading = false,
                        )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, showValidationError = false)
    }
}
