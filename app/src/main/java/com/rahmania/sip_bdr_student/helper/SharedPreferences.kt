package com.rahmania.sip_bdr_student.helper

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import com.rahmania.sip_bdr_student.activity.LoginActivity
import com.rahmania.sip_bdr_student.api.ApiClient.getClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object SharedPreferences {
    private var context_: Context? = null
    private var sharedPreferences: android.content.SharedPreferences? = null
    private var editor: android.content.SharedPreferences.Editor? = null

    val IS_LOGIN = "isLogin"
    val TOKEN = "access_token"
    val NAME = "name"
    val NIM = "nim"

    fun SessionManager(context: Context?): SharedPreferences {
        context_ = context
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        editor = sharedPreferences!!.edit()
        return SharedPreferences
    }

    fun createLoginSession(
        token: String,
        name: String?,
        nim: String?
    ) {
        editor!!.putBoolean(
            IS_LOGIN, true)
        editor!!.putString(
            TOKEN, "Bearer $token")
        editor!!.putString(
            NAME, name)
        editor!!.putString(
            NIM, nim)
        editor!!.commit()
    }

    // Storing session
    fun getUserDetail(): HashMap<String, String?>? {
        val user: HashMap<String, String?> = HashMap()
        user[TOKEN] = sharedPreferences!!.getString(
            TOKEN, null)
        user[NAME] = sharedPreferences!!.getString(
            NAME, null)
        user[NIM] = sharedPreferences!!.getString(
            NIM, null)
        return user
    }

    fun logoutSession() {
        editor!!.clear()
        editor!!.commit()
    }

    fun checkToken(): Boolean {
        return sharedPreferences!!.getBoolean(
            IS_LOGIN, false)
    }

    fun isLogin() {
        val token = sharedPreferences!!.getString(
            TOKEN, null)
        if (token != "") {
            val apiInterface: ApiInterface =
                getClient()!!.create(ApiInterface::class.java)
            val isLoginCall: Call<ResponseBody?>? = apiInterface.isLogin(token)
            isLoginCall?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>?,
                    response: Response<ResponseBody?>
                ) {
                    if (response.code() != 200) {
                        val intent = Intent(context_, LoginActivity::class.java)
                        logoutSession()
                        context_!!.startActivity(intent)
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                    Log.e("isLogin", t.message!!)
                }
            })
        }
    }

}