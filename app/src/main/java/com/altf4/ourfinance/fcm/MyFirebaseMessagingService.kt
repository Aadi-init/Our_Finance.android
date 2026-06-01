package com.altf4.ourfinance.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.altf4.ourfinance.MainActivity
import com.altf4.ourfinance.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if user has enabled push notifications in AccessibilityScreen
        val sharedPrefs = getSharedPreferences("OurFinancePrefs", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPrefs.getBoolean("push_notifications_enabled", true)

        if (!notificationsEnabled) {
            Log.d("FCM", "Push notifications are disabled by user. Ignoring message.")
            return
        }

        // Handle Notification Payload
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "New Notification", it.body ?: "")
        }

        // Handle Data Payload (if any)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "New Notification"
            val body = remoteMessage.data["body"] ?: ""
            if (remoteMessage.notification == null) {
                sendNotification(title, body)
            }
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        // Note: The token is registered in DashboardViewModel on app start.
        // If it changes during a session, it will be caught on next launch.
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("OPEN_NOTIFICATIONS", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "our_finance_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Our Finance Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
