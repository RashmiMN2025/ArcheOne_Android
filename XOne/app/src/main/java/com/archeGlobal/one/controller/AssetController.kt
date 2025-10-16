package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.AssetActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.TrackTicketsActivity
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.model.AssetDetails
import com.archeGlobal.one.model.AssetModel
import com.archeGlobal.one.model.EncryptedSOSResponse
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.model.SOSResponse
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.EncryptedAPIService
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssetController(
    private val context: Context,
    private val navigator: Navigator,
) {
    var model by mutableStateOf(AssetModel(isLoading = true))
        private set

    private var isDataLoaded = false
    private val encryptedApiService = EncryptedAPIService.getInstance(context)

    init {
        Log.d("AssetController", "AssetController created - data will be loaded on first access")
    }

    /**
     * Call this method when the Asset service is actually accessed by the user
     * This ensures data is processed only when needed
     */
    fun onServiceAccessed() {
        Log.d("AssetController", "Asset service accessed - processing data")
        if (!isDataLoaded) {
            loadAssetDetails()
            isDataLoaded = true
        } else {
            Log.d("AssetController", "Asset data already loaded, skipping processing")
        }
    }

    private fun loadAssetDetails() {
        val userData = OtpVerificationController.getUserData()
        val assetDetails = UserDataManager.getInstance(context).getAssetDetails()

        if (userData != null) {
            // If we have user data but no asset details
            if (assetDetails.isNullOrEmpty()) {
                model =
                    model.copy(
                        name = userData.name,
                        employeeId = userData.employeeId,
                        mobile = userData.mobile,
                        email = userData.email,
                        location = userData.location,
                        department = userData.department,
                        designation = userData.designation,
                        error = "No asset details found",
                        isLoading = false,
                    )
            } else {
                // Map the asset details to a list of AssetDetails objects
                val assetDetailsList =
                    assetDetails.map { asset ->
                        Log.d("AssetController", "Mapping asset: hostname=${asset.hostname}")
                        AssetDetails(
                            serialNo = asset.serial_number,
                            deviceModel = asset.model,
                            dateOfIssue = formatDate(asset.date_of_issue),
                            configuration = asset.configuration,
                            assetType = asset.asset_type,
                            purchaseDate = formatDate(asset.purchase_date ?: ""),
                            hostName = asset.hostname ?: "",
                        )
                    }

                // Update the model with the list of asset details
                model =
                    model.copy(
                        name = userData.name,
                        employeeId = userData.employeeId,
                        mobile = userData.mobile,
                        email = userData.email,
                        location = userData.location,
                        department = userData.department,
                        designation = userData.designation,
                        assetDetails = assetDetailsList, // Assign the list here
                        isLoading = false,
                    )
            }
        } else {
            model =
                model.copy(
                    error = "User data not found",
                    isLoading = false,
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
        Log.d("AssetController", "SUBMIT ENCRYPTED ASSET ISSUE CALLED")
        Log.d("AssetController", "Current issue description: '${model.issueDescription}'")
        Log.d("AssetController", "Description length: ${model.issueDescription.length}")
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
        val email = userData.email ?: ""
        val mobile = userData.mobile ?: ""
        val name = userData.name ?: ""
        val description = trimmedDescription

        Log.d("AssetController", "Preparing to submit encrypted asset issue:")
        Log.d("AssetController", "- Name: $name")
        Log.d("AssetController", "- Email: $email")
        Log.d("AssetController", "- Mobile: $mobile")
        Log.d("AssetController", "- Description: '$description'")

        // Create SOS request with "Technical Issue" category for asset issues
        val request =
            SOSRequest(
                name = name,
                email = email,
                mobile = mobile,
                category = "Asset Related Issue",
                query = description,
                anonymous = false, // Asset issues are not anonymous since they're tied to specific assets
            )

        Log.d("AssetController", "Created encrypted SOS request: $request")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("AssetController", "Sending encrypted asset issue request")

                // Use encrypted API service similar to SOSController
                val response =
                    encryptedApiService.encryptedRequest(
                        endpoint = "sos",
                        method = "POST",
                        body = request,
                        responseClass = EncryptedSOSResponse::class.java,
                        withAuthHeader = true,
                    )

                // Convert EncryptedSOSResponse to SOSResponse
                val sosResponse =
                    SOSResponse(
                        status = response.status == 200,
                        message = response.message,
                    )

                withContext(Dispatchers.Main) {
                    if (sosResponse.status) {
                        Log.d("AssetController", "Encrypted asset issue submitted successfully")
                        Toast.makeText(context, "Asset issue reported successfully with encryption", Toast.LENGTH_SHORT).show()
                        // Clear description after successful submission
                        model = model.copy(issueDescription = "")
                    } else {
                        Toast.makeText(context, "Unable to submit asset issue: ${sosResponse.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: APIError) {
                Log.e("AssetController", "API Error submitting encrypted asset issue: ${e.message}")
                withContext(Dispatchers.Main) {
                    val errorMessage =
                        when (e) {
                            is APIError.Unauthorized -> "Authentication error. Please login again."
                            is APIError.BadRequest -> "Invalid request. Please check your information."
                            is APIError.ServerError -> "Server error. Please try again later."
                            is APIError.EncryptionFailed -> "Security error. Please try again."
                            is APIError.DecryptionFailed -> "Security error. Please try again."
                            is APIError.SSLPinningFailed -> "Network security error. Please try again."
                            is APIError.DecodingError -> "Response processing error. Please try again."
                            else -> "Unable to submit asset issue. Please try again."
                        }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AssetController", "Exception during encrypted asset issue submission", e)
                withContext(Dispatchers.Main) {
                    Toast
                        .makeText(
                            context,
                            "Unable to submit asset issue. Please check your internet connection and try again.",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }

    fun onBackPressed() {
        (context as? AssetActivity)?.finishWithAnimation()
    }

    fun navigateToTrackTickets() {
        // Navigate directly to HomeActivity with track_tickets as the target
        val intent =
            Intent(context, TrackTicketsActivity::class.java).apply {
                putExtra("ticketCategory", "Asset Related Issue")
                putExtra("source", "asset")
            }
        Log.d("AssetController", "Starting TrackTicketsActivity with ticketCategory=Asset Related Issue")
        context.startActivity(intent)
        // Apply forward animation
        (context as? AssetActivity)?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        // Finish AssetActivity to prevent going back to itS
    }
}
