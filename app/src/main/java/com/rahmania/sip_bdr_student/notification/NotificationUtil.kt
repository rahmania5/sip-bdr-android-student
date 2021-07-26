package com.rahmania.sip_bdr_student.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.activity.MainActivity
import kotlin.random.Random
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.DATE
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.FINISH_TIME
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.MEETING_ID
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.START_TIME

class NotificationUtil(private val context: Context) {

    fun showNotification(
        title: String,
        message: String,
        meetingId: String? = null,
        date: String? = null,
        startTime: String? = null,
        finishTime: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (meetingId?.isNotEmpty() == true) {
            intent.putExtra(MEETING_ID, meetingId)
            intent.putExtra(DATE, date)
            intent.putExtra(START_TIME, startTime)
            intent.putExtra(FINISH_TIME, finishTime)
            intent.action = "CLASS_STARTS"
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 101 /* Request code */, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val CHANNEL_ID = "Channel_2002"
        val CHANNEL_NAME = "Notification Channel"

        val mBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
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
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            mNotificationManager.createNotificationChannel(notificationChannel)
        }

        mNotificationManager.notify(Random.nextInt(), mBuilder.build())
    }
}