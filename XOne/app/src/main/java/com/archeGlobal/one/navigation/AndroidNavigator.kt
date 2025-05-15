package com.archeGlobal.one.navigation

import android.content.Intent
import android.util.Log
import android.app.ActivityOptions
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import com.archeGlobal.one.*
import com.archeGlobal.one.ui.screens.CoreValuesActivity
import com.archeGlobal.one.ui.screens.IdeaVaultActivity
import java.net.URLEncoder

class AndroidNavigator(private val activity: ComponentActivity) : Navigator {
    internal var navController: NavController? = null

    fun setNavController(controller: NavController) {
        this.navController = controller
    }

    private fun navigate(route: String) {
        navController?.navigate(route)
    }

    private fun navigate(route: String, optionsBuilder: (NavOptionsBuilder) -> Unit) {
        navController?.navigate(route, navOptions(optionsBuilder))
    }

    private fun startActivity(intent: Intent, withAnimation: Boolean = true, slideLeft: Boolean = false) {
        if (withAnimation) {
            val bundle: Bundle = ActivityOptions.makeCustomAnimation(
                activity,
                if (slideLeft) R.anim.slide_in_left else R.anim.slide_in_right,
                if (slideLeft) R.anim.slide_out_right else R.anim.slide_out_left
            ).toBundle()
            activity.startActivity(intent, bundle)        } else {
            activity.startActivity(intent)
        }
    }

    private fun openWebView(url: String, title: String) {
        val intent = Intent(activity, WebViewActivity::class.java).apply {
            putExtra("fileUrl", url)
            putExtra("title", title)
        }
        startActivity(intent)
    }

    override fun openPulseLogin() {
        val intent = Intent(activity, WebViewActivity::class.java).apply {
            putExtra("fileUrl", "https://pulse.netcon.in/onboarding")
            putExtra("title", "Pulse")
        }
        startActivity(intent)
    }

    override fun navigateToLoginScreen() {
        startActivity(
            Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            false
        )
        activity.finish()
    }

    override fun navigateToOtpVerification(email: String, mobile: String, employeeId: String) {
        startActivity(Intent(activity, OtpVerificationActivity::class.java).apply {
            putExtra("email", email)
            putExtra("mobile", mobile)
            putExtra("employeeId", employeeId)
        })
    }

    override fun navigateToHome(
        fromOtp: Boolean,
        showBiometricSetup: Boolean,
        email: String,
        mobile: String,
        employeeId: String
    ) {
        Log.d("AndroidNavigator", "navigateToHome called with fromOtp=$fromOtp")

        if (activity is HomeActivity) {
            // If we're already in HomeActivity, just navigate to the home route
            try {
                // Use more specific navigation options to ensure we clear the back stack
                navController?.navigate("home") {
                    // Pop up to home and make sure to include it in the pop operation
                    popUpTo("home") {
                        inclusive = true
                    }
                    // Ensure we create a single instance at the top of the stack
                    launchSingleTop = true
                    // Don't restore any saved state
                    restoreState = false
                }
                Log.d("AndroidNavigator", "Successfully navigated to home route within HomeActivity with popUpTo")
            } catch (e: Exception) {
                Log.e("AndroidNavigator", "Error navigating to home within HomeActivity: ${e.message}")
                // If navigation fails, try to recreate the activity
                navigateToHomeActivity(fromOtp, showBiometricSetup, email, mobile, employeeId)
            }
        } else {
            // If we're in a different activity, start HomeActivity
            navigateToHomeActivity(fromOtp, showBiometricSetup, email, mobile, employeeId)
        }
    }

    private fun navigateToHomeActivity(
        fromOtp: Boolean,
        showBiometricSetup: Boolean,
        email: String,
        mobile: String,
        employeeId: String
    ) {
        startActivity(
            Intent(activity, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("FROM_OTP", fromOtp)
                putExtra("showBiometricSetup", showBiometricSetup)
                putExtra("email", email)
                putExtra("mobile", mobile)
                putExtra("employeeId", employeeId)
                putExtra("fromLogin", true)
                putExtra("navigateTo", "home")
            },
            true,
            true
        )

        // Finish the current activity to prevent going back to it
        if (!(activity is HomeActivity)) {
            activity.finish()
        }
    }
      // Implementation of navigateToGreetingDetail method
    override fun navigateToGreetingDetail(imageUrl: String, message: String, category: String) {
        Log.d("AndroidNavigator", "Navigating to greeting detail: $category")
        val intent = Intent(activity, GreetingDetailActivity::class.java).apply {
            putExtra("imageUrl", imageUrl)
            putExtra("message", message)
            putExtra("category", category)
        }
        // Use custom slide animation for a smoother transition
        startActivity(intent, true)
    }

    override fun navigateToLocations(showHeader: Boolean) {
        if (activity is HomeActivity) {
            navigate("locations")
        } else {
            startActivity(Intent(activity, HomeActivity::class.java).apply {
                putExtra("isEmergencyContact", true)
                putExtra("showHeader", showHeader)
                putExtra("destination", "locations")
            })
            activity.finish()
        }
    }    override fun navigateToBusinessCard() {
        navigate("business_card")
    }

    override fun navigateToAsset() {
        navigate("asset")
    }

    override fun navigateToID() {
        // Implementation not provided
    }

    override fun navigateToTimesheet() {
        // Implementation not provided
    }

    override fun navigateToLeave() {
        // Implementation not provided
    }

    override fun navigateToMyDocuments() {
        startActivity(Intent(activity, MyDocumentsActivity::class.java))
    }

    override fun navigateToUserDocuments() {
        startActivity(Intent(activity, UserDocumentsActivity::class.java))
    }

    override fun navigateToMyCareer() {
        // Implementation not provided
    }

    override fun navigateToELearning() {
        // Implementation not provided
    }

    override fun navigateToGoalSetting() {
        // Implementation not provided
    }

    override fun navigateToXCard() {
        // Implementation not provided
    }

    override fun navigateToMedical() {
        openWebView("https://ilhc.icicilombard.com/Customer/iCard", "Medical")
    }

    override fun navigateToFinance() {
        openWebView("https://ess.azatecon.com/login", "Finance")
    }

    override fun navigateToAdmin() {
        // Implementation not provided
    }

    override fun navigateToHR() {
        // Implementation not provided
    }

    override fun navigateToHolidayOptions() {
        navigate("holiday_options")
    }

    override fun navigateToHolidayCalendar() {
        navigate("calendar")
    }

    override fun navigateToClientCalendar() {
        // Implementation not provided
    }

    override fun navigateToGreetings() {
        if (activity is HomeActivity) {
            navigate("greetings")
        } else {
            startActivity(Intent(activity, HomeActivity::class.java).apply {
                putExtra("navigateTo", "greetings")
            })
        }
    }

    override fun navigateToIdeaVault() {
        val intent = Intent(activity, IdeaVaultActivity::class.java)
        activity.startActivity(intent)
    }


    override fun navigateToXConnect() {
        startActivity(Intent(activity, XConnectActivity::class.java))
    }

    override fun navigateToXConnect(initialTab: String) {
        startActivity(Intent(activity, XConnectActivity::class.java).apply {
            putExtra("initialTab", initialTab)
        })
    }

    override fun navigateToHelpdesk() {
        // Implementation not provided
    }

    override fun navigateToAnnouncements() {
        // Implementation not provided
    }

    override fun navigateToXProfile() {
        // Implementation not provided
    }

    override fun navigateToPasswordReset() {
        // Implementation not provided
    }

    override fun navigateToPolicy() {
        navigate("policy")
    }

    override fun navigateToSOS(showHeader: Boolean) {
        if (activity is HomeActivity) {
            navigate("sos?showHeader=$showHeader")
        } else {
            startActivity(Intent(activity, SOSActivity::class.java).apply {
                putExtra("showHeader", showHeader)
            })
        }
    }

    override fun navigateToTravelExpenses() {
        openWebView("https://ithsmart.travelhouseindia.in/travel/travel_web.xhtml", "Travel & Expenses")
    }

    override fun navigateToSAP() {
        openWebView("https://my422539.businessbydesign.cloud.sap", "SAP")
    }

    override fun navigateToAmple() {
        openWebView("https://amplenetcon.com", "Ample")
    }

    override fun navigateToAboutUs() {
        openWebView("https://arche.global/arche-one-aboutus", "AboutUs")
    }

    override fun navigateToZingHR() {
        openWebView("https://portal.zinghr.com/2015/pages/authentication/zing.aspx?ccode=netcongrp", "ZingHR")
    }

    override fun navigateToChat() {
        if (activity is HomeActivity) {
            navigate("chat")
        } else {
            startActivity(Intent(activity, ChatActivity::class.java))
        }
    }

    override fun navigateToProfile() {
        navigate("profile")
    }

    override fun navigateToAboutMe() {
        navigate("aboutme")
    }

    override fun navigateToAddressDetails() {
        navigate("addressdetails")
    }

    override fun navigateToEmergencyContact() {
        navigate("emergencycontact")
    }

    override fun navigateToPDFViewer(pdfUrl: String, title: String) {
        try {
            navigate("pdf_viewer/${URLEncoder.encode(pdfUrl, "UTF-8")}?title=$title")
        } catch (e: Exception) {
            Log.e("AndroidNavigator", "Error navigating to PDF viewer", e)
        }
    }

    override fun navigateToArcheOdyssey() {
        navigate("arche_odyssey")
    }

    override fun navigateToCommunique() {
        navigate("communique")
    }

    override fun navigateToCoreValues() {
        val intent = Intent(activity, CoreValuesActivity::class.java)
        activity.startActivity(intent)
    }

    override fun navigateToVision() {
        val intent = Intent(activity, VisionActivity::class.java)
        activity.startActivity(intent)
    }

    override fun navigateToTodo() {
        startActivity(Intent(activity, TodoActivity::class.java))
    }    override fun getCurrentRoute(): String? {
        return navController?.currentDestination?.route
    }

    override fun refreshCurrentScreen() {
        getCurrentRoute()?.let { currentRoute ->
            try {
                navigate(currentRoute) { builder ->
                    builder.popUpTo(currentRoute) { inclusive = true }
                }
            } catch (e: Exception) {
                Log.e("AndroidNavigator", "Error refreshing screen", e)
            }
        }
    }

    fun printNavigationGraph() {
        navController?.graph?.let { graph ->
            Log.d("AndroidNavigator", "Navigation graph routes:")
            graph.forEach { node ->
                Log.d("AndroidNavigator", "Route: ${node.route}")
            }
        } ?: Log.e("AndroidNavigator", "Navigation graph not available")
    }

    override fun getHomeIntent(): Intent {
        // Create an intent that will navigate to the HomeActivity
        return Intent(activity, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigateTo", "home")
        }
    }
}