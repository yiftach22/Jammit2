package com.jammit.network

import com.jammit.BuildConfig
import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val baseUrl: String = run {
        val url = BuildConfig.BACKEND_BASE_URL
        if (url.startsWith("https://REPLACE_") || url.isBlank()) {
            // Fallback for local dev: emulator -> 10.0.2.2, device -> use a real BACKEND_BASE_URL in build.gradle
            if (isEmulator()) "http://10.0.2.2:3000/" else "http://10.0.2.2:3000/" // local fallback; set BACKEND_BASE_URL for production
        } else {
            url.trimEnd('/') + "/"
        }
    }

    private fun isEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val brand = Build.BRAND
        val device = Build.DEVICE
        val product = Build.PRODUCT
        val hardware = Build.HARDWARE
        val board = Build.BOARD
        return fingerprint.startsWith("generic") ||
            fingerprint.lowercase().contains("vbox") ||
            fingerprint.lowercase().contains("test-keys") ||
            fingerprint.lowercase().contains("emulator") ||
            model.contains("google_sdk") ||
            model.contains("Emulator") ||
            model.contains("Android SDK built for x86") ||
            model.contains("sdk_gphone") ||
            manufacturer.contains("Genymotion") ||
            (manufacturer.contains("Google") && brand.contains("google_sdk")) ||
            (brand.startsWith("generic") && device.startsWith("generic")) ||
            product == "google_sdk" ||
            product.contains("sdk_gphone") ||
            product.contains("sdk_google") ||
            hardware.contains("goldfish") ||
            hardware.contains("ranchu") ||
            hardware.contains("vbox") ||
            hardware.contains("emulator") ||
            board.contains("goldfish")
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)

    /** Base URL for Socket.IO (no trailing slash). Use with path "/ws". */
    fun getSocketBaseUrl(): String = baseUrl.trimEnd('/')
}
