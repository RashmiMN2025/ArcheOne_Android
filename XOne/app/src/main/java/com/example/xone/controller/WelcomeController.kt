package com.example.xone.controller

import com.example.xone.model.WelcomeModel
import com.example.xone.navigation.Navigator

class WelcomeController(private val navigator: Navigator) {
    private var welcomeModel = WelcomeModel()
    
    fun getWelcomeData(): WelcomeModel = welcomeModel
    
    fun onXOneClick() {
        navigator.navigateToHome()
    }

    fun onPulseClick() {
        navigator.openPulseLogin()
    }
    
    private fun updateModel(newModel: WelcomeModel) {
        welcomeModel = newModel
        // Notify view of changes if needed
    }
} 