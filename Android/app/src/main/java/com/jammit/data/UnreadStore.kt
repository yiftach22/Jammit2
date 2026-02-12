package com.jammit.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicReference

/**
 * Tracks unread message count for the Chats tab badge and persists last-read per chat.
 * Call [init] from Application.onCreate. Call [updateFromChatsList] when chats load,
 * [setLastRead] when user opens a chat, and [addPendingUnread] when receiving new_message.
 */
object UnreadStore {
    private const val PREFS_NAME = "jammit_unread"
    private const val PREFIX_LAST_READ = "lastRead_"

    private var prefs: SharedPreferences? = null
    private val pendingUnread = mutableMapOf<String, Int>()
    private var lastKnownChats: List<ChatInfo> = emptyList()
    private val currentChatIdRef = AtomicReference<String?>(null)

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun setCurrentChatId(chatId: String?) { currentChatIdRef.set(chatId) }
    fun getCurrentChatId(): String? = currentChatIdRef.get()

    data class ChatInfo(val id: String, val lastMessageTimestamp: Long)

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun getLastRead(chatId: String): Long {
        return prefs?.getLong(PREFIX_LAST_READ + chatId, 0L) ?: 0L
    }

    fun setLastRead(context: Context, chatId: String) {
        init(context)
        prefs?.edit()?.putLong(PREFIX_LAST_READ + chatId, System.currentTimeMillis())?.apply()
        pendingUnread.remove(chatId)
        recompute()
    }

    fun updateFromChatsList(chats: List<ChatInfo>) {
        lastKnownChats = chats
        recompute()
    }

    fun addPendingUnread(chatId: String) {
        pendingUnread[chatId] = (pendingUnread[chatId] ?: 0) + 1
        recompute()
    }

    private fun recompute() {
        val base = lastKnownChats.count { getLastRead(it.id) < it.lastMessageTimestamp }
        val pending = pendingUnread.values.sum()
        _unreadCount.value = (base + pending).coerceAtLeast(0)
    }
}
