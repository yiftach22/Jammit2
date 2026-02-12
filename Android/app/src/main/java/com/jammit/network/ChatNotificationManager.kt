package com.jammit.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jammit.MainActivity
import com.jammit.data.UnreadStore
import com.jammit.navigation.NavRoute
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

private const val CHANNEL_ID = "jammit_chat_messages"
private const val NOTIFICATION_ID_BASE = 1000
private const val WS_PATH = "/ws"

/**
 * Connects to the chat WebSocket with only userId (user room) to receive new_message
 * events for notifications and unread badge. Call [connect] when user is logged in,
 * [disconnect] on logout.
 */
object ChatNotificationManager {
    private var socket: Socket? = null
    private var connectedUserId: String? = null
    private var appContext: Context? = null

    fun connect(context: Context, userId: String) {
        if (connectedUserId == userId && socket?.connected() == true) return
        disconnect()
        appContext = context.applicationContext
        connectedUserId = userId
        try {
            val options = IO.Options().apply {
                path = WS_PATH
                query = "userId=$userId"
                forceNew = true
            }
            val s = IO.socket(RetrofitClient.getSocketBaseUrl(), options)
            socket = s
            s.on(Socket.EVENT_CONNECT_ERROR) { }
            s.on("new_message") { args ->
                val obj = args.firstOrNull() as? JSONObject ?: return@on
                val chatId = obj.optString("chatId")
                val senderUsername = obj.optString("senderUsername", "Someone")
                val messageObj = obj.optJSONObject("message")
                val content = messageObj?.optString("content", "") ?: ""
                val isViewingThisChat = UnreadStore.getCurrentChatId() == chatId
                if (!isViewingThisChat) {
                    UnreadStore.addPendingUnread(chatId)
                    val ctx = appContext ?: return@on
                    showNotification(ctx, chatId, senderUsername, content)
                }
            }
            s.connect()
        } catch (_: Exception) { }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        connectedUserId = null
    }

    fun isConnected(): Boolean = socket?.connected() == true

    private fun showNotification(context: Context, chatId: String, senderUsername: String, content: String) {
        createChannel(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_chat", chatId)
        }
        val pending = PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = "New message from $senderUsername"
        val body = content.take(80).let { if (content.length > 80) "$it..." else it }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BASE + chatId.hashCode().and(0x7FFF), notification)
        } catch (_: SecurityException) { }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat messages",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                ?.createNotificationChannel(channel)
        }
    }
}
