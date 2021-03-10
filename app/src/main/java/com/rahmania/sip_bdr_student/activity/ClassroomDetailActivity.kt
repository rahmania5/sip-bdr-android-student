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
import com.rahmania.sip_bdr_student.viewModel.ClassroomDetailViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ClassroomDetailActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    private var meetingVM: ClassroomDetailViewModel? = null
    private var sessionManager: SharedPreferences? = null
    private lateinit var progressDialog: CustomProgressDialog

    var id: Int? = null
    private var tvClassName: TextView? = null
    private var tvSks: TextView? = null
    private var tvDay: TextView? = null
    private var tvTime: TextView? = null
    //private var tvLecturer:TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_classroom_detail)
        progressDialog = CustomProgressDialog(this)

        rv = findViewById<View>(R.id.rv_meetings) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(this)
        tvClassName = findViewById(R.id.tv_classroomName)
        tvSks = findViewById(R.id.tv_sks)
        tvDay = findViewById(R.id.tv_day)
        tvTime = findViewById(R.id.tv_time)
        //tvLecturer = findViewById(R.id.tv_lecturer)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        setUpContent()
    }

    private fun setUpContent() {
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]

        val meetingAdapter = AttendanceAdapter()
        meetingAdapter.MeetingAdapter(object: AttendanceAdapter.OnItemClickListener {
            @Throws(JSONException::class)
            override fun onItemClick(item: JSONObject) {
                //
            }
        })
        meetingAdapter.notifyDataSetChanged()
        rv!!.adapter = meetingAdapter

        try {
            val intent = intent
            val classroomDetail = JSONObject(intent.getStringExtra("krs"))
            id = classroomDetail.getInt("id")
            val startTime = classroomDetail.getString("start_time")
            val finishTime = classroomDetail.getString("finish_time")

            val outputTime = SimpleDateFormat("HH:mm", Locale.US)
            val inputTime = SimpleDateFormat("HH:mm:ss", Locale.US)
            val startTimeFormat = inputTime.parse(startTime)
            val finishTimeFormat = inputTime.parse(finishTime)

            tvClassName!!.text = (classroomDetail.getString("course_name").capitalizeFirstLetter() + " "
                    + classroomDetail.getString("classroom_code"))

//            var lecturerName = ""
//            for (i in 0 until classroomDetail.getJSONArray("lecturers").length()) {
//                lecturerName = if (i == 0) {
//                    lecturerName + classroomDetail.getJSONArray("lecturers").getJSONObject(i)
//                        .getString("name")
//                } else {
//                    "$lecturerName, " + classroomDetail.getJSONArray("lecturers").getJSONObject(i)
//                        .getString("name")
//                }
//            }
//            tvLecturer!!.text = lecturerName
            tvSks!!.text = (classroomDetail.getString("sks") + " SKS")
            tvDay!!.text = classroomDetail.getString("scheduled_day")
            tvTime!!.text = (outputTime.format(startTimeFormat) + " - "
                    + outputTime.format(finishTimeFormat))

            meetingVM = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
                ClassroomDetailViewModel::class.java
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

    private fun String.capitalizeFirstLetter() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()

}