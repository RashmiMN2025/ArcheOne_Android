package com.archeGlobal.one.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import com.archeGlobal.one.LoginActivity
import com.archeGlobal.one.OtpVerificationActivity
import com.archeGlobal.one.HomeActivity
import com.archeGlobal.one.MyDocumentsActivity
import com.archeGlobal.one.UserDocumentsActivity
import com.archeGlobal.one.XConnectActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.SOSActivity
import com.archeGlobal.one.ChatActivity

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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                       Intent.FLAG_ACTIVITY_CLEAR_TASK or
                       Intent.FLAG_ACTIVITY_CLEAR_TOP
        activity.startActivity(intent)
        activity.finish()
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
        Log.d("AndroidNavigator", "Navigating to home screen")
        if (activity !is HomeActivity) {
            val intent = Intent(activity, HomeActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            activity.finish()
        } else {
            navController?.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        }
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
        Log.d("AndroidNavigator", "Navigating to MyDocuments screen")
        val intent = Intent(activity, MyDocumentsActivity::class.java)
        activity.startActivity(intent)
    }
    override fun navigateToUserDocuments() {
        Log.d("AndroidNavigator", "Navigating to UserDocuments screen")
        val intent = Intent(activity, UserDocumentsActivity::class.java)
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
    override fun navigateToHolidayCalendar() {
        Log.d("AndroidNavigator", "Navigating to policy screen")
        navController?.navigate("holiday_calendar")
    }
    override fun navigateToClientCalendar() {}
    override fun navigateToGreetings() {}
    override fun navigateToXConnect() {
        Log.d("AndroidNavigator", "Navigating to XConnect screen")
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
    override fun navigateToSOS(showHeader: Boolean) {
        Log.d("AndroidNavigator", "Navigating to SOS screen with showHeader=$showHeader")
        if (activity is HomeActivity) {
            navController?.navigate("sos?showHeader=$showHeader") // Pass showHeader as a query parameter
        } else {
            val intent = Intent(activity, SOSActivity::class.java)
            intent.putExtra("showHeader", showHeader) // Pass showHeader as an intent extra
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
    override fun navigateToTravelExpenses() {}
    override fun navigateToSAP() {}
    override fun navigateToChat() {
        Log.d("AndroidNavigator", "Navigating to chat screen")
        if (activity is HomeActivity) {
            navController?.navigate("chat")
        } else {
            val intent = Intent(activity, ChatActivity::class.java)
            activity.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
    override fun navigateToProfile() {
        Log.d("AndroidNavigator", "Navigating to profile screen")
        navController?.navigate("profile")
    }
    
    override fun navigateToAboutMe() {
        Log.d("AndroidNavigator", "Navigating to about me screen")
        navController?.navigate("aboutme")
    }
    
    override fun navigateToAddressDetails() {
        Log.d("AndroidNavigator", "Navigating to address details screen")
        navController?.navigate("addressdetails")
    }
    
    override fun navigateToEmergencyContact() {
        Log.d("AndroidNavigator", "Navigating to emergency contact screen")
        navController?.navigate("emergencycontact")
    }
    
    override fun navigateToPDFViewer(pdfUrl: String, title: String) {
        Log.d("AndroidNavigator", "Navigating to PDF viewer screen: $pdfUrl")
        try {
            // URL encode the PDF URL to handle special characters
            val encodedUrl = java.net.URLEncoder.encode(pdfUrl, "UTF-8")
            navController?.navigate("pdf_viewer/$encodedUrl?title=$title")
        } catch (e: Exception) {
            Log.e("AndroidNavigator", "Error navigating to PDF viewer: ${e.message}", e)
        }
    }

    // New method to get the current route
    override fun getCurrentRoute(): String? {
        val currentDestination = navController?.currentDestination
        return currentDestination?.route
    }

    // Method to refresh the current screen by navigating to it again
    override fun refreshCurrentScreen() {
        val currentRoute = getCurrentRoute() ?: return
        Log.d("AndroidNavigator", "Refreshing current screen: $currentRoute")
        
        try {
            // Navigate to the same route to force a refresh
            navController?.navigate(currentRoute) {
                // This will replace the current destination with the same one, forcing a recomposition
                popUpTo(currentRoute) { inclusive = true }
            }
        } catch (e: Exception) {
            Log.e("AndroidNavigator", "Error refreshing current screen: ${e.message}", e)
        }
    }
}
