package com.example.xone.controller

import android.content.Context
import android.util.Log
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
import com.example.xone.network.AssetRequest

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
                val request = AssetRequest(employeeId = employeeId)
                val response = RetrofitClient.apiService.getAssetDetails(request).execute()
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        val asset = responseBody.details.firstOrNull()
                        
                        if (asset != null) {
                            model = model.copy(
                                name = responseBody.username,
                                employeeId = responseBody.Employee_Code,
                                mobile = responseBody.mobile_number,
                                email = responseBody.mail_id,
                                location = responseBody.location,
                                department = responseBody.department,
                                designation = responseBody.designation,
                                assetDetails = AssetDetails(
                                    serialNo = asset.serial_number,
                                    deviceModel = asset.model,
                                    dateOfIssue = formatDate(asset.date_of_issue),
                                    configuration = asset.configuration,
                                    assetType = asset.asset_type,
                                    purchaseDate = formatDate(asset.purchase_date ?: "")
                                ),
                                isLoading = false
                            )
                        } else {
                            model = model.copy(
                                error = "No asset details found",
                                isLoading = false
                            )
                        }
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

    // Helper function to format Excel date number to readable date
    private fun formatDate(excelDate: String): String {
        return try {
            val days = excelDate.toDouble().toInt()
            val calendar = java.util.Calendar.getInstance()
            calendar.set(1900, 0, 1) // Excel date system starts from 1900-01-01
            calendar.add(java.util.Calendar.DAY_OF_YEAR, days - 2) // Subtract 2 to account for Excel's date system
            val dateFormat = java.text.SimpleDateFormat("dd-MMM-yy", java.util.Locale.US)
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            excelDate // Return original string if parsing fails
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