package com.archeGlobal.one.controller

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.AssetModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.model.AssetDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.archeGlobal.one.network.AssetRequest
import com.archeGlobal.one.model.SOSRequest

class AssetController(
    private val context: Context,
    private val navigator: Navigator
) {
    var model by mutableStateOf(AssetModel(isLoading = true))
        private set

    init {
        val employeeId = OtpVerificationController.getUserData()?.employeeId?.trim() ?: ""
        if (employeeId.isNotBlank()) {
            fetchAssetDetails("$employeeId")
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

        // Get user data from LoginController
        val userData = OtpVerificationController.getUserData()
        if (userData == null) {
            Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Create SOS request
        val request = SOSRequest(
            name = userData.name,
            email = userData.email,
            mobile = userData.mobile,
            category = "Technical Issue", // Always use Technical Issue for asset concerns
            query = model.issueDescription
        )

        // Launch coroutine to make API call
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.submitSOS(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Issue reported successfully", Toast.LENGTH_SHORT).show()
                        // Clear the issue description and navigate back
                        model = model.copy(issueDescription = "")
                        navigator.navigateToHome()
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                        Toast.makeText(context, "Failed to submit issue: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onBackPressed() {
        navigator.navigateToHome()
    }
} 