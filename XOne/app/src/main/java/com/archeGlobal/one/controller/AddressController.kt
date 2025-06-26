package com.archeGlobal.one.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.AddressModel
import com.archeGlobal.one.navigation.Navigator

class AddressController(
    private val navigator: Navigator
) {
    var model by mutableStateOf(
        AddressModel(
            // Get address information from user details
            presentAddress = OtpVerificationController.getUserData()?.userDetails?.temporary_address ?: "",
            permanentAddress = OtpVerificationController.getUserData()?.userDetails?.permanent_address ?: ""
        )
    )

    fun onBackPressed() {
        navigator.navigateToProfile()
    }
}
