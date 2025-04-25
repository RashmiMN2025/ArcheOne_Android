package com.archeGlobal.one.controller

import com.archeGlobal.one.navigation.Navigator

class ArcheOdysseyController(
    private val navigator: Navigator
) {
    fun onBackPressed() {
        navigator.navigateToHome()
    }
    
    fun onCommuniqueClick() {
        navigator.navigateToCommunique()
    }
}
