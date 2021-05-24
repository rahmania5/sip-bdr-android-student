package com.rahmania.sip_bdr_student.activity

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.api.ApiClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import com.rahmania.sip_bdr_student.helper.NotificationHelper
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*

class AlertDialogActivity: Activity() {
    private var notificationHelper: NotificationHelper? = null
    private var token: String? = null
    private var krsId: Int? = null
    private var meetingId: Int? = null
    private var absence = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationHelper = NotificationHelper(this)
        krsId = intent.extras?.getInt("krsId")
        meetingId = intent.extras?.getInt("meetingId")
        token = intent.extras?.getString("token")

        val builder: AlertDialog.Builder = AlertDialog.Builder(
            ContextThemeWrapper(
                this,
                R.style.AlertDialog
            )
        )
        builder.setTitle("Konfirmasi Kehadiran")
        builder.setIcon(R.drawable.ic_checked)
        builder.setMessage("Konfirmasi kehadiran Anda?")
        builder.setPositiveButton(
            "Ya"
        ) { _, _ ->
            absence = false
            try {
                updateStatus(krsId, meetingId, token, "Hadir")
//                val returnIntent = Intent()
//                returnIntent.putExtra("absence", absence)
//                setResult(RESULT_OK, returnIntent)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        val dialog: AlertDialog? = builder.create()
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()

        val t = Timer()
        t.schedule(object : TimerTask() {
            override fun run() {
                dialog?.dismiss()
                t.cancel()
            }
        }, 60000)

        dialog?.setOnDismissListener {
            if (absence) {
                try {
                    updateStatus(krsId, meetingId, token, "Absen")
//                    geofenceActivity?.updateStatus(krsId, meetingId, token, "Absen")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            finish()
        }
    }

    private fun updateStatus(
        krsId: Int?,
        meetingId: Int?,
        token: String?,
        status: String?
    ) {
        val apiInterface: ApiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val attendanceResponse: Call<ResponseBody?>? = apiInterface.updateAttendance(
            token,
            krsId,
            meetingId,
            status
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
                    Log.d("Attendance Data", attendance.toString())
                    if (attendance.length() != 0) {
                        val attendanceStatus = attendance.getString("presence_status")

                        if (attendanceStatus == "Hadir") {
                            notificationHelper!!.sendHighPriorityNotification(
                                "Status Kehadiran", "Berhasil mengisi daftar hadir!",
                                MainActivity::class.java
                            )
                        } else if (attendanceStatus == "Absen") {
                            notificationHelper!!.sendHighPriorityNotification(
                                "Status Kehadiran", "Anda tidak hadir pada pertemuan ini.",
                                MainActivity::class.java
                            )
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
                notificationHelper!!.sendHighPriorityNotification(
                    "Status Kehadiran", "Gagal mengisi daftar hadir.",
                    MainActivity::class.java
                )
            }
        })
    }
}