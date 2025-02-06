package com.example.xone.controller

import android.util.Log
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

class LoginController {
    private val client = OkHttpClient()

    fun sendOtp(email: String, callback: (String, Boolean) -> Unit) {
        val url = "http://172.19.2.240:5000/send-otp"
        val jsonBody = JSONObject().apply {
            put("email", email)
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LoginController", "Network Error: ${e.message}")
                callback("Network error. Please try again.", true)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback("Server error. Try again later.", true)
                        return
                    }

                    val responseData = response.body?.string()
                    val jsonResponse = responseData?.let { JSONObject(it) }

                    val message = jsonResponse?.optString("message", "Something went wrong")
                    val isError = response.code != 200

                    callback(message ?: "Unknown error", isError)
                }
            }
        })
    }
}
