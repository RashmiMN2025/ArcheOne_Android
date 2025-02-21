package com.example.xone.controller

import com.example.xone.navigation.Navigator

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
