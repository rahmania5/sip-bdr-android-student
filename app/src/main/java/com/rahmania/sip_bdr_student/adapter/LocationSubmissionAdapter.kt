package com.rahmania.sip_bdr_student.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.rahmania.sip_bdr_student.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class LocationSubmissionAdapter(val context: Context) : RecyclerView.Adapter<LocationSubmissionAdapter.ListViewHolder>() {
    private var locationData = JSONArray()
    private var listener: OnItemClickListener? = null

    fun LocationSubmissionAdapter(listener: OnItemClickListener?) {
        this.listener = listener
    }

    fun setData(items: JSONArray) {
        locationData = items
        notifyDataSetChanged()
    }

    inner class ListViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val tvAddress: TextView = itemView.findViewById<View>(R.id.tv_item_address) as TextView
        var tvStatus: TextView = itemView.findViewById<View>(R.id.tv_item_submission_status) as TextView
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
            R.layout.locations_list,
            viewGroup, false
        )
        return ListViewHolder(mView)
    }

    override fun getItemCount(): Int {
        return locationData.length()
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        try {
            holder.tvAddress.text = locationData.getJSONObject(position).getString("address")
            holder.tvStatus.text = locationData.getJSONObject(position).getString("submission_status")
            if (holder.tvStatus.text == "Disetujui") {
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorSubTitle))
            } else if (holder.tvStatus.text == "Ditolak") {
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
            holder.bind(locationData.getJSONObject(position), listener)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    interface OnItemClickListener {
        @Throws(JSONException::class)
        fun onItemClick(item: JSONObject)
    }
}