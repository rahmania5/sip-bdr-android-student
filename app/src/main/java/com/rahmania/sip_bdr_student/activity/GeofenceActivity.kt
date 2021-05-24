package com.rahmania.sip_bdr_student.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.api.ApiClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import com.rahmania.sip_bdr_student.helper.GeofenceManager
import com.rahmania.sip_bdr_student.helper.NotificationHelper
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.helper.SharedPreferences.SessionManager
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "SameParameterValue")
class GeofenceActivity : FragmentActivity(), OnMapReadyCallback {
//    var weakActivity: WeakReference<GeofenceActivity>? = null
    private lateinit var sessionManager: SharedPreferences
    private lateinit var map: GoogleMap
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceManager: GeofenceManager
    private var notificationHelper: NotificationHelper? = null
    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 2001
    private val BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 3001
    private val GEOFENCE_RADIUS = 50f
    private val ENTER_ID = "Enter ID"
    private val DWELL_ID = "Dwell ID"
    private val EXIT_ID = "Exit ID"
    private var token: String? = null
    private var krsId: Int? = null
    private var meetingId: Int? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var absence = true
    private var result = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        registerReceiver(dwellBroadcastReceiver, IntentFilter("UPDATE_ATTENDANCE"))
        registerReceiver(exitBroadcastReceiver, IntentFilter("UPDATE_NEEDS_REVIEW"))
        val intent = intent
        krsId = intent.extras?.getInt("krsId")
        meetingId = intent.extras?.getInt("meetingId")
        latitude = intent.extras?.getString("latitude")
        longitude = intent.extras?.getString("longitude")

        sessionManager = SessionManager(this)
        sessionManager.isLogin()

        val user = sessionManager.getUserDetail()
        token = user[sessionManager.TOKEN]
        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceManager = GeofenceManager(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val location = LatLng(latitude!!.toDouble(), longitude!!.toDouble())
        if (latitude!!.isEmpty() && longitude!!.isEmpty()) {
            Log.e(TAG, "onFailure: empty latlng")
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 19.0f))
        checkLocationPermission()
        initLocation()
    }

    private fun initLocation() {
        if (latitude!!.isNotEmpty() && longitude!!.isNotEmpty()) {
            val location =
                LatLng(latitude!!.toDouble(), longitude!!.toDouble())
            map.clear()
            addMarker(location)
            addCircle(location, GEOFENCE_RADIUS)
            addGeofence(location, GEOFENCE_RADIUS)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                map.isMyLocationEnabled = true
            }
        }
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Background location granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Background location access is necessary for geofence.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun addMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions().position(latLng)
        map.addMarker(markerOptions)
    }

    private fun addCircle(latLng: LatLng, radius: Float) {
        val circleOptions = CircleOptions()
        circleOptions.center(latLng)
        circleOptions.radius(radius.toDouble())
        circleOptions.strokeColor(Color.argb(200, 81, 203, 206))
        circleOptions.fillColor(Color.argb(60, 81, 203, 206))
        circleOptions.strokeWidth(4f)
        map.addCircle(circleOptions)
    }

    private fun addGeofence(latLng: LatLng, radius: Float) {
//        val geofence = geofenceManager.buildGeofence(
//            GEOFENCE_ID,
//            latLng,
//            radius,
//            Geofence.GEOFENCE_TRANSITION_ENTER or
//                    Geofence.GEOFENCE_TRANSITION_DWELL or
//                    Geofence.GEOFENCE_TRANSITION_EXIT
//        )
        val enter = geofenceManager.buildGeofence(
            ENTER_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER
        )
        val dwell = geofenceManager.buildGeofence(
            DWELL_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_DWELL
        )
        val exit = geofenceManager.buildGeofence(
            EXIT_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_EXIT
        )

        val geofencingRequest = geofenceManager.buildGeofencingRequest(enter, dwell, exit)
        val pendingIntent = geofenceManager.getPendingIntent(krsId, meetingId, token)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "onSuccess: Geofence added successfully!")
                Toast.makeText(
                    this@GeofenceActivity,
                    "Berhasil menambahkan geofence!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                val errorMessage = geofenceManager.getErrorString(this, e)
                Log.e(TAG, "onFailure: $errorMessage")
                Toast.makeText(this@GeofenceActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
    }

    private fun removeGeofence() {
        val pendingIntent = geofenceManager.getPendingIntent(krsId, meetingId, token)
        geofencingClient.removeGeofences(pendingIntent)
            .addOnSuccessListener(
                this
            ) {
                Log.d(TAG, "onSuccess: Geofence removed successfully!")
                Toast.makeText(
                    this@GeofenceActivity,
                    "Berhasil menghapus geofence!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener(this) {
                //
            }
    }

    private var dwellBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            notificationHelper = NotificationHelper(context)
            val i = Intent(context, AlertDialogActivity::class.java)
            i.putExtra("krsId", krsId)
            i.putExtra("meetingId", meetingId)
            i.putExtra("token", token)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)

//            val builder: AlertDialog.Builder = AlertDialog.Builder(
//                ContextThemeWrapper(
//                    context,
//                    R.style.AlertDialog
//                )
//            )
//            builder.setTitle("Konfirmasi Kehadiran")
//            builder.setIcon(R.drawable.ic_checked)
//            builder.setMessage("Konfirmasi kehadiran Anda?")
//            builder.setPositiveButton(
//                "Ya"
//            ) { _, _ ->
//                absence = false
//                try {
//                    updateStatus(krsId, meetingId, token, "Hadir")
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//            }
//
//            val dialog: AlertDialog? = builder.create()
//            dialog?.setCanceledOnTouchOutside(false)
//            dialog?.show()
//
//            val t = Timer()
//            t.schedule(object : TimerTask() {
//                override fun run() {
//                    dialog?.dismiss()
//                    t.cancel()
//                }
//            }, 60000)
//
//            dialog?.setOnDismissListener {
//                if (absence) {
//                    try {
//                        updateStatus(krsId, meetingId, token, "Absen")
//                    } catch (e: JSONException) {
//                        e.printStackTrace()
//                    }
//                }
//            }
        }
    }

    private var exitBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateReviewStatus(token, 1)
        }
    }

//    fun startActivityForResult(context: Context) {
//        val i = Intent(context, AlertDialogActivity::class.java)
//        i.putExtra("krsId", krsId)
//        i.putExtra("meetingId", meetingId)
//        i.putExtra("token", token)
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivityForResult(i, 1)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 1) {
//            if (resultCode == Activity.RESULT_OK) {
//                if (data == null) return
//                absence = data.extras?.getBoolean("absence") == true
//                if (absence) {
//                    updateStatus(krsId, meetingId, token, "Absen")
//                } else if (!absence) {
//                    updateStatus(krsId, meetingId, token, "Hadir")
//                }
//            }  else {
//                return
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dwellBroadcastReceiver)
        unregisterReceiver(exitBroadcastReceiver)
    }

    fun updateReviewStatus(
        token: String?,
        needsReview: Int?
    ) {
        val apiInterface: ApiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val attendanceResponse: Call<ResponseBody?>? = apiInterface.updateReviewStatus(
            token,
            krsId,
            meetingId,
            needsReview
        )
        attendanceResponse?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                Log.d("Review Status", "True")
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
            }
        })
    }

    override fun onBackPressed() {
        removeGeofence()
        super.onBackPressed()
    }

    companion object {
        const val TAG = "GeofenceActivity"
    }
}