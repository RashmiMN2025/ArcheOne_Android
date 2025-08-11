package com.archeGlobal.one.navigation

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import com.archeGlobal.one.*
import com.archeGlobal.one.GlobalCelebrationDetailActivity
import com.archeGlobal.one.ui.screens.CoreValuesActivity
import com.archeGlobal.one.ui.screens.IdeaVaultActivity
import java.net.URLEncoder

class AndroidNavigator(
    private val activity: ComponentActivity
) : Navigator {

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
            activity.startActivity(intent, bundle)
        } else {
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
            putExtra("fileUrl", "https://dev.arche.global/onboarding")
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
        startActivity(
            Intent(activity, OtpVerificationActivity::class.java).apply {
                putExtra("email", email)
                putExtra("mobile", mobile)
                putExtra("employeeId", employeeId)
            }
        )
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

    override fun navigateToGreetingDetail(
        selectedGreetingUrl: String,
        allGreetings: List<String>,
        message: String,
        category: String
    ) {
        val intent = Intent(activity, GreetingDetailActivity::class.java).apply {
            putExtra("imageUrl", selectedGreetingUrl)
            putExtra("category", category)
            putExtra("message", message)
            putStringArrayListExtra("allGreetings", ArrayList(allGreetings))
        }
        activity.startActivity(intent)
    }

    override fun navigateToLocations(showHeader: Boolean) {
        if (activity is HomeActivity) {
            navigate("locations")
        } else {
            startActivity(
                Intent(activity, HomeActivity::class.java).apply {
                    putExtra("isEmergencyContact", true)
                    putExtra("showHeader", showHeader)
                    putExtra("destination", "locations")
                }
            )
            activity.finish()
        }
    } override fun navigateToBusinessCard() {
        navigate("business_card")
    }

    override fun navigateToAsset() {
        navigate("asset")
    }

    override fun navigateToDeskCart() {
        startActivity(Intent(activity, DeskCartActivity::class.java))
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
        // URL should be fetched from login API
        // openWebView(url, "Medical")
    }

    override fun navigateToFinance() {
        navigate("service_not_available?serviceName=Finance")
    }

    override fun navigateToMyPay() {
        // URL should be fetched from login API
        // openWebView(url, "MyPay")
    }

    override fun navigateToAdmin() {
        // Implementation not provided
    }

    override fun navigateToHR() {
        // Implementation not provided
    }

    override fun navigateToHolidayOptions() {
        // Navigate directly to HolidayCalendarActivity instead of the intermediate options screen
        val intent = Intent(activity, HolidayCalendarActivity::class.java)
        activity.startActivity(intent)
    }

    override fun navigateToHolidayCalendar() {
        val intent = Intent(activity, HolidayCalendarActivity::class.java)
        activity.startActivity(intent)
    }

    override fun navigateToClientCalendar() {
        // Implementation not provided
    }

    override fun navigateToGreetings() {
        if (activity is HomeActivity) {
            navigate("greetings")
        } else {
            startActivity(
                Intent(activity, HomeActivity::class.java).apply {
                    putExtra("navigateTo", "greetings")
                }
            )
        }
    }

    override fun navigateToGlobalCelebration() {
        val intent = Intent(activity, GlobalCelebrationActivity::class.java)
        activity.startActivity(intent)
    }

    override fun navigateToGlobalCelebrationDetail(subcategory: com.archeGlobal.one.model.GreetingSubcategory) {
        val gson = com.google.gson.Gson()
        val subcategoryJson = gson.toJson(subcategory)
        val intent = android.content.Intent(activity, GlobalCelebrationDetailActivity::class.java).apply {
            putExtra("subcategory_json", subcategoryJson)
        }
        startActivity(intent, true)
    }

    override fun navigateToIdeaVault() {
        val intent = Intent(activity, IdeaVaultActivity::class.java)
        activity.startActivity(intent)
    }

    override fun navigateToAdminDashboard() {
        val intent = Intent(activity, AdminDashboardActivity::class.java)
        activity.startActivity(intent)
    }

    override fun navigateToInventory() {
        val intent = Intent(activity, InventoryActivity::class.java)
        activity.startActivity(intent)
    }

    override fun navigateToOrderReceived() {
        if (activity is HomeActivity) {
            navigate("order_received")
        } else {
            startActivity(
                Intent(activity, HomeActivity::class.java).apply {
                    putExtra("navigateTo", "order_received")
                }
            )
        }
    }

    override fun navigateToOrderDetails(orderId: String) {
        navController?.navigate("order_details/$orderId") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToXConnect(initialTab: String) {
        startActivity(
            Intent(activity, XConnectActivity::class.java).apply {
                putExtra("initialTab", initialTab)
            }
        )
    }

    override fun navigateToHelpdesk() {
        // Implementation not provided
    }

    override fun navigateToTrackTickets(category: String) {
        // Use NavController to navigate while preserving back stack
        navController?.navigate("track_tickets") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToAnnouncements() {
        // Implementation not provided
    }

    override fun navigateToXProfile() {
        // Implementation not provided
    }

    override fun navigateToPasswordReset() {
        startActivity(Intent(activity, PasswordResetActivity::class.java))
    }

    override fun navigateToPolicy() {
        navigate("policy")
    }

    override fun navigateToSOS(showHeader: Boolean) {
        if (activity is HomeActivity) {
            navigate("sos?showHeader=$showHeader")
        } else {
            startActivity(
                Intent(activity, SOSActivity::class.java).apply {
                    putExtra("showHeader", showHeader)
                }
            )
        }
    }

    override fun navigateToTravel() {
        navController?.navigate("travel") {
            launchSingleTop = true
            restoreState = true
            popUpTo("home") {
                // Not inclusive so the home screen remains in the back stack
                inclusive = false
            }
        }
    }

    override fun navigateToTravelHistory() {
        navController?.navigate("travel_history") {
            launchSingleTop = true
            restoreState = true
            popUpTo("home") {
                // Not inclusive so the home screen remains in the back stack
                inclusive = false
            }
        }
    }

    override fun navigateToTravelExpenses() {
        navController?.navigate("travel_history") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToTravelRequestDetail() {
        navController?.navigate("travel_request_detail") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToTravelHistoryDetail() {
        navController?.navigate("travel_history_detail") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToTravelApprovals() {
        navController?.navigate("travel_approvals") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToTravelApprovalDetail() {
        navController?.navigate("travel_approval_detail") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToTravelApprovalConfirm() {
        navController?.navigate("travel_approval_confirm") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToTravelApprove() {
        android.util.Log.d("AndroidNavigator", "navigateToTravelApprove called")
        android.util.Log.d("AndroidNavigator", "navController is ${if (navController == null) "null" else "not null"}")

        // Log current navigation state
        navController?.let { nc ->
            val currentRoute = nc.currentDestination?.route
            android.util.Log.d("AndroidNavigator", "Current navigation destination: $currentRoute")
            // We can't access the private backQueue property directly
            android.util.Log.d("AndroidNavigator", "Attempting to navigate to travel_approve")
        }

        try {
            navController?.navigate("travel_approve") {
                launchSingleTop = true
                restoreState = true
            }
            android.util.Log.d("AndroidNavigator", "After navigate() call - navigation command sent successfully")
        } catch (e: Exception) {
            android.util.Log.e("AndroidNavigator", "Error during navigation: ${e.message}", e)
        }

        // Verify navigation occurred
        android.util.Log.d("AndroidNavigator", "Current destination after navigation attempt: ${navController?.currentDestination?.route}")
    }

    override fun navigateToTravelReject() {
        navController?.navigate("travel_reject") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToTravelApprovalDetails() {
        navController?.navigate("travel_approval_details") {
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateToSAP() {
        // URL should be fetched from login API
        // openWebView(url, "SAP")
    }

    override fun navigateToAmple() {
        // URL should be fetched from login API
        // openWebView(url, "Ample")
    }

    override fun navigateToAboutUs() {
        // URL should be fetched from login API
        // openWebView(url, "AboutUs")
    }

    override fun navigateToZingHR() {
        // URL should be fetched from login API
        // openWebView(url, "ZingHR")
    }

    override fun navigateToChat() {
        // Get reference to ChatController to clear history before navigation
        if (activity is HomeActivity) {
            val chatController = (activity as HomeActivity).getChatController()
            // Clear chat history before navigating
            chatController?.clearChatHistory()
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
    } override fun getCurrentRoute(): String? {
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

    override fun popBackStack() {
        try {
            navController?.popBackStack()
        } catch (e: Exception) {
            Log.e("AndroidNavigator", "Error popping back stack", e)
            // Fallback to navigating to travel approvals if pop fails
            navigateToTravelApprovals()
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

    override fun navigateToMpinSetup(email: String, mobile: String, employeeId: String, token: String) {
        val intent = Intent(activity, com.archeGlobal.one.ui.screens.MpinActivity::class.java).apply {
            putExtra("email", email)
            putExtra("mobile", mobile)
            putExtra("employeeId", employeeId)
            putExtra("token", token)
        }
        activity.startActivity(intent)
        activity.finish()
    }

    override fun showUpdateDialog() {
        when (activity) {
            is com.archeGlobal.one.LoginActivity -> {
                (activity as com.archeGlobal.one.LoginActivity).showUpdateDialog()
            }
            is com.archeGlobal.one.OtpVerificationActivity -> {
                (activity as com.archeGlobal.one.OtpVerificationActivity).showUpdateDialog()
            }
            else -> {
                // For other activities, navigate to LoginActivity and show update dialog
                val intent = Intent(activity, com.archeGlobal.one.LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("showUpdateDialog", true)
                }
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }

    // Methods for services with dynamic URLs
    override fun navigateToSAPWithUrl(url: String) {
        openWebView(url, "SAP")
    }

    override fun navigateToAmpleWithUrl(url: String) {
        openWebView(url, "Ample")
    }

    override fun navigateToZingHRWithUrl(url: String) {
        openWebView(url, "ZingHR")
    }

    override fun navigateToMyPayWithUrl(url: String) {
        openWebView(url, "MyPay")
    }

    override fun navigateToMedicalWithUrl(url: String) {
        openWebView(url, "Medical")
    }

    override fun navigateToAboutUsWithUrl(url: String) {
        openWebView(url, "About Us")
    }

    override fun openPulseLoginWithUrl(url: String) {
        openWebView(url, "Pulse")
    }
}
