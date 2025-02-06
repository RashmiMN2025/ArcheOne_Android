package com.example.xone.navigation

import android.util.Log
import androidx.navigation.NavHostController

class AndroidNavigator(
    private val navController: NavHostController
) : Navigator {
    override fun openPulseLogin() {
        // TODO: Implement pulse login navigation
    }

    override fun navigateToHome() {
        navController.navigate("home")
    }

    override fun navigateToID() {
        navController.navigate("id")
    }

    override fun navigateToAsset() {
        navController.navigate("asset")
    }

    override fun navigateToTimesheet() {
        navController.navigate("timesheet")
    }

    override fun navigateToLeave() {
        navController.navigate("leave")
    }

    override fun navigateToMyDocuments() {
        navController.navigate("documents")
    }

    override fun navigateToMyCareer() {
        navController.navigate("career")
    }

    override fun navigateToELearning() {
        navController.navigate("learning")
    }

    override fun navigateToGoalSetting() {
        navController.navigate("goals")
    }

    override fun navigateToXCard() {
        navController.navigate("xcard")
    }

    override fun navigateToMedical() {
        navController.navigate("medical")
    }

    override fun navigateToFinance() {
        navController.navigate("finance")
    }

    override fun navigateToAdmin() {
        navController.navigate("admin")
    }

    override fun navigateToHR() {
        navController.navigate("hr")
    }

    override fun navigateToHolidayCalendar() {
        navController.navigate("holiday_calendar")
    }

    override fun navigateToClientCalendar() {
        navController.navigate("client_calendar")
    }

    override fun navigateToGreetings() {
        navController.navigate("greetings")
    }

    override fun navigateToXConnect() {
        navController.navigate("xconnect")
    }

    override fun navigateToLocations() {
        Log.d("AndroidNavigator", "Navigating to locations screen")
        navController.navigate("locations")
    }

    override fun navigateToHelpdesk() {
        navController.navigate("helpdesk")
    }

    override fun navigateToAnnouncements() {
        navController.navigate("announcements")
    }

    override fun navigateToXProfile() {
        navController.navigate("xprofile")
    }

    override fun navigateToPasswordReset() {
        navController.navigate("password_reset")
    }

    override fun navigateToPolicy() {
        navController.navigate("policy")
    }

    override fun navigateToSOS() {
        navController.navigate("sos")
    }

    override fun navigateToTravelExpenses() {
        navController.navigate("travel_expenses")
    }

    override fun navigateToSAP() {
        navController.navigate("sap")
    }

    override fun navigateToChat() {
        navController.navigate("chat")
    }

    override fun navigateToBusinessCard() {
        navController.navigate("business_card")
    }
} 
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
