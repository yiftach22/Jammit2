package com.jammit.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jammit.data.SessionManager
import com.jammit.network.RetrofitClient
import com.jammit.repository.AuthRepository

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(RetrofitClient.apiService),
    private val context: Context? = null
) : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loginWithEmail(email: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.login(email, password)
                .onSuccess { (token, userId) ->
                    context?.let {
                        // Store token and userId for authenticated requests
                        SessionManager.saveToken(it, token)
                        SessionManager.saveUserId(it, userId)
                    }
                    _isAuthenticated.value = true
                    _isLoading.value = false
                    onSuccess(userId)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Login failed"
                    _isLoading.value = false
                }
        }
    }

    fun loginWithGoogle(token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.loginWithGoogle(token)
                .onSuccess { authToken ->
                    // TODO: Store token for authenticated requests
                    _isAuthenticated.value = true
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Google login failed"
                    _isLoading.value = false
                }
        }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.register(email, password)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Registration failed"
                    _isLoading.value = false
                }
        }
    }

    fun checkUsername(username: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.checkUsername(username)
                .onSuccess { available ->
                    _isLoading.value = false
                    onResult(available)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to check username"
                    _isLoading.value = false
                    onResult(false)
                }
        }
    }

    fun completeRegistration(
        email: String,
        password: String,
        username: String,
        instruments: List<com.jammit.network.InstrumentWithLevelRequest>,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.completeRegistration(email, password, username, instruments)
                .onSuccess { (token, userId) ->
                    context?.let {
                        // Store token and userId for authenticated requests
                        SessionManager.saveToken(it, token)
                        SessionManager.saveUserId(it, userId)
                    }
                    _isAuthenticated.value = true
                    _isLoading.value = false
                    onSuccess(userId)
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Registration failed"
                    _isLoading.value = false
                }
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _error.value = null
    }

    fun clearError() {
        _error.value = null
    }
}

