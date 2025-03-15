package com.archeGlobal.one.controller

import com.archeGlobal.one.navigation.Navigator

class WelcomeController(private val navigator: Navigator) {

    fun onXOneClick() {
        navigator.navigateToLoginScreen()
    }
} 