package com.jammit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loginWithEmail(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000) // Simulate network call
            _isAuthenticated.value = true
            _isLoading.value = false
            onSuccess()
        }
    }

    fun loginWithGoogle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000) // Simulate network call
            _isAuthenticated.value = true
            _isLoading.value = false
            onSuccess()
        }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1000) // Simulate network call
            _isAuthenticated.value = true
            _isLoading.value = false
            onSuccess()
        }
    }

    fun logout() {
        _isAuthenticated.value = false
    }
}

