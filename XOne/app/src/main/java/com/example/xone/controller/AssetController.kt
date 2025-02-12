package com.example.xone.controller

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.xone.model.AssetModel
import com.example.xone.navigation.Navigator

class AssetController(
    private val context: Context,
    private val navigator: Navigator
) {
    var model by mutableStateOf(AssetModel(
        name = LoginController.getUserData()?.name ?: "",
        employeeId = LoginController.getUserData()?.employeeId ?: "",
        mobile = LoginController.getUserData()?.mobile ?: "",
        email = LoginController.getUserData()?.email ?: "",
        location = LoginController.getUserData()?.location ?: "Bangalore"
    ))
        private set

    fun onIssueDescriptionChange(description: String) {
        model = model.copy(issueDescription = description)
    }

    fun onSubmitIssue() {
        if (model.issueDescription.isBlank()) {
            Toast.makeText(context, "Please describe your issue", Toast.LENGTH_SHORT).show()
            return
        }
        // TODO: Implement API call to submit issue
        Toast.makeText(context, "Issue submitted successfully", Toast.LENGTH_SHORT).show()
        navigator.navigateToHome()
    }

    fun onBackPressed() {
        navigator.navigateToHome()
    }
} 