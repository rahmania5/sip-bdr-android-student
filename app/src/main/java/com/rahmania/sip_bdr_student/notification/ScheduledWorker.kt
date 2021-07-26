package com.rahmania.sip_bdr_student.notification

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class ScheduledWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {

        Log.d(TAG, "Work START")

        // Get Notification Data
        val title = inputData.getString(NOTIFICATION_TITLE)
        val message = inputData.getString(NOTIFICATION_MESSAGE)
        val meetingId = inputData.getString(MEETING_ID)
        val date = inputData.getString(DATE)
        val startTime = inputData.getString(START_TIME)
        val finishTime = inputData.getString(FINISH_TIME)

        // Show Notification
        NotificationUtil(applicationContext).showNotification(
            title!!,
            message!!,
            meetingId,
            date,
            startTime,
            finishTime
        )
        Log.d(TAG, "Work DONE")

        // Return result
        return Result.success()
    }

    companion object {
        private const val TAG = "ScheduledWorker"
        const val NOTIFICATION_TITLE = "notificationTitle"
        const val NOTIFICATION_MESSAGE = "notificationMessage"
        const val MEETING_ID = "meetingId"
        const val DATE = "date"
        const val START_TIME = "startTime"
        const val FINISH_TIME = "finishTime"
    }
}