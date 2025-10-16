package com.archeGlobal.one.controller

import com.archeGlobal.one.navigation.Navigator

open class WelcomeController(
    private val navigator: Navigator,
) {
    open fun onXOneClick() {
        navigator.navigateToLoginScreen()
    }
}
