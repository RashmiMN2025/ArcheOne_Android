package com.example.xone.controller

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.xone.model.ProfileModel
import com.example.xone.navigation.Navigator

class ProfileController(
    private val context: Context,
    private val navigator: Navigator
) {
    var model by mutableStateOf(
        ProfileModel(
            name = LoginController.getUserData()?.name ?: "",
            email = LoginController.getUserData()?.email ?: ""
        )
    )
        internal set

    fun onAboutMeClick() {
        // Navigate to About Me screen
    }

    fun onAddressClick() {
        // Navigate to Address screen
    }

    fun onEmergencyContactClick() {
        // Navigate to Emergency Contact screen
    }

    fun onDocumentsClick() {
        navigator.navigateToMyDocuments()
    }

    fun onLogoutClick() {
        // TODO: Implement logout functionality later
        navigator.navigateToLoginScreen()
    }

    fun onBackPressed() {
        navigator.navigateToHome()
    }
} 