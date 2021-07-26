@file:Suppress(
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "SameParameterValue"
)

package com.rahmania.sip_bdr_student.activity

import android.Manifest
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ParseException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.api.ApiClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import com.rahmania.sip_bdr_student.fragment.AccountFragment
import com.rahmania.sip_bdr_student.fragment.ClassroomFragment
import com.rahmania.sip_bdr_student.fragment.LocationSubmissionFragment
import com.rahmania.sip_bdr_student.helper.CustomProgressDialog
import com.rahmania.sip_bdr_student.geofence.GeofenceBroadcastReceiver
import com.rahmania.sip_bdr_student.geofence.GeofenceManager
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.helper.SharedPreferences.SessionManager
import com.rahmania.sip_bdr_student.notification.NotificationUtil
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.DATE
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.FINISH_TIME
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.MEETING_ID
import com.rahmania.sip_bdr_student.notification.ScheduledWorker.Companion.START_TIME
import com.rahmania.sip_bdr_student.viewModel.LatLngViewModel
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val fragmentClassroom: Fragment = ClassroomFragment()
    private val fragmentLocationSubmission: Fragment = LocationSubmissionFragment()
    private val fragmentAccount: Fragment = AccountFragment()
    private val fragmentManager: FragmentManager = supportFragmentManager
    private var active: Fragment = fragmentClassroom

    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 2001
    private val BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 3001

    private lateinit var menu: Menu
    private lateinit var menuItem: MenuItem
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var sessionManager: SharedPreferences
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceManager: GeofenceManager
    private var latLngVM: LatLngViewModel? = null
    private lateinit var progressDialog: CustomProgressDialog

    private val GEOFENCE_RADIUS = 50f
    private val ENTER_ID = "Enter ID"
    private val DWELL_ID = "Dwell ID"
    private val EXIT_ID = "Exit ID"
    private var token: String? = null
    private var meetingId: String? = null
    private var date: String? = null
    private var startTime: String? = null
    private var finishTime: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var dwellTriggered = "false"
    private var absence = "false"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressDialog = CustomProgressDialog(this)
        sessionManager = SessionManager(this)
        sessionManager.isLogin()
        val user = sessionManager.getUserDetail()
        token = user[sessionManager.TOKEN]
        enableUserLocation()
        enableBackgroundLocation()
        setUpBottomNav()

        registerReceiver(dwellBroadcastReceiver, IntentFilter("UPDATE_ATTENDANCE"))
        registerReceiver(exitBroadcastReceiver, IntentFilter("UPDATE_NEEDS_REVIEW"))

        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceManager = GeofenceManager(this)
        val extras = intent.extras
        if (extras != null) {
            meetingId = intent.getStringExtra(MEETING_ID)
            date = intent.getStringExtra(DATE)
            startTime = intent.getStringExtra(START_TIME)
            finishTime = intent.getStringExtra(FINISH_TIME)
            Log.d("Intent", "$meetingId, $date, $startTime, $finishTime")
        } else {
            Log.e("Intent", "Activity does not start from notification")
        }

        latLngVM = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            LatLngViewModel::class.java
        )
        progressDialog.showLoading()
        latLngVM!!.setLocation(token)
        latLngVM!!.getLocation().observe(this,
            Observer<HashMap<String, String>> { stringStringHashMap ->
                if (stringStringHashMap.size > 0) {
                    latitude = stringStringHashMap[latLngVM!!.lat]
                    longitude = stringStringHashMap[latLngVM!!.lng]
                    progressDialog.hideLoading()
                    if (startTime?.isNotEmpty() == true) {
                        if(isInSchedule(startTime!!, finishTime!!, date!!)) {
                            initLocation()
                        } else if (isClassFinished(finishTime!!) && dwellTriggered == "false" && absence == "false") {
                            updateStatus(meetingId, token)
                            removeGeofence()
                        } else if (isClassFinished(finishTime!!) && dwellTriggered == "true") {
                            removeGeofence()
                            dwellTriggered = "false"
                        }
                    }
                }
            })
    }

    private fun setUpBottomNav() {
        fragmentManager.beginTransaction().add(R.id.container, fragmentClassroom).show(
            fragmentClassroom
        ).commit()
        fragmentManager.beginTransaction().add(R.id.container, fragmentLocationSubmission).hide(
            fragmentLocationSubmission
        ).commit()
        fragmentManager.beginTransaction().add(R.id.container, fragmentAccount).hide(fragmentAccount).commit()

        bottomNavigationView = findViewById(R.id.nav_view)
        menu = bottomNavigationView.menu

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_classroom -> {
                    callFragment(0, fragmentClassroom)
                }
                R.id.navigation_location_submission -> {
                    callFragment(1, fragmentLocationSubmission)
                }
                R.id.navigation_account -> {
                    callFragment(2, fragmentAccount)
                }
            }
            false
        }
    }

    private fun callFragment(index: Int, fragment: Fragment) {
        menuItem = menu.getItem(index)
        menuItem.isChecked = true
        fragmentManager.beginTransaction().hide(active).show(fragment).commit()
        active = fragment
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use location functionality")
                .setIcon(R.drawable.ic_location)
                .setPositiveButton(
                    "OK"
                ) { _dialog, i -> //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        FINE_LOCATION_ACCESS_REQUEST_CODE
                    )
                }
                .create()
                .show()
        }
    }

    private fun enableBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs Background Location permission, please accept to use location functionality")
                    .setIcon(R.drawable.ic_location)
                    .setPositiveButton(
                        "OK"
                    ) { _, _ -> //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
                        )
                    }
                    .create()
                    .show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FINE_LOCATION_ACCESS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        FINE_LOCATION_ACCESS_REQUEST_CODE
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "To allow location access, please go to Settings > Permissions > Location",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            BACKGROUND_LOCATION_ACCESS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        BACKGROUND_LOCATION_ACCESS_REQUEST_CODE
                    )
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                ) {
                    Toast.makeText(
                        this@MainActivity,
                        "To allow location access, please go to Settings > Permissions > Location",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val extras = intent.extras
        if (extras != null) {
            meetingId = intent.getStringExtra(MEETING_ID)
            date = intent.getStringExtra(DATE)
            startTime = intent.getStringExtra(START_TIME)
            finishTime = intent.getStringExtra(FINISH_TIME)
            Log.d("Intent", "$meetingId, $date, $startTime, $finishTime")
        } else {
            Log.e("Intent", "Activity does not start from notification")
        }

        if (startTime?.isNotEmpty() == true) {
            if(isInSchedule(startTime!!, finishTime!!, date!!)) {
                initLocation()
            } else if (isClassFinished(finishTime!!) && dwellTriggered == "false" && absence == "false") {
                updateStatus(meetingId, token)
                removeGeofence()
            } else if (isClassFinished(finishTime!!) && dwellTriggered == "true") {
                removeGeofence()
                dwellTriggered = "false"
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun initLocation() {
        if (latitude?.isNotEmpty() == true && longitude?.isNotEmpty() == true) {
            val location =
                LatLng(latitude!!.toDouble(), longitude!!.toDouble())
            addGeofence(location, GEOFENCE_RADIUS)
        }
    }

    private var dwellBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras
            if (extras != null) {
                dwellTriggered = intent.getStringExtra("dwellTriggered").toString()
                Log.e(GeofenceBroadcastReceiver.TAG, "From receiver: $dwellTriggered")
            }
            val i = Intent(context, AlertDialogActivity::class.java)
            i.putExtra("meetingId", meetingId)
            i.putExtra("startTime", startTime)
            i.putExtra("token", token)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }

    private fun addGeofence(latLng: LatLng, radius: Float) {
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
        val pendingIntent = geofenceManager.getPendingIntent(meetingId, dwellTriggered)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener {
                Log.d("Geofence Activity", "onSuccess: Geofence added successfully!")
                Toast.makeText(
                    this@MainActivity,
                    "Berhasil menambahkan geofence!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                val errorMessage = geofenceManager.getErrorString(this, e)
                Log.e("Geofence Activity", "onFailure: $errorMessage")
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
            }
    }

    private var exitBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateReviewStatus(token, 1)
        }
    }

    private fun removeGeofence() {
        val pendingIntent = geofenceManager.getPendingIntent(meetingId, token)
        geofencingClient.removeGeofences(pendingIntent)
            .addOnSuccessListener(
                this
            ) {
                Log.d("Geofence", "onSuccess: Geofence removed successfully!")
                Toast.makeText(
                    this@MainActivity,
                    "Geofence dinonaktifkan!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener(this) {
                //
            }
    }

    fun updateReviewStatus(
        token: String?,
        needsReview: Int?
    ) {
        val apiInterface: ApiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val attendanceResponse: Call<ResponseBody?>? = apiInterface.updateReviewStatus(
            token,
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

    private fun isInSchedule(
        startTime: String,
        finishTime: String,
        date: String
    ): Boolean {
        Log.d("Class Schedule", "Class Schedule: $startTime-$finishTime $date")
        if (startTime.isNotEmpty() && finishTime.isNotEmpty() && date.isNotEmpty()) {
            val sdfTime =
                SimpleDateFormat("HH:mm", Locale.US)
            val sdfDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            try {
                val timeNow = sdfTime.parse(
                    sdfTime.format(
                        Calendar.getInstance().time
                    )
                )
                val dateNow = sdfDate.parse(
                    sdfDate.format(
                        Calendar.getInstance().time
                    )
                )
                val startTimeOutput = sdfTime.parse(startTime)
                val finishTimeOutput = sdfTime.parse(finishTime)
                val dateOutput = sdfDate.parse(date)
                val isStart = timeNow.time >= startTimeOutput.time
                val isNotFinish = timeNow.time < finishTimeOutput.time
                val isToday = dateNow.time == dateOutput.time
                if (isStart && isNotFinish && isToday) {
                    return true
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun isClassFinished(
        finishTime: String
    ): Boolean {
        if (finishTime.isNotEmpty()) {
            val sdfTime =
                SimpleDateFormat("HH:mm", Locale.US)
            try {
                val timeNow = sdfTime.parse(
                    sdfTime.format(
                        Calendar.getInstance().time
                    )
                )
                val finishTimeOutput = sdfTime.parse(finishTime)
                val isFinished = timeNow.time >= finishTimeOutput.time
                if (isFinished) {
                    return true
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return false
    }

    private fun updateStatus(
        meetingId: String?,
        token: String?
    ) {
        val apiInterface: ApiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val attendanceResponse: Call<ResponseBody?>? = apiInterface.updateAttendance(
            token,
            meetingId,
            "Absen",
            0
        )
        attendanceResponse?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                val jsonRESULTS: JSONObject?
                try {
                    jsonRESULTS = JSONObject(response.body()!!.string())
                    val attendance = jsonRESULTS.getJSONObject("attendance")
                    if (attendance.length() != 0) {
                        val attendanceStatus = attendance.getString("presence_status")
                        if (attendanceStatus == "Absen") {
                            NotificationUtil(applicationContext).showNotification(
                                "Status Kehadiran",
                                "Anda tidak hadir pada pertemuan ini."
                            )
                        }
                    }
                    absence = "true"
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
                NotificationUtil(applicationContext).showNotification(
                    "Status Kehadiran", "Anda tidak hadir pada pertemuan ini."
                )
            }
        })
    }

    override fun onPause() {
        super.onPause()
        val extras = intent.extras
        if (extras != null) {
            if (isClassFinished(finishTime!!) && dwellTriggered == "false" && absence == "false") {
                updateStatus(meetingId, token)
                removeGeofence()
            } else if (isClassFinished(finishTime!!) && dwellTriggered == "true") {
                removeGeofence()
                dwellTriggered = "false"
            }
        } else {
            Log.e("Intent", "Activity does not start from notification")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dwellBroadcastReceiver)
        unregisterReceiver(exitBroadcastReceiver)
        removeGeofence()
    }
}