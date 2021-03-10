package com.rahmania.sip_bdr_student.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {
    private const val BASE_URL = "http://192.168.43.254:8000/api/"

    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit? {
        val interceptor = HttpLoggingInterceptor()
        interceptor.apply { interceptor.level = HttpLoggingInterceptor.Level.BODY }
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(5, TimeUnit.MINUTES) // connect timeout
            .writeTimeout(5, TimeUnit.MINUTES) // write timeout
            .readTimeout(5, TimeUnit.MINUTES) // read timeout

        val client = builder.addInterceptor(interceptor).build()

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        return retrofit
    }
}