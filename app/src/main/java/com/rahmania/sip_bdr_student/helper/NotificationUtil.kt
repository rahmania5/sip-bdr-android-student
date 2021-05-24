package com.rahmania.sip_bdr_student.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.activity.MainActivity
import kotlin.random.Random

class NotificationUtil(private val context: Context) {

    fun showNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 1 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val CHANNEL_ID = "Channel_2002"
        val CHANNEL_NAME = "Firebase Channel"

        val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mBuilder.setChannelId(CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }

        mNotificationManager.notify(Random.nextInt(), mBuilder.build())
    }
}