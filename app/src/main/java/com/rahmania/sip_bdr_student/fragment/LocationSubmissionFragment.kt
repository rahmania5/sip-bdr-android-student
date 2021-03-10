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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.activity.AddLocationActivity
import com.rahmania.sip_bdr_student.activity.LocationDetailActivity
import com.rahmania.sip_bdr_student.adapter.LocationSubmissionAdapter
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.helper.SharedPreferences.SessionManager
import com.rahmania.sip_bdr_student.viewModel.LocationSubmissionViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class LocationSubmissionFragment : Fragment() {

    private var rv: RecyclerView? = null
    private var tvNoLocationSubmission: TextView? = null
    private var locationVM: LocationSubmissionViewModel? = null
    private var sessionManager: SharedPreferences? = null

    private var fabAddLocation: FloatingActionButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_submission, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)
        rv = v.findViewById<View>(R.id.rv_locations) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(activity)
        tvNoLocationSubmission = v.findViewById<View>(R.id.tv_no_locationSubmission) as TextView
        fabAddLocation = v.findViewById<View>(R.id.fab_addLocation) as FloatingActionButton
        val locationAdapter = activity?.let { LocationSubmissionAdapter(it) }
        locationAdapter?.LocationSubmissionAdapter(object: LocationSubmissionAdapter.OnItemClickListener {
            @Throws(JSONException::class)
            override fun onItemClick(item: JSONObject) {
                val intent = Intent(activity, LocationDetailActivity::class.java)
                intent.putExtra("id", item.getInt("id"))
                intent.putExtra("name", item.getString("name"))
                intent.putExtra("nim", item.getString("nim"))
                intent.putExtra("address", item.getString("address"))
                intent.putExtra("longitude", item.getString("longitude"))
                intent.putExtra("latitude", item.getString("latitude"))
                intent.putExtra("submissionStatus", item.getString("submission_status"))
                startActivity(intent)
            }
        })
        locationAdapter?.notifyDataSetChanged()
        rv!!.adapter = locationAdapter
        sessionManager = SessionManager(context)
        sessionManager!!.isLogin()
        val user = sessionManager!!.getUserDetail()
        val token = user!![sessionManager!!.TOKEN]
        locationVM =
            ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()).get(
                LocationSubmissionViewModel::class.java)
        locationVM!!.setStudentSubmission(token)
        locationVM!!.getStudentSubmission()?.observe(viewLifecycleOwner,
            Observer<JSONArray?> { data ->
                tvNoLocationSubmission!!.visibility = View.VISIBLE
                if (data != null && data.length() > 0) {
                    locationAdapter?.setData(data)
                    tvNoLocationSubmission!!.visibility = View.GONE
                }
            })

        fabAddLocation?.setOnClickListener { view ->
            when (view.id) {
                R.id.fab_addLocation -> {
                    val intent = Intent(activity, AddLocationActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}