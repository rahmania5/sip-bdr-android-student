package com.rahmania.sip_bdr_student.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rahmania.sip_bdr_student.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MeetingAdapter : RecyclerView.Adapter<MeetingAdapter.ListViewHolder>() {
    private var meetingData = JSONArray()
    private var listener: OnItemClickListener? = null

    fun MeetingAdapter(listener: OnItemClickListener?) {
        this.listener = listener
    }

    fun setData(items: JSONArray) {
        meetingData = items
        notifyDataSetChanged()
    }

    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvNumber: TextView = itemView.findViewById<View>(R.id.tv_item_number) as TextView
        val tvDate: TextView = itemView.findViewById<View>(R.id.tv_item_date) as TextView
        fun bind(item: JSONObject, listener: OnItemClickListener?) {
            itemView.setOnClickListener {
                try {
                    listener?.onItemClick(item)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ListViewHolder {
        val mView: View = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.meetings_list,
            viewGroup, false
        )
        return ListViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return meetingData.length()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        try {
            val date = meetingData.getJSONObject(position).getString("date")
            val outputDate = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val inputDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dateFormat = inputDate.parse(date)

            holder.tvNumber.text = ("Pertemuan ke-" + meetingData.getJSONObject(position).getString("number_of_meeting"))
            holder.tvDate.text = outputDate.format(dateFormat)
            holder.bind(meetingData.getJSONObject(position), listener)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    interface OnItemClickListener {
        @Throws(JSONException::class)
        fun onItemClick(item: JSONObject)
    }
}