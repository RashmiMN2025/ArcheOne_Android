package com.example.xone.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import com.example.xone.LoginActivity
import com.example.xone.OtpVerificationActivity
import com.example.xone.HomeActivity
import com.example.xone.MyDocumentsActivity
import com.example.xone.XConnectActivity
import com.example.xone.ui.screens.SOSActivity

class AndroidNavigator(private val activity: ComponentActivity) : Navigator {
    private var navController: NavController? = null

    fun setNavController(controller: NavController) {
        navController = controller
    }

    override fun openPulseLogin() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pulse.netcon.in/login"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
    }

    override fun navigateToLoginScreen() {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
    }

    override fun navigateToOtpVerification(email: String, mobile: String, employeeId: String) {
        val intent = Intent(activity, OtpVerificationActivity::class.java).apply {
            putExtra("email", email)
            putExtra("mobile", mobile)
            putExtra("employeeId", employeeId)
        }
        activity.startActivity(intent)
    }

    override fun navigateToHome() {
        val intent = Intent(activity, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
    }

    override fun navigateToLocations() {
        Log.d("AndroidNavigator", "Navigating to locations screen")
        navController?.navigate("locations")
    }

    override fun navigateToBusinessCard() {
        Log.d("AndroidNavigator", "Navigating to business card screen")
        navController?.navigate("business_card")
    }

    override fun navigateToAsset() {
        navController?.navigate("asset")
    }

    // Other navigation methods with default empty implementations
    override fun navigateToID() {}
    override fun navigateToTimesheet() {}
    override fun navigateToLeave() {}
    override fun navigateToMyDocuments() {
        val intent = Intent(activity, MyDocumentsActivity::class.java)
        activity.startActivity(intent)
    }
    override fun navigateToMyCareer() {}
    override fun navigateToELearning() {}
    override fun navigateToGoalSetting() {}
    override fun navigateToXCard() {}
    override fun navigateToMedical() {}
    override fun navigateToFinance() {}
    override fun navigateToAdmin() {}
    override fun navigateToHR() {}
    override fun navigateToHolidayCalendar() {}
    override fun navigateToClientCalendar() {}
    override fun navigateToGreetings() {}
    override fun navigateToXConnect() {
        val intent = Intent(activity, XConnectActivity::class.java)
        activity.startActivity(intent)
    }
    override fun navigateToHelpdesk() {}
    override fun navigateToAnnouncements() {}
    override fun navigateToXProfile() {}
    override fun navigateToPasswordReset() {}
    override fun navigateToPolicy() {
        Log.d("AndroidNavigator", "Navigating to policy screen")
        navController?.navigate("policy")
    }
    override fun navigateToSOS() {
        val intent = Intent(activity, SOSActivity::class.java)
        activity.startActivity(intent)
    }
    override fun navigateToTravelExpenses() {}
    override fun navigateToSAP() {}
    override fun navigateToChat() {}
}
