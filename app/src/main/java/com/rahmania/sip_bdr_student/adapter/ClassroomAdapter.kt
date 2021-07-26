package com.rahmania.sip_bdr_student.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rahmania.sip_bdr_student.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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
        val tvCourseCode: TextView = itemView.findViewById<View>(R.id.tv_item_code) as TextView
        val tvPeriod: TextView = itemView.findViewById<View>(R.id.tv_item_period) as TextView
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

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        try {
            holder.tvClassroom.text = (classroomData.getJSONObject(position).getString("course_name").capitalizeFirstLetter() + " "
                    + classroomData.getJSONObject(position).getString("classroom_code"))
            holder.tvCourseCode.text = classroomData.getJSONObject(position).getString("course_code").toUpperCase()
            holder.tvPeriod.text = classroomData.getJSONObject(position).getString("period")
            holder.bind(classroomData.getJSONObject(position), listener)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    interface OnItemClickListener {
        @Throws(JSONException::class)
        fun onItemClick(item: JSONObject)
    }

    @SuppressLint("DefaultLocale")
    private fun String.capitalizeFirstLetter() = this.split(" ").joinToString(" ") { it.capitalize() }.trimEnd()

}