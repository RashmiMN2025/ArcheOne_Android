package com.example.xone.controller

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.xone.model.AssetModel
import com.example.xone.navigation.Navigator
import com.example.xone.network.RetrofitClient
import com.example.xone.model.AssetDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssetController(
    private val context: Context,
    private val navigator: Navigator
) {
    var model by mutableStateOf(AssetModel(isLoading = true))
        private set

    init {
        val employeeId = LoginController.getUserData()?.employeeId?.trim() ?: ""
        if (employeeId.isNotBlank()) {
            fetchAssetDetails("nt$employeeId")
        }
    }

    private fun fetchAssetDetails(employeeId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getAssetDetails(employeeId).execute()
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.details?.isNotEmpty() == true) {
                        val asset = response.body()!!.details[0]
                        model = model.copy(
                            name = asset.username,
                            employeeId = asset.Employee_Code ?: "",
                            mobile = asset.mobile_number,
                            email = asset.mail_id,
                            location = asset.location,
                            assetDetails = AssetDetails(
                                serialNo = asset.serial_number,
                                deviceModel = asset.model,
                                dateOfIssue = asset.date_of_issue,
                                configuration = asset.configuration
                            ),
                            isLoading = false
                        )
                    } else {
                        model = model.copy(
                            error = "Failed to load asset details",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    model = model.copy(
                        error = e.message ?: "Unknown error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }

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