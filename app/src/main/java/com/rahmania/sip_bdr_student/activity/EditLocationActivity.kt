package com.rahmania.sip_bdr_student.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.api.ApiClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import com.rahmania.sip_bdr_student.helper.CustomProgressDialog
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class EditLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private var address: String? = null
    private var longitude: String? = null
    private var latitude: String? = null
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SharedPreferences? = null
    lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var etAddress: EditText? = null
    private var etLongitude: EditText? = null
    private var etLatitude: EditText? = null
    private var btnEditLocation: Button? = null

    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_location)
        progressDialog = CustomProgressDialog(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        etAddress = findViewById(R.id.et_address)
        etLongitude = findViewById(R.id.et_longitude)
        etLatitude = findViewById(R.id.et_latitude)

        btnEditLocation = findViewById(R.id.btn_edit_location)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user[sessionManager!!.TOKEN]

        id = intent.extras?.getInt("id")
        address = intent.extras?.getString("address")
        longitude = intent.extras?.getString("longitude")
        latitude = intent.extras?.getString("latitude")

        setAutoFill()

        btnEditLocation?.setOnClickListener { v ->
            when (v.id) {
                R.id.btn_edit_location -> {
                    editLocation(token!!, id!!, id!!)
                }
            }
        }
    }

    private fun setAutoFill() {
        etAddress!!.setText(address)
        etLongitude!!.setText(longitude)
        etLatitude!!.setText(latitude)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        longitude = intent.extras?.getString("longitude")
        latitude = intent.extras?.getString("latitude")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                googleMap?.isMyLocationEnabled = true
            }
        }
        else {
            googleMap?.isMyLocationEnabled = true
        }

        val location = LatLng(latitude!!.toDouble(), longitude!!.toDouble())
        var marker = googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .title("Your Location")
                .draggable(true)
        )
        googleMap?.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0F))
            setOnMyLocationButtonClickListener {
                marker?.remove()
                val getLocation = getLocation()
                val currentLocation = LatLng(getLocation!!.latitude, getLocation.longitude)
                marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(currentLocation)
                        .title("Your Location")
                        .draggable(true)
                )
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0F))
                val position = marker?.position

                etLongitude?.setText(position?.longitude.toString())
                etLatitude?.setText(position?.latitude.toString())

                val addresses: List<Address>
                val geocoder = Geocoder(applicationContext, Locale.getDefault())

                addresses = geocoder.getFromLocation(
                    position!!.latitude,
                    position.longitude,
                    1
                ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                if (addresses.isNotEmpty()) {
                    val address: String = addresses[0].getAddressLine(0)
                    etAddress?.setText(address)
                } else {
                    etAddress?.setText("")
                    Toast.makeText(
                        this@EditLocationActivity, "Invalid location. Please select a valid location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(marker: Marker) {
                    Toast.makeText(
                        this@EditLocationActivity, "Dragging Start",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onMarkerDragEnd(marker: Marker) {
                    val pos: LatLng = marker.position

                    etLongitude?.setText(pos.longitude.toString())
                    etLatitude?.setText(pos.latitude.toString())

                    val addresses: List<Address>
                    val geocoder = Geocoder(applicationContext, Locale.getDefault())
                    addresses = geocoder.getFromLocation(
                        pos.latitude,
                        pos.longitude,
                        1
                    ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    if (addresses.isNotEmpty()) {
                        val address: String = addresses[0].getAddressLine(0)
                        etAddress?.setText(address)
                    } else {
                        etAddress?.setText("")
                        Toast.makeText(
                            this@EditLocationActivity, "Invalid location. Please select a valid location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onMarkerDrag(marker: Marker) {
                    //
                }
            })
        }
    }

    private fun getLocation(): Location? {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationGPS: Location? =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val locationNet: Location? =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@EditLocationActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_ACCESS_REQUEST_CODE
            )
        }

        var gpsLocationTime: Long = 0
        if (null != locationGPS) {
            gpsLocationTime = locationGPS.time
        }

        var netLocationTime: Long = 0

        if (null != locationNet) {
            netLocationTime = locationNet.time
        }

        return if (0 < gpsLocationTime - netLocationTime) {
            locationGPS
        } else {
            locationNet
        }
    }

    private fun editLocation(token: String, id: Int, locationId: Int) {
        address = etAddress?.text.toString()
        longitude = etLongitude?.text.toString()
        latitude = etLatitude?.text.toString()

        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val locationCall: Call<ResponseBody?>? =
            apiInterface.editLocation(token, id, locationId,
                address!!, longitude, latitude)
        locationCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Toast.makeText(
                        this@EditLocationActivity,
                        "Lokasi berhasil diupdate!",
                        Toast.LENGTH_SHORT
                    ).show()
                val intent =
                    Intent(this@EditLocationActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                progressDialog.hideLoading()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
                Toast.makeText(this@EditLocationActivity,
                    "Gagal menyimpan data.",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}