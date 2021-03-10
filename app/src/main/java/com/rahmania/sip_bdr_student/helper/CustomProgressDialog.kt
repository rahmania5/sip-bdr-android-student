package com.rahmania.sip_bdr_student.helper

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.rahmania.sip_bdr_student.R

class CustomProgressDialog(activity: Activity) {
    private val dialog: AlertDialog

    init {
        val builder = AlertDialog.Builder(activity)
        val pbLayout = LayoutInflater.from(activity).inflate(R.layout.progress_dialog, null)
        builder.setView(pbLayout)
        builder.setCancelable(true)
        this.dialog = builder.create()
    }

    fun showLoading(){
        dialog.show()
    }

    fun hideLoading(){
        dialog.dismiss()
    }
}