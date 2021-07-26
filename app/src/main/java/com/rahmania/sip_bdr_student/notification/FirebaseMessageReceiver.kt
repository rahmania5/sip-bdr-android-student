package com.rahmania.sip_bdr_student.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.DATE
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.FINISH_TIME
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.MEETING_ID
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.NOTIFICATION_MESSAGE
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.NOTIFICATION_TITLE
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.START_TIME
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessageReceiver: FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

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
        val meetingId = remote.data["meetingId"]
        val date = remote.data["date"]
        val startTime = remote.data["startTime"]
        val finishTime = remote.data["finishTime"]

        val requestId = remote.data["meetingId"]?.toInt()

        val alarmMgr = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent =
            Intent(applicationContext, NotificationBroadcastReceiver::class.java).let { intent ->
                intent.putExtra(NOTIFICATION_TITLE, title)
                intent.putExtra(NOTIFICATION_MESSAGE, message)
                intent.putExtra(MEETING_ID, meetingId)
                intent.putExtra(DATE, date)
                intent.putExtra(START_TIME, startTime)
                intent.putExtra(FINISH_TIME, finishTime)
                PendingIntent.getBroadcast(
                    applicationContext,
                    requestId!!,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            }

        if (scheduledTimeString == "0") {
            alarmMgr.cancel(alarmIntent)
        } else {
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
    }

    companion object {
        private const val TAG = "FirebaseMsgService"
    }
}