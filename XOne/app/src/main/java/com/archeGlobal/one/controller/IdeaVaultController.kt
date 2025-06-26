package com.archeGlobal.one.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.AboutMeModel
import com.archeGlobal.one.navigation.Navigator

class IdeaVaultController(
    private val navigator: Navigator
) {
    var employeeData by mutableStateOf(
        AboutMeModel(
            name = OtpVerificationController.getUserData()?.name ?: "",
            email = OtpVerificationController.getUserData()?.email ?: ""
        )
    )

    fun onBackPressed() {
        navigator.navigateToHome()
    }
}
