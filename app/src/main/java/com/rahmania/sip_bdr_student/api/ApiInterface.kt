package com.rahmania.sip_bdr_student.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {
    @FormUrlEncoded
    @POST("login")
    fun loginResponse(
        @Field("username") username: String?,
        @Field("password") password: String?
    ): Call<ResponseBody?>?

    @POST("logout")
    fun logout(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @GET("islogin")
    fun isLogin(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @GET("user")
    fun getDetails(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @FormUrlEncoded
    @PUT("changepassword")
    fun changePassword(
        @Header("Authorization") authToken: String?,
        @Field("old_password") oldPassword: String?,
        @Field("password") newPassword: String?,
        @Field("password_confirmation") passwordConfirmation: String?
    ): Call<ResponseBody?>?

    @GET("krs")
    fun getClassroomSchedule(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @GET("studentlocation")
    fun getStudentSubmission(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @FormUrlEncoded
    @POST("studentlocation")
    fun addLocation(
        @Header("Authorization") authToken: String?,
        @Field("address") address: String,
        @Field("longitude") longitude: String?,
        @Field("latitude") latitude: String?
    ): Call<ResponseBody?>?

    @FormUrlEncoded
    @PUT("editlocation/{id}")
    fun editLocation(
        @Header("Authorization") authToken: String?,
        @Path("id") id: Int?,
        @Field("id") locationId: Int?,
        @Field("address") address: String,
        @Field("longitude") longitude: String?,
        @Field("latitude") latitude: String?
    ): Call<ResponseBody?>?

    @DELETE("studentlocation/{id}")
    fun deleteLocation(
        @Header("Authorization") authToken: String?,
        @Path("id") id: Int?
    ): Call<ResponseBody?>?

    @GET("attendance/{id}")
    fun getAttendanceHistory(
        @Header("Authorization") authToken: String?,
        @Path("id") krsId: Int?
    ): Call<ResponseBody?>?

    @FormUrlEncoded
    @PATCH("studentattendance/{id}")
    fun confirmAttendance(
        @Header("Authorization") authToken: String?,
        @Path("id") id: Int?,
        @Field("presence_status") presenceStatus: String?
    ): Call<ResponseBody?>?

    @PATCH("attendance/{id}")
    fun updateAttendance(
        @Header("Authorization") authToken: String?,
        @Path("id") attendanceId: Int?
    ): Call<ResponseBody?>?
}