package com.rahmania.sip_bdr_student.activity

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.fragment.AccountFragment
import com.rahmania.sip_bdr_student.fragment.ClassroomFragment
import com.rahmania.sip_bdr_student.fragment.LocationSubmissionFragment
import com.rahmania.sip_bdr_student.helper.FirebaseMessageReceiver
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.helper.SharedPreferences.SessionManager


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableUserLocation()
        enableBackgroundLocation()
        sessionManager = SessionManager(this)
        sessionManager.isLogin()

        setUpBottomNav()
    }

    private fun setUpBottomNav() {
        fragmentManager.beginTransaction().add(R.id.container, fragmentClassroom).show(fragmentClassroom).commit()
        fragmentManager.beginTransaction().add(R.id.container, fragmentLocationSubmission).hide(fragmentLocationSubmission).commit()
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
                .setPositiveButton("OK"
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
                    .setPositiveButton("OK"
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
                    Toast.makeText(this@MainActivity,
                        "To allow location access, please go to Settings > Permissions > Location",
                        Toast.LENGTH_LONG).show()
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
                    Toast.makeText(this@MainActivity,
                        "To allow location access, please go to Settings > Permissions > Location",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}