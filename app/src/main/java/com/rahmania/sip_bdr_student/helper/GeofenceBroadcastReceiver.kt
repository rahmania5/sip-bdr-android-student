package com.rahmania.sip_bdr_student.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.rahmania.sip_bdr_student.activity.GeofenceActivity
import com.rahmania.sip_bdr_student.api.ApiClient.getClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    private var notificationHelper: NotificationHelper? = null
    override fun onReceive(context: Context?, intent: Intent) {
        val attendanceId = intent.getIntExtra("id", 0)
        val token = intent.getStringExtra("token")
        notificationHelper = NotificationHelper(context)
        val geofencingEvent: GeofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: ")
            return
        }
        val geofenceList: List<Geofence> =
            geofencingEvent.triggeringGeofences
        for (geofence in geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.requestId)
        }
        when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show()
                notificationHelper!!.sendHighPriorityNotification(
                    "GEOFENCE TRANSITION ENTER", "Enter Location",
                    GeofenceActivity::class.java
                )
                updateStatus(context, attendanceId, token)
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show()
                notificationHelper!!.sendHighPriorityNotification(
                    "GEOFENCE TRANSITION DWELL", "",
                    GeofenceActivity::class.java
                )
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show()
                notificationHelper!!.sendHighPriorityNotification(
                    "GEOFENCE TRANSITION EXIT", "Out Location",
                    GeofenceActivity::class.java
                )
            }
        }
    }

    private fun updateStatus(
        context: Context?,
        attendanceId: Int?,
        token: String?
    ) {
        val apiInterface: ApiInterface = getClient()!!.create(ApiInterface::class.java)
        val attendanceResponse: Call<ResponseBody?>? = apiInterface.updateAttendance(token, attendanceId)
        attendanceResponse?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Berhasil mengisi daftar hadir", Toast.LENGTH_SHORT).show()
                    notificationHelper!!.sendHighPriorityNotification(
                        "Berhasil!", "Berhasil mengisi daftar hadir!",
                        GeofenceActivity::class.java
                    )
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable?) {}
        })
    }

    companion object {
        private const val TAG = "GeofenceBR"
    }
}