package com.archeGlobal.one.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.app.ActivityOptions
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import com.archeGlobal.one.*
import java.net.URLEncoder

class AndroidNavigator(private val activity: ComponentActivity) : Navigator {
    private var navController: NavController? = null

    fun setNavController(controller: NavController) {
        this.navController = controller
    }

    private fun navigate(route: String, builder: NavOptionsBuilder.() -> Unit = {}) {
        navController?.navigate(route, navOptions(builder))
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
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://pulse.netcon.in/login")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            false
        )
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
        if (activity !is HomeActivity) {
            startActivity(
                Intent(activity, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("FROM_OTP", fromOtp)
                    putExtra("showBiometricSetup", showBiometricSetup)
                    putExtra("email", email)
                    putExtra("mobile", mobile)
                    putExtra("employeeId", employeeId)
                    putExtra("fromLogin", true)
                },
                true,
                true
            )
            activity.finish()
        } else {
            navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        }
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
    }

    override fun navigateToBusinessCard() = navigate("business_card")
    override fun navigateToAsset() = navigate("asset")
    override fun navigateToID() = Unit
    override fun navigateToTimesheet() = Unit
    override fun navigateToLeave() = Unit
    
    override fun navigateToMyDocuments() {
        startActivity(Intent(activity, MyDocumentsActivity::class.java))
    }

    override fun navigateToUserDocuments() {
        startActivity(Intent(activity, UserDocumentsActivity::class.java))
    }

    override fun navigateToMyCareer() = Unit
    override fun navigateToELearning() = Unit
    override fun navigateToGoalSetting() = Unit
    override fun navigateToXCard() = Unit
    
    override fun navigateToMedical() = openWebView("https://ilhc.icicilombard.com/Customer/iCard", "Medical")
    override fun navigateToFinance() = openWebView("https://ess.azatecon.com/login", "Finance")
    override fun navigateToAdmin() = Unit
    override fun navigateToHR() = Unit
    override fun navigateToHolidayCalendar() = navigate("holiday_calendar")
    override fun navigateToClientCalendar() = Unit

    override fun navigateToGreetings() {
        if (activity is HomeActivity) {
            navigate("greetings")
        } else {
            startActivity(Intent(activity, HomeActivity::class.java).apply {
                putExtra("navigateTo", "greetings")
            })
        }
    }

    override fun navigateToXConnect() {
        startActivity(Intent(activity, XConnectActivity::class.java))
    }

    override fun navigateToXConnect(initialTab: String) {
        startActivity(Intent(activity, XConnectActivity::class.java).apply {
            putExtra("initialTab", initialTab)
        })
    }

    override fun navigateToHelpdesk() = Unit
    override fun navigateToAnnouncements() = Unit
    override fun navigateToXProfile() = Unit
    override fun navigateToPasswordReset() = Unit
    override fun navigateToPolicy() = navigate("policy")

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

    override fun navigateToProfile() = navigate("profile")
    override fun navigateToAboutMe() = navigate("aboutme")
    override fun navigateToAddressDetails() = navigate("addressdetails")
    override fun navigateToEmergencyContact() = navigate("emergencycontact")
    
    override fun navigateToPDFViewer(pdfUrl: String, title: String) {
        try {
            navigate("pdf_viewer/${URLEncoder.encode(pdfUrl, "UTF-8")}?title=$title")
        } catch (e: Exception) {
            Log.e("AndroidNavigator", "Error navigating to PDF viewer", e)
        }
    }

    override fun navigateToArcheOdyssey() = navigate("arche_odyssey")
    override fun navigateToCommunique() = navigate("communique")
    override fun navigateToVision() = navigate("vision")
    override fun navigateToCoreValues() = navigate("core_values")
    override fun navigateToAboutUs() = openWebView("https://arche.global/arche-one-aboutus", "About Us")

    override fun getCurrentRoute(): String? = navController?.currentDestination?.route

    override fun refreshCurrentScreen() {
        getCurrentRoute()?.let { currentRoute ->
            try {
                navigate(currentRoute) {
                    popUpTo(currentRoute) { inclusive = true }
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
}
