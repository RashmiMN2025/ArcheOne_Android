package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.model.PasswordResetRequest
import com.archeGlobal.one.model.PasswordResetResponse
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PasswordResetController(
    private val navigator: Navigator,
    private val context: Context
) {
    // Initialize RetrofitClient with context and use its apiService
    init {
        RetrofitClient.initialize(context)
    }
    private val apiService = RetrofitClient.apiService

    fun resetPassword(
        email: String,
        employeeId: String,
        callback: (response: PasswordResetResponse?, errorMessage: String?) -> Unit
    ) {
        val request = PasswordResetRequest(email, employeeId)
        
        apiService.resetPassword(request).enqueue(object : Callback<PasswordResetResponse> {
            override fun onResponse(
                call: Call<PasswordResetResponse>,
                response: Response<PasswordResetResponse>
            ) {
                if (response.isSuccessful) {
                    val resetResponse = response.body()
                    if (resetResponse != null && resetResponse.status == 200) {
                        callback(resetResponse, null)
                    } else {
                        val errorMessage = resetResponse?.message ?: "Password reset failed"
                        callback(resetResponse, errorMessage)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    callback(null, "Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<PasswordResetResponse>, t: Throwable) {
                Log.e("PasswordResetController", "API call failed", t)
                callback(null, "Network error: ${t.message}")
            }
        })
    }
}
