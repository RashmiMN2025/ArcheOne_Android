package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
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
        // This would navigate to Kudos screen when implemented
        Log.d("HolidayOptionsController", "Kudos feature is not available yet")
        android.widget.Toast.makeText(
            context,
            "Kudos feature is coming soon",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    fun onBackPressed() {
        navigator.navigateToHome()
    }
}
