package com.rahmania.sip_bdr_student.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.model.LatLng
import com.rahmania.sip_bdr_student.R


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class GeofenceManager(base: Context?) : ContextWrapper(base) {
    private var pendingIntent: PendingIntent? = null
    fun buildGeofencingRequest(enter: Geofence, dwell: Geofence, exit: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .addGeofence(enter)
            .addGeofence(dwell)
            .addGeofence(exit)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_DWELL)
            .build()
    }

    fun buildGeofence(
        id: String,
        latLng: LatLng,
        radius: Float,
        transitionType: Int
    ): Geofence {
        return Geofence.Builder()
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setRequestId(id)
            .setTransitionTypes(transitionType)
            .setLoiteringDelay(60000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    fun getPendingIntent(meetingId: String?, dwellTriggered: String?): PendingIntent? {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        intent.putExtra("meetingId", meetingId)
        intent.putExtra("dwellTriggered", dwellTriggered)
        pendingIntent =
            PendingIntent.getBroadcast(this, 5001, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }

    fun getErrorString(context: Context, e: Exception): String {
        return if (e is ApiException) {
            getErrorString(context, e.statusCode)
        } else {
            context.resources.getString(R.string.geofence_unknown_error)
        }
    }

    fun getErrorString(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                resources.getString(R.string.geofence_not_available)

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                resources.getString(R.string.geofence_too_many_geofences)

            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                resources.getString(R.string.geofence_too_many_pending_intents)

            else -> resources.getString(R.string.geofence_unknown_error)
        }
    }
}