package com.jammit.ui.chatdetail

import com.jammit.data.model.Message
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.jammit.network.RetrofitClient
import com.jammit.repository.ChatsRepository
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ChatDetailViewModel(
    private val chatId: String,
    private val currentUserId: String,
    private val chatsRepository: ChatsRepository = ChatsRepository(RetrofitClient.apiService),
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var socket: Socket? = null

    init {
        loadMessages()
        connectSocket()
    }

    private fun parseCreatedAt(createdAt: String): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(createdAt)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(createdAt)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            chatsRepository.getMessages(currentUserId, chatId)
                .onSuccess { list ->
                    _messages.value = list
                    _isLoading.value = false
                }
                .onFailure { e ->
                    _error.value = e.message ?: "Failed to load messages"
                    _isLoading.value = false
                }
        }
    }

    private fun connectSocket() {
        viewModelScope.launch {
            try {
                val baseUrl = RetrofitClient.getSocketBaseUrl()
                val options = IO.Options().apply {
                    path = "/ws"
                    query = "userId=$currentUserId&chatId=$chatId"
                    forceNew = true
                }
                val s = IO.socket(baseUrl, options)
                socket = s

                s.on(Socket.EVENT_CONNECT) {
                    _connectionState.value = ConnectionState.Connected
                }
                s.on(Socket.EVENT_DISCONNECT) {
                    _connectionState.value = ConnectionState.Disconnected
                }
                s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    _connectionState.value = ConnectionState.Error(
                        (args.firstOrNull() as? Exception)?.message ?: "Connection failed"
                    )
                }
                s.on("error") { args ->
                    val msg = (args.firstOrNull() as? JSONObject)?.optString("message", "Error")
                        ?: (args.firstOrNull() as? String) ?: "Error"
                    _error.value = msg
                }
                s.on("message") { args ->
                    val obj = args.firstOrNull() as? JSONObject ?: return@on
                    val id = obj.optString("id")
                    val chatIdStr = obj.optString("chatId")
                    val senderId = obj.optString("senderId")
                    val content = obj.optString("content")
                    val createdAt = obj.optString("createdAt")
                    val ts = parseCreatedAt(createdAt)
                    val message = Message(
                        id = id,
                        chatId = chatIdStr,
                        senderId = senderId,
                        content = content,
                        timestamp = ts
                    )
                    _messages.value = _messages.value + message
                }

                s.connect()
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(e.message ?: "Failed to connect")
            }
        }
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) return

        val s = socket
        if (s != null && s.connected()) {
            val payload = JSONObject().apply {
                put("content", trimmed)
            }
            s.emit("message", payload)
        } else {
            _error.value = "Not connected. Send when connected."
        }
    }

    fun isCurrentUser(senderId: String): Boolean = senderId == currentUserId

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    sealed class ConnectionState {
        data object Disconnected : ConnectionState()
        data object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
}

/** Factory to create [ChatDetailViewModel] with [chatId] and [currentUserId]. */
class ChatDetailViewModelFactory(
    private val chatId: String,
    private val currentUserId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatDetailViewModel::class.java)) {
            return ChatDetailViewModel(chatId, currentUserId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
