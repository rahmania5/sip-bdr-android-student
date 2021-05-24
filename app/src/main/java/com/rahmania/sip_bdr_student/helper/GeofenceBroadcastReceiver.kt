package com.rahmania.sip_bdr_student.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.rahmania.sip_bdr_student.activity.MainActivity


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private lateinit var geofenceManager: GeofenceManager
    private var notificationHelper: NotificationHelper? = null
    override fun onReceive(context: Context, intent: Intent) {
        notificationHelper = NotificationHelper(context)

        val geofencingEvent: GeofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = geofenceManager.getErrorString(context,
                geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            return
        }
        val geofenceList: List<Geofence> =
            geofencingEvent.triggeringGeofences
        for (geofence in geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.requestId)
        }

        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, "Entered location", Toast.LENGTH_SHORT).show()
                notificationHelper!!.sendHighPriorityNotification(
                    "Geofence Notification", "Entered location",
                    MainActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, "Updating attendance status...", Toast.LENGTH_SHORT).show()
                notificationHelper!!.sendHighPriorityNotification(
                    "Geofence Notification", "Updating attendance status...",
                    MainActivity::class.java
                )
                context.sendBroadcast(Intent("UPDATE_ATTENDANCE"))
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, "You are out of the registered geofence", Toast.LENGTH_SHORT).show()
                notificationHelper!!.sendHighPriorityNotification(
                    "Geofence Notification", "You are out of the registered geofence",
                    MainActivity::class.java
                )
                context.sendBroadcast(Intent("UPDATE_NEEDS_REVIEW"))
            }
        }
    }

    companion object {
        const val TAG = "GeofenceBR"
    }
}