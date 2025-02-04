package com.example.xone.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import android.util.Log

class AndroidNavigator(
    private val context: Context,
    private val navController: NavController
) : Navigator {
    override fun openPulseLogin() {
        val pulseUrl = "https://pulse.netcon.in/login"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pulseUrl))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun navigateToHome() {
        navController.navigate("home")
    }

    override fun navigateToID() {
        // TODO: Implement navigation
    }

    override fun navigateToAsset() {
        navController.navigate("asset")
    }

    override fun navigateToTimesheet() {
        // TODO: Implement navigation
    }

    override fun navigateToLeave() {
        // TODO: Implement navigation
    }

    override fun navigateToMyDocuments() {
        navController.navigate("documents")
    }

    override fun navigateToMyCareer() {
        // TODO: Implement navigation
    }

    override fun navigateToELearning() {
        // TODO: Implement navigation
    }

    override fun navigateToGoalSetting() {
        // TODO: Implement navigation
    }

    override fun navigateToXCard() {
        navController.navigate("xcard")
    }

    override fun navigateToMedical() {
        // TODO: Implement navigation
    }

    override fun navigateToFinance() {
        // TODO: Implement navigation
    }

    override fun navigateToAdmin() {
        // TODO: Implement navigation
    }

    override fun navigateToHR() {
        // TODO: Implement navigation
    }

    override fun navigateToHolidayCalendar() {
        navController.navigate("holiday_calendar")
    }

    override fun navigateToClientCalendar() {
        // TODO: Implement navigation
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
        // TODO: Implement navigation
    }

    override fun navigateToAnnouncements() {
        // TODO: Implement navigation
    }

    override fun navigateToXProfile() {
        navController.navigate("xprofile")
    }

    override fun navigateToPasswordReset() {
        // TODO: Implement navigation
    }

    override fun navigateToPolicy() {
        navController.navigate("policy")
    }

    override fun navigateToSOS() {
        navController.navigate("sos")
    }

    override fun navigateToTravelExpenses() {
        // TODO: Implement navigation
    }

    override fun navigateToSAP() {
        // TODO: Implement navigation
    }

    override fun navigateToChat() {
        navController.navigate("chat")
    }
} 