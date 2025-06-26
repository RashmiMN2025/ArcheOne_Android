package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import com.archeGlobal.one.HolidayOptionsActivity
import com.archeGlobal.one.navigation.Navigator

/**
 * Controller for the Holiday Options screen
 */
class HolidayOptionsController(
    private val context: Context,
    private val navigator: Navigator
) {
    fun navigateToHolidayCalendar() {
        Log.d("HolidayOptionsController", "Navigating to Holiday Calendar")
        navigator.navigateToHolidayCalendar()
    }
    fun navigateToKudos() {
        // Navigate to service not available screen for Kudos
        Log.d("HolidayOptionsController", "Navigating to Service Not Available screen for Kudos")
        if (navigator is com.archeGlobal.one.navigation.AndroidNavigator) {
            navigator.navController?.navigate("service_not_available?serviceName=Kudos")
        }
    }

    fun onBackPressed() {
        (context as? HolidayOptionsActivity)?.finishWithAnimation()
    }
}
