package com.archeGlobal.one.controller

import com.archeGlobal.one.model.AboutMeModel
import com.archeGlobal.one.navigation.Navigator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AboutMeController(
    private val navigator: Navigator
) {
    var model by mutableStateOf(AboutMeModel(
        // Basic Information - these are directly available
        name = OtpVerificationController.getUserData()?.name ?: "",
        email = OtpVerificationController.getUserData()?.email ?: "",
        mobile = OtpVerificationController.getUserData()?.mobile ?: "",
        employeeId = OtpVerificationController.getUserData()?.employeeId ?: "",
        
        // Personal Details - from userDetails
        panNumber = OtpVerificationController.getUserData()?.userDetails?.pan ?: "",
        uanNumber = OtpVerificationController.getUserData()?.userDetails?.uan ?: "",
        bloodGroup = OtpVerificationController.getUserData()?.userDetails?.blood_group ?: "",
        
        // Reporting Structure - from userDetails
        reportingManager = OtpVerificationController.getUserData()?.userDetails?.reporting_manager ?: "",
        divisionalHead = OtpVerificationController.getUserData()?.userDetails?.divisional_head ?: "",
        
        // Work Information - these are available
        department = OtpVerificationController.getUserData()?.department ?: "",
        designation = OtpVerificationController.getUserData()?.designation ?: "",
        location = OtpVerificationController.getUserData()?.location ?: ""
    ))
    
    fun onBackPressed() {
        navigator.navigateToHome()
    }
} 