package com.rahmania.sip_bdr_student.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.api.ApiClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import com.rahmania.sip_bdr_student.helper.CustomProgressDialog
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.viewModel.AccountViewModel
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ChangePasswordActivity : AppCompatActivity() {
    private var oldPassword: String? = null
    private var newPassword: String? = null
    private var passwordConfirmation: String? = null
    private lateinit var apiInterface: ApiInterface
    private var sessionManager: SharedPreferences? = null
    lateinit var progressDialog: CustomProgressDialog
    private var accountVM: AccountViewModel? = null

    private var tvName: TextView? = null
    private var tvNim: TextView? = null
    private var etOldPassword: EditText? = null
    private var etNewPassword: EditText? = null
    private var etPasswordConfirmation: EditText? = null
    private var btnChangePassword: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        progressDialog = CustomProgressDialog(this)

        tvName = findViewById(R.id.tv_name)
        tvNim = findViewById(R.id.tv_nim)
        etOldPassword = findViewById(R.id.et_old_password)
        etNewPassword = findViewById(R.id.et_new_password)
        etPasswordConfirmation = findViewById(R.id.et_password_confirmation)

        btnChangePassword = findViewById(R.id.btn_change_pass)

        sessionManager = SharedPreferences.SessionManager(this)
        sessionManager!!.isLogin()

        val user = sessionManager!!.getUserDetail()
        val token = user[sessionManager!!.TOKEN]

        accountVM =
            ViewModelProvider(
                this,
                ViewModelProvider.NewInstanceFactory()
            ).get(AccountViewModel::class.java)

        accountVM!!.setProfile(token)
        accountVM!!.getProfile().observe(
            this,
            Observer<HashMap<String, String>> { stringStringHashMap ->
                if (stringStringHashMap.size > 0) {
                    tvName?.text = stringStringHashMap[accountVM!!.name]
                    tvNim?.text = stringStringHashMap[accountVM!!.nim]
                }
            })

        btnChangePassword?.setOnClickListener { v ->
            when (v.id) {
                R.id.btn_change_pass -> {
                    oldPassword = etOldPassword?.text.toString()
                    newPassword = etNewPassword?.text.toString()
                    passwordConfirmation = etPasswordConfirmation?.text.toString()
                    changePassword(token!!, oldPassword!!, newPassword!!, passwordConfirmation!!)
                }
            }
        }
    }

    private fun changePassword(
        token: String,
        oldPassword: String,
        newPassword: String,
        passwordConfirmation: String
    ) {
        progressDialog.showLoading()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val changePassCall: Call<ResponseBody?>? =
            apiInterface.changePassword(token, oldPassword, newPassword, passwordConfirmation)
        changePassCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.code() == 200) {
                    Toast.makeText(
                        this@ChangePasswordActivity,
                        "Password berhasil diubah!",
                        Toast.LENGTH_SHORT
                    ).show()
                    val i = Intent(
                        this@ChangePasswordActivity, MainActivity::class.java
                    )
                    startActivity(i)
                } else {
                    val error = JSONObject(response.errorBody()?.string())
                    passwordValidation(error)
                }
                progressDialog.hideLoading()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
            }
        })
    }

    private fun passwordValidation(error: JSONObject) {
        if(error.getJSONObject("message").has("old_password")){
            val oldPassError = error.getJSONObject("message").getJSONArray("old_password")
            for (i in 0 until oldPassError.length()) {
                etOldPassword?.error = oldPassError.getString(i).toString()
            }
        }

        if(error.getJSONObject("message").has("password")){
            val newPassError = error.getJSONObject("message").getJSONArray("password")
            for (i in 0 until newPassError.length()) {
                etNewPassword?.error = newPassError.getString(i).toString()
            }
        }

        if(error.getJSONObject("message").has("password_confirmation")){
            val passConfirmError = error.getJSONObject("message").getJSONArray("password_confirmation")
            for (i in 0 until passConfirmError.length()) {
                etPasswordConfirmation?.error = passConfirmError.getString(i).toString()
            }
        }
    }
}