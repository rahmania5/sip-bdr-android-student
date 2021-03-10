package com.rahmania.sip_bdr_student.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.activity.ClassroomDetailActivity
import com.rahmania.sip_bdr_student.adapter.ClassroomAdapter
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.helper.SharedPreferences.SessionManager
import com.rahmania.sip_bdr_student.viewModel.ClassroomViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ClassroomFragment : Fragment() {

    private var rv: RecyclerView? = null
    private var tvNoClassroom: TextView? = null
    private var classroomVM: ClassroomViewModel? = null
    private var sessionManager: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_classroom, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        rv = v.findViewById<View>(R.id.rv_classrooms) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(activity)
        tvNoClassroom = v.findViewById<View>(R.id.tv_no_classroom) as TextView
        val classroomAdapter = ClassroomAdapter()
        classroomAdapter.ClassroomAdapter(object: ClassroomAdapter.OnItemClickListener {
            @Throws(JSONException::class)
            override fun onItemClick(item: JSONObject) {
                val intent = Intent(activity, ClassroomDetailActivity::class.java)
                intent.putExtra("krs", item.toString())
                startActivity(intent)
            }
        })
        classroomAdapter.notifyDataSetChanged()
        rv!!.adapter = classroomAdapter
        sessionManager = SessionManager(context)
        sessionManager!!.isLogin()
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]
        classroomVM =
            ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()).get(
                ClassroomViewModel::class.java)
        classroomVM!!.setClassroomSchedule(token)
        classroomVM!!.getClassroomSchedule()?.observe(viewLifecycleOwner,
            Observer<JSONArray?> { data ->
                tvNoClassroom!!.visibility = View.VISIBLE
                if (data != null && data.length() > 0) {
                    classroomAdapter.setData(data)
                    tvNoClassroom!!.visibility = View.GONE
                }
            })
    }
}