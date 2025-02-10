package com.example.xone.controller

import android.content.Context
import com.example.xone.R
import com.example.xone.model.BusinessCardModel
import com.example.xone.navigation.AndroidNavigator

class BusinessCardController(
    private val context: Context,
    private val navigator: AndroidNavigator
) {
    // Get user data from LoginController
    val businessCard = LoginController.getUserData()?.let { userData ->
        BusinessCardModel(
            companyLogo = R.drawable.arche,
            name = userData.name,
            designation = userData.designation,
            department = userData.department,
            email = userData.email,
            phone = userData.mobile,
            location = "Bangalore", // Note: Location is not currently part of UserData
            qrCode = "" // Generate QR code string here
        )
    } ?: BusinessCardModel(
        // Fallback default values if userData is null
        companyLogo = R.drawable.arche,
        name = "",
        designation = "",
        department = "",
        email = "",
        phone = "",
        location = "",
        qrCode = ""
    )
    
    fun onDownloadCard() {
        // Implement download functionality
    }
    
    fun onShareCard() {
        // Implement share functionality using context
    }
    
    fun onBackPressed() {
        navigator.navigateToHome()  // Navigate back to home screen
    }
} 