package com.example.xone.controller

import com.example.xone.navigation.Navigator

class WelcomeController(private val navigator: Navigator) {

    fun onXOneClick() {
        navigator.navigateToLoginScreen()
    }
} 