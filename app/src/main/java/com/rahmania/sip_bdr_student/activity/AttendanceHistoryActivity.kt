package com.rahmania.sip_bdr_student.activity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.adapter.AttendanceAdapter
import com.rahmania.sip_bdr_student.helper.CustomProgressDialog
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.viewModel.AttendanceHistoryViewModel
import com.rahmania.sip_bdr_student.viewModel.ClassroomScheduleViewModel
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)
class AttendanceHistoryActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    private var meetingVM: AttendanceHistoryViewModel? = null
    private var scheduleVM: ClassroomScheduleViewModel? = null
    private var sessionManager: SharedPreferences? = null
    private lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    var token: String? = null
    private var tvClassName: TextView? = null
    private var tvCourseCode: TextView? = null
    private var tvSks: TextView? = null
    private var tvSchedule: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_history)
        progressDialog = CustomProgressDialog(this)

        rv = findViewById<View>(R.id.rv_attendances) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(this)
        tvClassName = findViewById(R.id.tv_classroomName)
        tvCourseCode = findViewById(R.id.tv_code)
        tvSks = findViewById(R.id.tv_sks)
        tvSchedule = findViewById(R.id.tv_schedule)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        token = user[sessionManager!!.TOKEN]

        val meetingAdapter = AttendanceAdapter(this)
        meetingAdapter.notifyDataSetChanged()
        rv!!.adapter = meetingAdapter

        try {
            id = intent.extras?.getInt("id")
            val classroomId = intent.extras?.getInt("classroomId")
            tvClassName!!.text = intent.extras?.getString("className")
            tvCourseCode!!.text = intent.extras?.getString("courseCode")
            tvSks!!.text = (intent.extras?.getString("sks") + " SKS")

            scheduleVM = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
                .get(ClassroomScheduleViewModel::class.java)
            scheduleVM!!.setClassroomSchedule(token, classroomId!!)
            scheduleVM!!.getClassroomSchedule().observe(this,
                Observer<JSONArray?> { data ->
                    if (data != null && data.length() > 0) {
                        var schedules = ""
                        for (i in 0 until data.length()) {
                            val day = data.getJSONObject(i).getString("scheduled_day")
                            val startTime = data.getJSONObject(i).getString("start_time")
                            val finishTime = data.getJSONObject(i).getString("finish_time")

                            val outputTime = SimpleDateFormat("HH:mm", Locale.US)
                            val inputTime = SimpleDateFormat("HH:mm:ss", Locale.US)
                            val startTimeFormat = inputTime.parse(startTime)
                            val finishTimeFormat = inputTime.parse(finishTime)

                            schedules = if (i == 0) {
                                schedules + (day + " | " + outputTime.format(startTimeFormat) + " - "
                                        + outputTime.format(finishTimeFormat))
                            } else {
                                "$schedules\n" + (day + " | " + outputTime.format(startTimeFormat) + " - "
                                        + outputTime.format(finishTimeFormat))
                            }
                        }
                        tvSchedule!!.text = schedules
                    }
                })

            meetingVM = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
                AttendanceHistoryViewModel::class.java
            )
            progressDialog.showLoading()
            meetingVM!!.setMeetings(token, id)
            meetingVM!!.getMeetings()?.observe(this,
                Observer<JSONArray?> { data ->
                    if (data != null) {
                        meetingAdapter.setData(data)
                        progressDialog.hideLoading()
                    }
                })
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}