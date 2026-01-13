package com.jammit.ui.chats

import com.jammit.data.model.Chat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jammit.network.RetrofitClient
import com.jammit.repository.ChatsRepository

class ChatsViewModel(
    private val chatsRepository: ChatsRepository = ChatsRepository(RetrofitClient.apiService),
    private val currentUserId: String
) : ViewModel() {
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            chatsRepository.getChats(currentUserId)
                .onSuccess { chatsList ->
                    _chats.value = chatsList
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to load chats"
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

