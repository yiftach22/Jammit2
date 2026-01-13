package com.jammit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jammit.network.RetrofitClient
import com.jammit.repository.AuthRepository

class RegisterUsernameViewModel(
    private val email: String,
    private val password: String,
    private val authRepository: AuthRepository = AuthRepository(RetrofitClient.apiService)
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _usernameAvailable = MutableStateFlow<Boolean?>(null)
    val usernameAvailable: StateFlow<Boolean?> = _usernameAvailable.asStateFlow()

    fun checkUsername(username: String) {
        val trimmed = username.trim()
        if (trimmed.length < 3) {
            _usernameAvailable.value = null
            return
        }
        viewModelScope.launch {
            authRepository.checkUsername(trimmed)
                .onSuccess { available ->
                    _usernameAvailable.value = available
                    if (!available) {
                        _error.value = "Username is already taken"
                    } else {
                        _error.value = null
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to check username"
                    _usernameAvailable.value = null
                }
        }
    }

    fun validateAndProceed(username: String, onSuccess: (String) -> Unit) {
        val trimmed = username.trim()
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Check username availability
            authRepository.checkUsername(trimmed)
                .onSuccess { available ->
                    if (available) {
                        _isLoading.value = false
                        // Pass email, password, and username encoded
                        onSuccess("$email|$password|$trimmed")
                    } else {
                        _error.value = "Username is already taken"
                        _isLoading.value = false
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to validate username"
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
