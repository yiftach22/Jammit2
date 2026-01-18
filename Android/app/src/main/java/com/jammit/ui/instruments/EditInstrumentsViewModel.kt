package com.jammit.ui.instruments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jammit.data.model.Instrument
import com.jammit.data.model.InstrumentWithLevel
import com.jammit.data.model.MusicianLevel
import com.jammit.data.model.User
import com.jammit.network.RetrofitClient
import com.jammit.repository.InstrumentsRepository
import com.jammit.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditInstrumentsUiState(
    val catalog: List<Instrument> = emptyList(),
    val selected: List<UserInstrument> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)

class EditInstrumentsViewModel(
    private val userId: String,
    private val userRepository: UserRepository = UserRepository(RetrofitClient.apiService),
    private val instrumentsRepository: InstrumentsRepository = InstrumentsRepository(RetrofitClient.apiService),
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditInstrumentsUiState(isLoading = true))
    val uiState: StateFlow<EditInstrumentsUiState> = _uiState.asStateFlow()

    private var loadedUser: User? = null

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val instrumentsResult = instrumentsRepository.getInstruments()
            val userResult = userRepository.getUser(userId)

            instrumentsResult
                .onSuccess { catalog ->
                    _uiState.value = _uiState.value.copy(catalog = catalog)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to load instruments")
                }

            userResult
                .onSuccess { user ->
                    loadedUser = user
                    _uiState.value =
                        _uiState.value.copy(
                            selected = user.instruments.map { UserInstrument(it.instrument.id, it.level) },
                        )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to load user")
                }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun updateSearchQuery(q: String) {
        _uiState.value = _uiState.value.copy(searchQuery = q)
    }

    fun addInstrument(instrumentId: String, level: MusicianLevel) {
        val state = _uiState.value
        if (state.selected.any { it.instrumentId == instrumentId }) return
        _uiState.value = state.copy(selected = state.selected + UserInstrument(instrumentId, level))
    }

    fun removeInstrument(instrumentId: String) {
        val state = _uiState.value
        _uiState.value = state.copy(selected = state.selected.filterNot { it.instrumentId == instrumentId })
    }

    fun changeLevel(instrumentId: String, level: MusicianLevel) {
        val state = _uiState.value
        _uiState.value =
            state.copy(
                selected =
                    state.selected.map {
                        if (it.instrumentId == instrumentId) it.copy(level = level) else it
                    },
            )
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = loadedUser ?: return@launch
            val state = _uiState.value

            _uiState.value = state.copy(isSaving = true, error = null)

            val catalogById = state.catalog.associateBy { it.id }
            val updatedInstruments =
                state.selected.mapNotNull { ui ->
                    val inst = catalogById[ui.instrumentId] ?: return@mapNotNull null
                    InstrumentWithLevel(inst, ui.level)
                }

            userRepository.updateUser(userId, user.copy(instruments = updatedInstruments))
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.value =
                        _uiState.value.copy(
                            isSaving = false,
                            error = e.message ?: "Failed to save",
                        )
                }
        }
    }
}

