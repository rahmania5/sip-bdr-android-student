package com.rahmania.sip_bdr_student.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rahmania.sip_bdr_student.api.ApiClient.getClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class LocationSubmissionViewModel : ViewModel() {
    private lateinit var apiInterface: ApiInterface
    private val locationSubmission = MutableLiveData<JSONArray>()

    fun setStudentSubmission(token: String?) {
        this.apiInterface = getClient()!!.create(ApiInterface::class.java)
        apiInterface.getStudentSubmission(token)?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful) {
                    val jsonRESULTS: JSONObject?
                    try {
                        jsonRESULTS = JSONObject(response.body()!!.string())
                        val submissions = jsonRESULTS.getJSONArray("studentlocation")
                        locationSubmission.postValue(submissions)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                Log.e("Error by view model", t.message.toString())
            }
        })
    }

    fun getStudentSubmission(): LiveData<JSONArray>? {
        return locationSubmission
    }
}