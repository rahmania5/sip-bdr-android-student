package com.rahmania.sip_bdr_student.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.api.ApiClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import com.rahmania.sip_bdr_student.helper.CustomProgressDialog
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import okhttp3.ResponseBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class LocationDetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SharedPreferences? = null
    lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    var name: String? = null
    var nim: String? = null
    private var address: String? = null
    private lateinit var longitude: String
    private lateinit var latitude: String
    private var submissionStatus: String? = null
    private var tvName: TextView? = null
    private var tvNim: TextView? = null
    private var tvAddress: TextView? = null
    private var tvLongitude: TextView? = null
    private var tvLatitude: TextView? = null
    private var tvStatus: TextView? = null
    private var fabMain: FloatingActionButton? = null
    private var fabEditLocation: FloatingActionButton? = null
    private var fabDeleteLocation: FloatingActionButton? = null
    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var fabClock: Animation
    private lateinit var fabAntiClock: Animation

    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_detail)
        progressDialog = CustomProgressDialog(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        tvName = findViewById(R.id.tv_name)
        tvNim = findViewById(R.id.tv_nim)
        tvAddress = findViewById(R.id.tv_address)
        tvLongitude = findViewById(R.id.tv_longitude)
        tvLatitude = findViewById(R.id.tv_latitude)
        tvStatus = findViewById(R.id.tv_submission_status)

        fabMain = findViewById(R.id.fab)
        fabEditLocation = findViewById(R.id.fab_editLocation)
        fabDeleteLocation = findViewById(R.id.fab_deleteLocation)

        fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        fabClock = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_clock)
        fabAntiClock = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_rotate_anticlock)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

        try {
            id = intent.extras?.getInt("id")
            name = intent.extras?.getString("name")
            nim = intent.extras?.getString("nim")
            address = intent.extras?.getString("address")
            longitude = intent.extras?.getString("longitude").toString()
            latitude = intent.extras?.getString("latitude").toString()
            submissionStatus = intent.extras?.getString("submissionStatus")

            tvName!!.text = name
            tvNim!!.text = nim
            tvAddress!!.text = address
            tvLongitude!!.text = longitude
            tvLatitude!!.text = latitude
            tvStatus!!.text = submissionStatus

            if(submissionStatus != "Belum Disetujui") {
                fabMain!!.visibility = View.GONE
            }

            fabMain?.setOnClickListener {
                isOpen = if (isOpen) {
                    fabEditLocation?.startAnimation(fabClose)
                    fabDeleteLocation?.startAnimation(fabClose)
                    fabMain!!.startAnimation(fabAntiClock)
                    fabEditLocation?.isClickable = false
                    fabDeleteLocation?.isClickable = false
                    false
                } else {
                    fabEditLocation?.startAnimation(fabOpen)
                    fabDeleteLocation?.startAnimation(fabOpen)
                    fabMain!!.startAnimation(fabClock)
                    fabEditLocation?.isClickable = true
                    fabDeleteLocation?.isClickable = true
                    true
                }
            }

            fabEditLocation?.setOnClickListener { v ->
                when (v.id) {
                    R.id.fab_editLocation -> {
                        val i = Intent(this@LocationDetailActivity, EditLocationActivity::class.java)
                        i.putExtra("id", id!!)
                        i.putExtra("address", address!!)
                        i.putExtra("longitude", longitude)
                        i.putExtra("latitude", latitude)
                        startActivity(i)
                    }
                }
            }

            fabDeleteLocation?.setOnClickListener { v ->
                when (v.id) {
                    R.id.fab_deleteLocation -> {
                        val builder: AlertDialog.Builder =
                            AlertDialog.Builder(this@LocationDetailActivity)
                        builder.setTitle("Hapus Lokasi")
                        builder.setIcon(R.drawable.ic_delete)
                        builder.setMessage("Anda yakin ingin menghapus lokasi ini?")

                        builder.setPositiveButton("Ya"
                        ) { dialog, _ ->
                            deleteLocation(token!!, id!!)
                        }

                        builder.setNegativeButton("Cancel", null)
                        val dialog: AlertDialog? = builder.create()
                        dialog?.setCanceledOnTouchOutside(false)
                        dialog?.show()
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        longitude = intent.extras?.getString("longitude").toString()
        latitude = intent.extras?.getString("latitude").toString()

        googleMap?.apply {
            val location = LatLng(latitude.toDouble(), longitude.toDouble())
            addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Your Location")
            )
            moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0F))
        }
    }

    private fun deleteLocation(token: String, locationId: Int) {
        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val deleteCall: Call<ResponseBody?>? =
            apiInterface.deleteLocation(token, locationId)
        deleteCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.code() == 200) {
                    Toast.makeText(
                        this@LocationDetailActivity,
                        "Berhasil menghapus lokasi!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent =
                        Intent(this@LocationDetailActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    progressDialog.hideLoading()
                } else {
                    Toast.makeText(
                        this@LocationDetailActivity,
                        "Lokasi sudah disetujui. Gagal menghapus lokasi",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog.hideLoading()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
                Toast.makeText(this@LocationDetailActivity,
                    "Gagal menghapus lokasi",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
}