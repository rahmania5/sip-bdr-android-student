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
class ClassroomAdapter : RecyclerView.Adapter<ClassroomAdapter.ListViewHolder>() {
    private var classroomData = JSONArray()
    private var listener: OnItemClickListener? = null

    fun ClassroomAdapter(listener: OnItemClickListener?) {
        this.listener = listener
    }

    fun setData(items: JSONArray) {
        classroomData = items
        notifyDataSetChanged()
    }

    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvClassroom: TextView = itemView.findViewById<View>(R.id.tv_item_classroom) as TextView
        val tvDay: TextView = itemView.findViewById<View>(R.id.tv_item_day) as TextView
        val tvTime: TextView = itemView.findViewById<View>(R.id.tv_item_time) as TextView
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
            R.layout.classrooms_list,
            viewGroup, false
        )
        return ListViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return classroomData.length()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        try {
            val startTime = classroomData.getJSONObject(position).getString("start_time")
            val finishTime = classroomData.getJSONObject(position).getString("finish_time")

            val outputTime = SimpleDateFormat("HH:mm", Locale.US)
            val inputTime = SimpleDateFormat("HH:mm:ss", Locale.US)
            val startTimeFormat = inputTime.parse(startTime)
            val finishTimeFormat = inputTime.parse(finishTime)

            holder.tvClassroom.text = (classroomData.getJSONObject(position).getString("course_name").capitalizeFirstLetter() + " "
                    + classroomData.getJSONObject(position).getString("classroom_code"))
            holder.tvDay.text = classroomData.getJSONObject(position).getString("scheduled_day")
            holder.tvTime.text = (outputTime.format(startTimeFormat) + " - "
                    + outputTime.format(finishTimeFormat))
            holder.bind(classroomData.getJSONObject(position), listener)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    interface OnItemClickListener {
        @Throws(JSONException::class)
        fun onItemClick(item: JSONObject)
    }

    private fun String.capitalizeFirstLetter() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()

}