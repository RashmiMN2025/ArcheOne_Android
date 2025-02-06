package com.example.xone.controller

import android.content.Context
import com.example.xone.R
import com.example.xone.model.BusinessCardModel
import com.example.xone.navigation.AndroidNavigator

class BusinessCardController(
    private val context: Context,
    private val navigator: AndroidNavigator
) {
    
    val businessCard = BusinessCardModel(
        companyLogo = R.drawable.netcon,
        name = "Annamalai",
        designation = "Graduate Engineer Trainee",
        department = "Delivery",
        email = "annamalai.k@netcon.in",
        phone = "7838971194",
        location = "Bangalore",
        qrCode = "" // Generate QR code string here
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