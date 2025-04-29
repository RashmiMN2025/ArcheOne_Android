package com.archeGlobal.one.controller

import com.archeGlobal.one.navigation.Navigator

class ArcheOdysseyController(
    private val navigator: Navigator
) {
    fun onBackPressed() {
        navigator.navigateToHome()
    }

    fun onCoreValuesClick() {
        navigator.navigateToCoreValues()
    }

    fun onAboutUs() {
        navigator.navigateToAboutUs()
    }
}
