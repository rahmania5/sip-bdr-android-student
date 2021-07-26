package com.rahmania.sip_bdr_student.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.DATE
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.FINISH_TIME
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.MEETING_ID
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.NOTIFICATION_MESSAGE
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.NOTIFICATION_TITLE
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.START_TIME

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val title = it.getStringExtra(NOTIFICATION_TITLE)
            val message = it.getStringExtra(NOTIFICATION_MESSAGE)
            val meetingId = it.getStringExtra(MEETING_ID)
            val date = it.getStringExtra(DATE)
            val startTime = it.getStringExtra(START_TIME)
            val finishTime = it.getStringExtra(FINISH_TIME)

            // Create Notification Data
            val notificationData = Data.Builder()
                .putString(NOTIFICATION_TITLE, title)
                .putString(NOTIFICATION_MESSAGE, message)
                .putString(MEETING_ID, meetingId)
                .putString(DATE, date)
                .putString(START_TIME, startTime)
                .putString(FINISH_TIME, finishTime)
                .build()

            // Init Worker
            val work = OneTimeWorkRequest.Builder(ScheduledWorker::class.java)
                .setInputData(notificationData)
                .build()

            // Start Worker
            WorkManager.getInstance().beginWith(work).enqueue()

            Log.d(javaClass.name, "WorkManager is Enqueued.")
        }
    }
}
