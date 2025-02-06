package com.example.xone.navigation

interface Navigator {
    fun openPulseLogin()
    fun navigateToLoginScreen()
    fun navigateToOtpVerification(email: String, mobile: String, employeeId: String)
} 