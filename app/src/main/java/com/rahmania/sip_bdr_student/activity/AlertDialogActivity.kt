package com.rahmania.sip_bdr_student.activity

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.api.ApiClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import com.rahmania.sip_bdr_student.notification.NotificationUtil
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)
class AlertDialogActivity: Activity() {
    private var token: String? = null
    private var meetingId: String? = null
    private var startTime: String? = null
    private var absence = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        meetingId = intent.extras?.getString("meetingId")
        startTime = intent.extras?.getString("startTime")
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
                updateStatus(meetingId, token, "Hadir")
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
                    updateStatus(meetingId, token, "Absen")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            finish()
        }
    }

    private fun updateStatus(
        meetingId: String?,
        token: String?,
        status: String?
    ) {
        var needsReview = 0
        if (startTime?.let { isLate(it) } == true && status == "Hadir") {
            needsReview = 2
        }
        val apiInterface: ApiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val attendanceResponse: Call<ResponseBody?>? = apiInterface.updateAttendance(
            token,
            meetingId,
            status,
            needsReview
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
                        val meetingID = attendance.getString("meeting_id")

                        if (attendanceStatus == "Hadir") {
                            NotificationUtil(applicationContext).showNotification(
                                "Status Kehadiran",
                                "Berhasil mengisi daftar hadir!",
                                meetingID
                            )
                        } else if (attendanceStatus == "Absen") {
                            NotificationUtil(applicationContext).showNotification(
                                "Status Kehadiran",
                                "Anda tidak hadir pada pertemuan ini.",
                                meetingID
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
                NotificationUtil(applicationContext).showNotification(
                    "Status Kehadiran", "Anda tidak hadir pada pertemuan ini."
                )
           }
        })
    }

    private fun isLate(
        startTimeString: String
    ): Boolean {
        val sdfTime = SimpleDateFormat("HH:mm", Locale.US)
        val timeNow = sdfTime.parse(
            sdfTime.format(
                Calendar.getInstance().time
            )
        )
        val startTime = sdfTime.parse(startTimeString)
        val isLate = timeNow.time > (startTime.time + 15 * 60 * 1000)
        if (isLate) {
            return true
        }
        return false
    }
}