package com.rahmania.sip_bdr_student.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.helper.CustomProgressDialog
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.viewModel.ClassroomScheduleViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


@Suppress(
    "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)
class MeetingDetailActivity : AppCompatActivity() {
    private var sessionManager: SharedPreferences? = null
    private var scheduleVM: ClassroomScheduleViewModel? = null
    private lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var tvClassName: TextView? = null
    private var tvSks: TextView? = null
    private var tvSchedule: TextView? = null
    private var tvStatus: TextView? = null
    private var tvNumber: TextView? = null
    private var tvDate: TextView? = null
    private var tvTime: TextView? = null
    private var tvTopic: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_detail)
        progressDialog = CustomProgressDialog(this)

        tvStatus = findViewById(R.id.tv_presence_status)
        tvClassName = findViewById(R.id.tv_classroomName)
        tvSks = findViewById(R.id.tv_sks)
        tvSchedule = findViewById(R.id.tv_schedule)
        tvNumber = findViewById(R.id.tv_meetingNumber)
        tvDate = findViewById(R.id.tv_date)
        tvTime = findViewById(R.id.tv_time)
        tvTopic = findViewById(R.id.tv_topic)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user[sessionManager!!.TOKEN]

        try {
            val intent = intent
            val classroomId = intent.extras?.getInt("classroomId")
            val meetingDetail = JSONObject(intent.getStringExtra("meeting"))
            id = meetingDetail.getInt("id")
            val meetingNumber = meetingDetail.getString("number_of_meeting")
            val date = meetingDetail.getString("date")
            val startTime = meetingDetail.getString("start_time")
            val finishTime = meetingDetail.getString("finish_time")

            val outputDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val inputDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dateFormat = inputDate.parse(date)
            val outputTime = SimpleDateFormat("HH:mm", Locale.US)
            val inputTime = SimpleDateFormat("HH:mm", Locale.US)
            val startTimeFormat = inputTime.parse(startTime)
            val finishTimeFormat = inputTime.parse(finishTime)

            tvClassName!!.text = intent.extras?.getString("className")
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
                            val classStartTime = data.getJSONObject(i).getString("start_time")
                            val classFinishTime = data.getJSONObject(i).getString("finish_time")

                            val classStartTimeFormat = inputTime.parse(classStartTime)
                            val classFinishTimeFormat = inputTime.parse(classFinishTime)

                            schedules = if (i == 0) {
                                schedules + (day + " | " + outputTime.format(classStartTimeFormat) + " - "
                                        + outputTime.format(classFinishTimeFormat))
                            } else {
                                "$schedules\n" + ("$day | " + outputTime.format(
                                    classStartTimeFormat
                                ) + " - "
                                        + outputTime.format(classFinishTimeFormat))
                            }
                        }
                        tvSchedule!!.text = schedules
                    }
                })

            tvNumber!!.text = ("Pertemuan ke-$meetingNumber")
            tvDate!!.text = outputDate.format(dateFormat)
            tvTime!!.text = (outputTime.format(startTimeFormat) + " - "
                    + outputTime.format(finishTimeFormat))
            tvTopic!!.text = meetingDetail.getString("topic")
            tvStatus!!.text = meetingDetail.getString("presence_status")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
