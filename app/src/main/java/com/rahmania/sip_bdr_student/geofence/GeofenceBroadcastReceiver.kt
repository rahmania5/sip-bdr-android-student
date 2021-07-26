package com.rahmania.sip_bdr_student.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.rahmania.sip_bdr_student.notification.NotificationUtil


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private lateinit var geofenceManager: GeofenceManager
    override fun onReceive(context: Context, intent: Intent) {
        val meetingId = intent.getStringExtra("meetingId")
        var dwellTriggered = intent.getStringExtra("dwellTriggered")
        Log.e(TAG, "From PendingIntent: $dwellTriggered")
        val geofencingEvent: GeofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = geofenceManager.getErrorString(
                context,
                geofencingEvent.errorCode
            )
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
                NotificationUtil(context).showNotification(
                    "Geofence Notification",
                    "Entered location",
                    meetingId
                )
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                if (dwellTriggered == "false") {
                    Toast.makeText(context, "Updating attendance status...", Toast.LENGTH_SHORT).show()
                    NotificationUtil(context).showNotification(
                        "Geofence Notification",
                        "Updating attendance status...",
                        meetingId
                    )
                    dwellTriggered = "true"
                    val i = Intent()
                    Log.e(TAG, "To MainActivity: $dwellTriggered")
                    i.putExtra("dwellTriggered", dwellTriggered)
                    i.action = "UPDATE_ATTENDANCE"
                    context.sendBroadcast(i)
                } else {
                    Log.e(TAG, "Dwell transition already triggered once")
                }
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(
                    context,
                    "You are out of the registered geofence",
                    Toast.LENGTH_SHORT
                ).show()
                NotificationUtil(context).showNotification(
                    "Geofence Notification",
                    "You are out of the registered geofence",
                    meetingId
                )
                context.sendBroadcast(Intent("UPDATE_NEEDS_REVIEW"))
            }
        }
    }

    companion object {
        const val TAG = "GeofenceBR"
    }
}