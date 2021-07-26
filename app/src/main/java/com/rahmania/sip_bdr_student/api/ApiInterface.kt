package com.rahmania.sip_bdr_student.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {
    @FormUrlEncoded
    @POST("login")
    fun loginResponse(
        @Field("username") username: String?,
        @Field("password") password: String?,
        @Field("device_id") deviceId: String?
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
    fun getClassroomList(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @GET("schedules/{classroom_id}")
    fun getClassroomSchedule(
        @Header("Authorization") authToken: String?,
        @Path("classroom_id") classroomId: Int?
    ): Call<ResponseBody?>?

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

    @GET("latlng")
    fun getLatLng(@Header("Authorization") authToken: String?): Call<ResponseBody?>?

    @GET("attendance/{id}")
    fun getAttendanceHistory(
        @Header("Authorization") authToken: String?,
        @Path("id") krsId: Int?
    ): Call<ResponseBody?>?

    @FormUrlEncoded
    @POST("studentattendance")
    fun updateAttendance(
        @Header("Authorization") authToken: String?,
        @Field("meeting_id") meetingId: String?,
        @Field("presence_status") attendanceStatus: String?,
        @Field("needs_review") needsReview: Int?
    ): Call<ResponseBody?>?

    @FormUrlEncoded
    @PATCH("needsreview/{meeting_id}")
    fun updateReviewStatus(
        @Header("Authorization") authToken: String?,
        @Path("meeting_id") meetingId: String?,
        @Field("needs_review") needsReview: Int?
    ): Call<ResponseBody?>?
}