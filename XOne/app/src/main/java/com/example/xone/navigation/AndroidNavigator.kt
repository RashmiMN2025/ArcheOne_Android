package com.example.xone.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.xone.LoginActivity
import com.example.xone.OtpVerificationActivity

class AndroidNavigator(private val context: Context) : Navigator {
    override fun openPulseLogin() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pulse.netcon.in/login"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun navigateToLoginScreen() {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun navigateToOtpVerification(email: String, mobile: String, employeeId: String) {
        val intent = Intent(context, OtpVerificationActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("mobile", mobile)
        intent.putExtra("employeeId", employeeId)
        context.startActivity(intent)
    }
}
