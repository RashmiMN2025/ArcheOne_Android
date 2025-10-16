package com.archeGlobal.one.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.EmergencyContactModel
import com.archeGlobal.one.navigation.Navigator

class EmergencyContactController(
    private val navigator: Navigator,
) {
    var model by mutableStateOf(
        EmergencyContactModel(
            // Get emergency contact information from user details
            name = OtpVerificationController.getUserData()?.userDetails?.emergency_contact_name ?: "",
            relationship = OtpVerificationController.getUserData()?.userDetails?.emergency_contact_relation ?: "",
            phoneNumber = OtpVerificationController.getUserData()?.userDetails?.emergency_contact ?: "",
        ),
    )

    fun onBackPressed() {
        navigator.navigateToProfile()
    }
}
