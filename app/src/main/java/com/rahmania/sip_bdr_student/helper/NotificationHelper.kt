package com.rahmania.sip_bdr_student.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.rahmania.sip_bdr_student.R
import java.util.*


class NotificationHelper(base: Context?) : ContextWrapper(base) {
    private val CHANNEL_NAME = "Final project channel"
    private val CHANNEL_ID = "com.example.notifications$CHANNEL_NAME"

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannels() {
        val notificationChannel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
    }

    fun sendHighPriorityNotification(
        title: String?,
        body: String?,
        activityName: Class<*>?
    ) {
        val intent = Intent(this, activityName)
        val pendingIntent =
            PendingIntent.getActivity(this, 267, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(
                    NotificationCompat.BigTextStyle().setSummaryText("summary")
                        .setBigContentTitle(title).bigText(body)
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(this).notify(Random().nextInt(), notification)
    }

    companion object {
        private const val TAG = "NotificationHelper"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }
}