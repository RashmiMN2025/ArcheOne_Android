package com.example.xone.controller

import android.util.Log
import com.example.xone.model.UserData
import com.example.xone.network.RetrofitClient
import com.example.xone.network.VerifyOtpRequest
import com.example.xone.navigation.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class OtpVerificationController(
    private val navigator: Navigator,
    private val loginController: LoginController
) {
    fun verifyOtp(
        email: String,
        mobile: String,
        employeeId: String,
        otp: String,
        callback: (String, Boolean) -> Unit
    ) {
        loginController.verifyOtpAndLogin(email, mobile, employeeId, otp, callback)
    }

    fun resendOtp(email: String, mobile: String, employeeId: String, callback: (String) -> Unit) {
        // Implementation for resending OTP
    }
}
