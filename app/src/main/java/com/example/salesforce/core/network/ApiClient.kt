
package com.example.salesforce.core.network

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

object ApiClient {

    // Replace this with your actual backend host.
    // Keep the trailing slash.
    private const val BASE_URL = "https://52.64.71.168/"
    //private const val BASE_URL = "http://192.168.18.195:8000/"


    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    private val okHttpClient = OkHttpClient.Builder()
        .build()

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory(
                    "application/json".toMediaType()
                )
            )
            .build()
            .create(ApiService::class.java)
    }
}
