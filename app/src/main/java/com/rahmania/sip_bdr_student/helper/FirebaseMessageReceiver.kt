package com.rahmania.sip_bdr_student.helper

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rahmania.sip_bdr_student.helper.ScheduledWorker.Companion.NOTIFICATION_MESSAGE
import com.rahmania.sip_bdr_student.helper.ScheduledWorker.Companion.NOTIFICATION_TITLE
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessageReceiver: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

//        // Check if message contains a data payload.
//        if (remoteMessage.data.isNotEmpty()) {
//            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
//        }
//        // Check if message contains a notification payload.
//        remoteMessage.notification?.let {
//            Log.d(TAG, "Message Notification Body: ${it.body}")
//        }

        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            // Check whether notification is scheduled or not
            val isScheduled = remoteMessage.data["isScheduled"]?.toBoolean()
            isScheduled?.let {
                if (it) {
                    scheduleAlarm(remoteMessage)
                } else {
                    showNotification(remoteMessage)
                }
            }
        }

        if(remoteMessage.notification != null){
            showNotification(remoteMessage)
        }
    }

    private fun showNotification(remote: RemoteMessage) {
        val title = remote.notification?.title
        val message = remote.notification?.body

        NotificationUtil(applicationContext).showNotification(title!!, message!!)
    }

    private fun scheduleAlarm(remote: RemoteMessage) {
        val title = remote.data["title"]
        val message = remote.data["body"]
        val scheduledTimeString = remote.data["scheduledTime"]

        val alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent =
            Intent(applicationContext, NotificationBroadcastReceiver::class.java).let { intent ->
                intent.putExtra(NOTIFICATION_TITLE, title)
                intent.putExtra(NOTIFICATION_MESSAGE, message)
                PendingIntent.getBroadcast(applicationContext, 0, intent, 0)
            }

        // Parse Schedule time
        val scheduledTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .parse(scheduledTimeString!!)

        scheduledTime?.let {
            // With set(), it'll set non repeating one time alarm.
            alarmMgr.set(
                AlarmManager.RTC_WAKEUP,
                it.time,
                alarmIntent
            )
        }
    }

    companion object {
        private const val TAG = "FirebaseMsgService"
    }
}