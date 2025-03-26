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
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.utils.UserDataManager
import android.util.Log

class AssetController(
    private val context: Context,
    private val navigator: Navigator
) {
    var model by mutableStateOf(AssetModel(isLoading = true))
        private set

    init {
        // Get user data and asset details from UserDataManager instead of making API call
        loadAssetDetails()
    }
    
    private fun loadAssetDetails() {
        val userData = OtpVerificationController.getUserData()
        val assetDetails = UserDataManager.getInstance(context).getAssetDetails()
        
        if (userData != null) {
            // If we have user data but no asset details
            if (assetDetails.isNullOrEmpty()) {
                model = model.copy(
                    name = userData.name,
                    employeeId = userData.employeeId,
                    mobile = userData.mobile,
                    email = userData.email,
                    location = userData.location,
                    department = userData.department,
                    designation = userData.designation,
                    error = "No asset details found",
                    isLoading = false
                )
            } else {
                // We have both user data and asset details
                val asset = assetDetails.firstOrNull()
                if (asset != null) {
                    model = model.copy(
                        name = userData.name,
                        employeeId = userData.employeeId,
                        mobile = userData.mobile,
                        email = userData.email,
                        location = userData.location,
                        department = userData.department,
                        designation = userData.designation,
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
                        name = userData.name,
                        employeeId = userData.employeeId,
                        mobile = userData.mobile,
                        email = userData.email,
                        location = userData.location,
                        department = userData.department,
                        designation = userData.designation,
                        error = "No asset details found",
                        isLoading = false
                    )
                }
            }
        } else {
            model = model.copy(
                error = "User data not found",
                isLoading = false
            )
        }
    }

    // Helper function to format date string
    private fun formatDate(dateStr: String): String {
        // If the date is already in a readable format (e.g., "07-Aug-24"), return it as is
        if (dateStr.contains("-") && !dateStr.matches(Regex("\\d+"))) {
            return dateStr
        }
        
        // Otherwise, try to parse it as an Excel date number
        return try {
            val days = dateStr.toDouble().toInt()
            val calendar = java.util.Calendar.getInstance()
            calendar.set(1900, 0, 1) // Excel date system starts from 1900-01-01
            calendar.add(java.util.Calendar.DAY_OF_YEAR, days - 2) // Subtract 2 to account for Excel's date system
            val dateFormat = java.text.SimpleDateFormat("dd-MMM-yy", java.util.Locale.US)
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            dateStr // Return original string if parsing fails
        }
    }

    fun onIssueDescriptionChange(description: String) {
        // Log both before and after state for debugging
        val oldDesc = model.issueDescription
        Log.d("AssetController", "Updating description: '$oldDesc' -> '$description'")
        
        // Update the model with the new description
        model = model.copy(issueDescription = description)
        
        // Verify the update was successful
        Log.d("AssetController", "Description updated: '${model.issueDescription}'")
    }

    fun onSubmitIssue() {
        Log.d("AssetController", "==========================================")
        Log.d("AssetController", "SUBMIT ISSUE CALLED")
        Log.d("AssetController", "Current issue description: '${model.issueDescription}'")
        Log.d("AssetController", "Description length: ${model.issueDescription.length}")
        Log.d("AssetController", "Description chars: ${model.issueDescription.toCharArray().joinToString { "'$it' (${it.code})" }}")
        Log.d("AssetController", "==========================================")
        
        // Check if the description is blank after trimming whitespace
        val trimmedDescription = model.issueDescription.trim()
        if (trimmedDescription.isBlank()) {
            Log.d("AssetController", "Issue description is blank after trimming - showing toast")
            Toast.makeText(context, "Please describe your issue", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user data from OtpVerificationController
        val userData = OtpVerificationController.getUserData()
        if (userData == null) {
            Log.d("AssetController", "User data is null - showing toast")
            Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Get email and mobile from userData
        val email = userData.email
        val mobile = userData.mobile
        val name = userData.name
        val description = trimmedDescription // Trim any whitespace

        Log.d("AssetController", "Preparing to submit issue:")
        Log.d("AssetController", "- Name: $name")
        Log.d("AssetController", "- Email: $email")
        Log.d("AssetController", "- Mobile: $mobile")
        Log.d("AssetController", "- Description: '$description'")

        // Create SOS request with "Other" category like in the RaiseConcernScreen
        val request = SOSRequest(
            name = name,
            email = email,
            mobile = mobile,
            category = "Other", // Using "Other" category as requested
            query = description,
            description = "" // Add missing parameter with empty string as default
        )
        
        Log.d("AssetController", "Created SOS request: $request")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AssetController", "Sending SOS request")
                // Use submitSOS like in RaiseConcernScreen instead of createSOSRequest
                val response = RetrofitClient.apiService.submitSOS(request)
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            Log.d("AssetController", "SOS request successful: $responseBody")
                            Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("AssetController", "SOS request successful but no response body")
                            Toast.makeText(context, "Issue reported successfully", Toast.LENGTH_SHORT).show()
                        }
                        // Clear description after successful submission
                        model = model.copy(issueDescription = "")
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                        Log.d("AssetController", "SOS request failed: $errorMessage")
                        Toast.makeText(context, "Failed to report issue: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("AssetController", "Exception during SOS request", e)
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