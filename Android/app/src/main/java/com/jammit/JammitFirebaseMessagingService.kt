package com.jammit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles FCM messages when the app is in background or killed.
 * When the server sends a notification+data message, the system shows the notification.
 * When the user taps it, the launcher Activity is started with data in the intent (chatId etc.).
 */
class JammitFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token is sent to backend when app opens (MainNavigation)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // When app is in background/killed, notification payload is shown by the system.
        // We only need to handle data-only messages here (show notification ourselves).
        val data = message.data
        if (data.isEmpty()) return
        val chatId = data["chatId"] ?: return
        val senderUsername = data["senderUsername"] ?: "Someone"
        val content = data["content"] ?: ""
        showNotification(chatId, senderUsername, content)
    }

    private fun showNotification(chatId: String, senderUsername: String, content: String) {
        createChannel()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("chatId", chatId)
            putExtra("navigate_to_chat", chatId)
        }
        val pending = PendingIntent.getActivity(
            this,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = "New message from $senderUsername"
        val body = content.take(80).let { if (content.length > 80) "$it..." else it }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_BASE + chatId.hashCode().and(0x7FFF), notification)
        } catch (_: SecurityException) { }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Chat messages",
                NotificationManager.IMPORTANCE_HIGH
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                ?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "jammit_chat_messages"
        private const val NOTIFICATION_ID_BASE = 1000
    }
}
