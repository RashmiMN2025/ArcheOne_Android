package com.example.xone.controller

import com.example.xone.model.Holiday
import com.example.xone.navigation.Navigator
import com.example.xone.utils.HolidayData

class HolidayCalendarController(
    private val navigator: Navigator? = null // Navigator is now optional
) {
    fun getHolidays(): List<Holiday> {
        return HolidayData.holidays
    }

    fun onBackPressed() {
        navigator?.navigateToHome() // Only call navigateToHome if navigator is not null
    }
}

