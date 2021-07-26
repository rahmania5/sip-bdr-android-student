@file:Suppress("DEPRECATION")

package com.rahmania.sip_bdr_student.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import com.rahmania.sip_bdr_student.R
import com.rahmania.sip_bdr_student.api.ApiClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import com.rahmania.sip_bdr_student.helper.CustomProgressDialog
import com.rahmania.sip_bdr_student.helper.SharedPreferences
import com.rahmania.sip_bdr_student.helper.SharedPreferences.SessionManager
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    private var username: String? = null
    private var password: String? = null
    private var etUsername: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private lateinit var apiInterface: ApiInterface
    private lateinit var sessionManager: SharedPreferences
    lateinit var progressDialog: CustomProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        progressDialog = CustomProgressDialog(this)

        sessionManager = SessionManager(this@LoginActivity)
        if (sessionManager.checkToken()) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        etUsername = findViewById(R.id.et_username)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)

        btnLogin?.setOnClickListener { v: View ->
            when (v.id) {
                R.id.btn_login -> {
                    username = etUsername?.text.toString()
                    password = etPassword?.text.toString()
                    login(username!!, password!!)
                }
            }
        }
    }

    private fun login(username: String, password: String) {
        progressDialog.showLoading()
        val deviceId = FirebaseInstanceId.getInstance().token.toString()
        apiInterface = ApiClient.getClient()!!.create(ApiInterface::class.java)
        val loginCall: Call<ResponseBody?>? = apiInterface.loginResponse(username, password, deviceId)
        loginCall?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    val jsonRESULTS: JSONObject?
                    try {
                        jsonRESULTS = JSONObject(response.body()!!.string())
                        val userData = jsonRESULTS.getJSONObject("data")
                        Log.e("User Data", userData.toString())
                        if (userData.length() != 0) {
                            val token = userData.getString("access_token")
                            val name = userData.getString("name")
                            val nim = userData.getString("nim")
                            sessionManager.createLoginSession(token, name, nim)
                            Toast.makeText(this@LoginActivity, "Anda login sebagai $name", Toast.LENGTH_SHORT).show()
                            val intent =
                                Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Username atau password salah", Toast.LENGTH_SHORT).show()
                }
                progressDialog.hideLoading()
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("error data", t.message.toString())
            }
        })
    }
}