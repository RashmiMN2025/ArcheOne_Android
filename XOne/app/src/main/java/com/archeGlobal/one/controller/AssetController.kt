package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.room.util.copy
import com.archeGlobal.one.AssetActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.TrackTicketsActivity
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.model.AssetDetails
import com.archeGlobal.one.model.AssetModel
import com.archeGlobal.one.model.AssetV2Request
import com.archeGlobal.one.model.EncryptedSOSResponse
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.model.SOSResponse
import com.archeGlobal.one.model.SelfTagAssetRequest
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.network.EncryptedAPIService
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.coroutines.withContext
import kotlin.jvm.java

class AssetController(
    private val context: Context,
    private val navigator: Navigator,
) {
    var model by mutableStateOf(AssetModel(isLoading = true))
        private set

    private var isDataLoaded = false
    private val apiService = RetrofitClient.apiService
    private val encryptedApiService = EncryptedAPIService.getInstance(context)

    private var _showSelfTagDialog by mutableStateOf(false)
    private var _selectedAssetType by mutableStateOf("")
    private var _modelNumberInput by mutableStateOf("")
    private var _serialNumberInput by mutableStateOf("")
    private var _selfTagSubmitting by mutableStateOf(false)
    private var _selfTagResult by mutableStateOf<String?>(null)

    // Public access with recomposition support
    var showSelfTagDialog: Boolean
        get() = _showSelfTagDialog
        private set(value) { _showSelfTagDialog = value }

    var selectedAssetType: String
        get() = _selectedAssetType
        set(value) { _selectedAssetType = value }

    var modelNumberInput: String
        get() = _modelNumberInput
        set(value) { _modelNumberInput = value }

    var serialNumberInput: String
        get() = _serialNumberInput
        set(value) { _serialNumberInput = value }

    var selfTagSubmitting: Boolean
        get() = _selfTagSubmitting
        private set(value) { _selfTagSubmitting = value }

    var selfTagResult: String?
        get() = _selfTagResult
        private set(value) { _selfTagResult = value }

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
        if (userData == null) {
            model = model.copy(error = "User data not found", isLoading = false)
            return
        }

        val reportingToFromDetails = userData.userDetails?.reporting_manager?.takeIf { it.isNotBlank() }
            ?: "N/A"

        // ---- 1. Fill the static user fields (Employee ID, Location, Reporting To…) ----
        model = model.copy(
            name = userData.name,
            employeeId = userData.employeeId,
            mobile = userData.mobile,
            email = userData.email,
            location = userData.location,
            department = userData.department,
            designation = userData.designation,
            reportingTo = reportingToFromDetails ,
            isLoading = true
        )

        // ---- 2. Call the new API -------------------------------------------------
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = AssetV2Request(employeeCode = userData.employeeId)
                val response = apiService.getAssetsV2(request)

                withContext(Dispatchers.Main) {
                    if (response.success && response.data.isNotEmpty()) {
                        val group = response.data[0]                     // one employee
                        val assets = group.assets.map { api ->
                            AssetDetails(
                                assetType = api.assetType,
                                oldAssetId = api.oldAssetId ?: "",
                                newAssetId = api.newAssetId ?: "",
                                purchaseDate = api.purchaseDate,
                                modelNumber = api.modelNumber,
                                configuration = api.configuration,
                                reportingTo = api.reportingTo ?: "",
                                divisionalHead = api.divisionalHead ?: "",
                                warrantyStart = api.warrantyStart ?: "",
                                warrantyEnd = api.warrantyEnd ?: "",
                                dateOfIssue = api.dateOfIssue,
                                serialNumber = api.serialNumber,
                                isTagged = api.isTagged,
                                division = api.division ?: "",
                                location = api.location ?: ""
                            )
                        }

                        model = model.copy(
                            assetDetails = assets,
                            isLoading = false
                        )
                    } else {
                        model = model.copy(
                            error = "No asset details found",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AssetController", "API error", e)
                withContext(Dispatchers.Main) {
                    model = model.copy(
                        error = "Failed to load assets",
                        isLoading = false
                    )
                }
            }
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

    fun showSelfTagDialog() {
        selectedAssetType = ""
        modelNumberInput = ""
        serialNumberInput = ""
        selfTagResult = null
        showSelfTagDialog = true
    }

    fun hideSelfTagDialog() {
        showSelfTagDialog = false
    }

    /** Called from UI when the user presses **Submit** */
    fun submitSelfTagAsset() {
        val user = OtpVerificationController.getUserData() ?: run {
            Toast.makeText(context, "User data not available", Toast.LENGTH_SHORT).show()
            return
        }

        // ---- Validation -------------------------------------------------
        if (selectedAssetType.isBlank() || modelNumberInput.isBlank() || serialNumberInput.isBlank()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        selfTagSubmitting = true
        selfTagResult = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiDivision = if (model.assetDetails.isNotEmpty()) {
                    model.assetDetails[0].division.takeIf { it.isNotBlank() }
                } else null

                val request = SelfTagAssetRequest(
                    assetType = selectedAssetType,
                    modelNumber = modelNumberInput,
                    serialNumber = serialNumberInput,
                    employeeCode = user.employeeId,
                    username = user.name,
                    mailId = user.email,
                    mobileNumber = user.mobile,
                    location = user.location,
                    designation = user.designation,
                    division = apiDivision ?: "",
                    department = user.department,
                    dateOfIssue = "",                         // can be current date if needed
                    reportingTo = user.userDetails?.reporting_manager ?: "",
                    divisionalHead = user.userDetails?.divisional_head ?: ""
                )

                val response = apiService.selfTagAsset(request)

                withContext(Dispatchers.Main) {
                    selfTagSubmitting = false
                    if (response.isSuccessful && response.body()?.success == true) {
                        selfTagResult = response.body()?.message
                            ?: "Self-tag request submitted successfully."
                        // optional: refresh asset list after a short delay
                        CoroutineScope(Dispatchers.IO).launch {
                            delay(800)
                            loadAssetDetails()
                        }
                    } else {
                        selfTagResult = response.body()?.message
                            ?: "Failed to submit self-tag."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    selfTagSubmitting = false
                    selfTagResult = "Network error. Please try again."
                    Log.e("AssetController", "Self-tag asset error", e)
                }
            }
        }
    }
}
