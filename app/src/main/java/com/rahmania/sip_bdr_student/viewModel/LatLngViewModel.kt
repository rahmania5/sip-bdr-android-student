package com.rahmania.sip_bdr_student.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rahmania.sip_bdr_student.api.ApiClient.getClient
import com.rahmania.sip_bdr_student.api.ApiInterface
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class LatLngViewModel : ViewModel() {
    private lateinit var apiInterface: ApiInterface
    private val latLng = MutableLiveData<HashMap<String, String>>()
    val lat = "latitude"
    val lng = "longitude"

    fun setLocation(token: String?) {
        val userLocation: HashMap<String, String> = HashMap()
        this.apiInterface = getClient()!!.create(ApiInterface::class.java)
        apiInterface.getLatLng(token)?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful) {
                    val jsonRESULTS: JSONObject?
                    try {
                        jsonRESULTS = JSONObject(response.body()!!.string())
                        val location = jsonRESULTS.getJSONObject("location")
                        if (location.length() != 0) {
                            val latitude =
                                location.getJSONObject("latlng").getString("latitude")
                            val longitude =
                                location.getJSONObject("latlng").getString("longitude")

                            userLocation[lat] = latitude
                            userLocation[lng] = longitude
                            latLng.value = userLocation
                        }
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

    fun getLocation(): LiveData<HashMap<String, String>> {
        return latLng
    }

}