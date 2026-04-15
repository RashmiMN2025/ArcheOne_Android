package com.archeGlobal.one.ui.screens

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.graphicsLayer
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HelpDeskController
import com.archeGlobal.one.controller.SOSController
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.model.DynamicFormFieldValue
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.ui.components.DynamicFormFieldComponent
import com.archeGlobal.one.ui.components.EmployeeDetailsSection
import com.archeGlobal.one.ui.components.validateDynamicField
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull

@Composable
fun RaiseConcernScreen(
    onBackPressed: () -> Unit,
    title: String = "Raise a Concern",
    source: String = "helpdesk", // Add source parameter to track where we came from
    prefilledCategory: String? = null, // FAQ category to prefill and lock
    prefilledSubcategory: String? = null, // FAQ subcategory to prefill and lock
    prefilledFaqId: String? = null, // FAQ ID to check if dynamic fields should be loaded
    helpDeskController: HelpDeskController? = null, // Pass controller from HomeActivity for FAQ lookup
    onNavigateToTrackTickets: ((String) -> Unit)? = null, // Add navigation callback for track tickets
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Get user data and controllers
    val userDataManager = remember { UserDataManager.getInstance(context) }
    val userData = remember { userDataManager.getUserData() }
    val sosController = remember { SOSController(context.applicationContext as Application) }
    val localHelpDeskController = remember { helpDeskController ?: HelpDeskController(context) }

    // Determine if this is a help desk ticket or SOS concern
    val isHelpDeskTicket = title.contains("Ticket", ignoreCase = true)

    // Form state
    var selectedCategory by remember { mutableStateOf<String?>(prefilledCategory) }
    var selectedSubcategory by remember { mutableStateOf<String?>(prefilledSubcategory) }
    var issueDescription by remember { mutableStateOf("") }

    // Debug logging for initial state
    LaunchedEffect(Unit) {
        Log.d("RaiseConcernScreen", "=== INITIAL STATE ===")
        Log.d("RaiseConcernScreen", "Title: '$title'")
        Log.d("RaiseConcernScreen", "Source: '$source'")
        Log.d("RaiseConcernScreen", "Prefilled Category: '$prefilledCategory'")
        Log.d("RaiseConcernScreen", "Prefilled Subcategory: '$prefilledSubcategory'")
        Log.d("RaiseConcernScreen", "Selected Category: '$selectedCategory'")
        Log.d("RaiseConcernScreen", "Selected Subcategory: '$selectedSubcategory'")
        Log.d("RaiseConcernScreen", "Is Help Desk Ticket: $isHelpDeskTicket")
        Log.d("RaiseConcernScreen", "===================")
    }
    var expanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    val isCategoryLocked = prefilledCategory != null
    val isSubcategoryLocked = prefilledSubcategory != null
    var isSubmitting by remember { mutableStateOf(false) }
    var showAnonymousDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(20) }
    var showDynamicFormSuccessDialog by remember { mutableStateOf(false) }
    var successTicketId by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Dynamic form fields state
    val dynamicFormFields by localHelpDeskController.dynamicFormFields.collectAsState()
    val dynamicFieldsLoading by localHelpDeskController.dynamicFieldsLoading.collectAsState()
    val dynamicFieldsError by localHelpDeskController.dynamicFieldsError.collectAsState()
    var dynamicFieldValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var dynamicFieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Get categories based on context
    val helpDeskModel by localHelpDeskController.model.collectAsState()
    val categories =
        if (isHelpDeskTicket) {
            // Extract categories from help desk FAQ data in their original order
            val helpDeskCategories =
                helpDeskModel.faqItems
                    .map { it.category }
                    .distinct()
                    .filter { it != "General" && it != "Other Issues" }
            // Preserve original order by removing .sorted()

            // Add help desk specific categories at the end
            helpDeskCategories + listOf("Other Issue")
        } else {
            // SOS categories
            listOf(
                "Medical Emergency",
                "Fire Safety",
                "Security Risk",
                "Workplace Safety",
                "Non-Compliance",
                "PoSH",
                "Other Issue",
            )
        }

    val assetSubcategories =
        listOf(
            "Asset Malfunction",
            "Asset Not Allocated",
            "Asset Return",
            "Asset Transfer",
            "Incorrect Asset Details",
            "Incorrect Asset Tagging",
            "Other Issue",
        )

    // Subcategory mapping - extract from FAQ data for help desk, use hardcoded for SOS
    val subcategoryMap =
        if (isHelpDeskTicket) {
            // Extract subcategories from FAQ data by accessing the original FAQ structure
            val subcategoriesFromFAQ = mutableMapOf<String, MutableSet<String>>()

            // Get the original FAQ data from UserDataManager to access FAQAnswer.cat fields
            val userDataManager = UserDataManager.getInstance(context)
            val originalFaqData = userDataManager.getFAQData()

            originalFaqData?.forEach { faqCategory ->
                // Skip the default categories
                if (faqCategory.title != "General" && faqCategory.title != "Other Issues") {
                    val subcategorySet = subcategoriesFromFAQ.getOrPut(faqCategory.title) { mutableSetOf() }

                    // Debug logging to see the actual FAQ structure
                    Log.d("RaiseConcern", "Processing FAQ Category: ${faqCategory.title}")

                    // Use FAQ questions as subcategories (each question represents a subcategory)
                    faqCategory.items.forEach { faqItem ->
                        Log.d("RaiseConcern", "  Adding FAQ question as subcategory: ${faqItem.question}")
                        subcategorySet.add(faqItem.question)
                    }

                    Log.d("RaiseConcern", "  Final subcategories for ${faqCategory.title}: ${subcategorySet.toList()}")
                }
            }

            // Convert to Map<String, List<String>> without adding "Other" options
            val finalSubcategoryMap =
                subcategoriesFromFAQ
                    .mapValues { (_, subcategories) ->
                        subcategories.toList()
                            .filter { it.isNotBlank() && it != "undefined" }
                            .sorted()
                    }.toMutableMap()
                    .apply {
                        // Add default subcategories for "Other Issue" category
                        put("Other Issue", listOf("General Query", "Feature Request", "Training", "Documentation", "Other"))
                    }

            // Debug logging for final subcategory map
            Log.d("RaiseConcern", "Final subcategory map:")
            finalSubcategoryMap.forEach { (category, subcategories) ->
                Log.d("RaiseConcern", "  $category: ${subcategories.joinToString(", ")}")
            }

            finalSubcategoryMap
        } else {
            // SOS categories - keep hardcoded as they don't come from FAQ API
            mapOf(
                "Medical Emergency" to listOf("Heart Attack", "Stroke", "Injury", "Breathing Issues", "Unconscious", "Other Medical"),
                "Fire Safety" to listOf("Fire Outbreak", "Smoke Detection", "Evacuation", "Fire Equipment", "Other Fire Safety"),
                "Security Risk" to listOf("Unauthorized Access", "Theft", "Violence", "Threat", "Suspicious Activity", "Other Security"),
                "Workplace Safety" to listOf("Accident", "Hazardous Conditions", "Equipment Failure", "Chemical Spill", "Other Safety"),
                "Non-Compliance" to listOf("Policy Violation", "Regulatory Issue", "Safety Standards", "Other Compliance"),
                "PoSH" to listOf("Sexual Harassment", "Discrimination", "Inappropriate Behavior", "Other PoSH"),
                "Other Issue" to listOf("General Concern", "Anonymous Report", "Other"),
            )
        }

    // Get available subcategories for selected category
    val availableSubcategories =
        if (source == "asset") {
            assetSubcategories.filter { it.isNotBlank() && it != "undefined" }
        } else {
            selectedCategory?.let { subcategoryMap[it] }?.filter { it.isNotBlank() && it != "undefined" } ?: emptyList()
        }

    // Debug logging for available subcategories
    LaunchedEffect(selectedCategory, availableSubcategories) {
        Log.d("RaiseConcernScreen", "=== SUBCATEGORY DEBUG ===")
        Log.d("RaiseConcernScreen", "Selected Category: '$selectedCategory'")
        Log.d("RaiseConcernScreen", "Available Subcategories: $availableSubcategories")
        Log.d("RaiseConcernScreen", "Selected Subcategory: '$selectedSubcategory'")
        Log.d("RaiseConcernScreen", "Is subcategory in list: ${availableSubcategories.contains(selectedSubcategory)}")
        Log.d("RaiseConcernScreen", "========================")
    }

    // Reset subcategory when category changes (unless it's pre-filled)
    LaunchedEffect(selectedCategory) {
        if (!isSubcategoryLocked) {
            selectedSubcategory = null
        }
    }

    // Load dynamic form fields when BOTH category AND subcategory are selected (only for help desk tickets, NOT for asset tickets)
    LaunchedEffect(selectedCategory, selectedSubcategory, prefilledFaqId) {
        if (source != "asset" && isHelpDeskTicket && selectedCategory != null && selectedSubcategory != null) {
            // Only load dynamic fields when both category and subcategory are selected
            Log.d("RaiseConcernScreen", "Loading dynamic fields for category: $selectedCategory, subcategory: $selectedSubcategory")

            // Clear previous field values and errors
            dynamicFieldValues = emptyMap()
            dynamicFieldErrors = emptyMap()

            // Load dynamic fields based on category and subcategory
            localHelpDeskController.loadDynamicFormFields(
                category = selectedCategory!!,
                subcategory = selectedSubcategory,
            )
        } else if (selectedCategory == null || selectedSubcategory == null) {
            // Clear dynamic fields when category or subcategory is not selected
            localHelpDeskController.clearDynamicFormFields()
            dynamicFieldValues = emptyMap()
            dynamicFieldErrors = emptyMap()
        }
    }

    // Submit function for dynamic form tickets (defined first so it can be called by submitHelpDeskTicket)
    suspend fun submitDynamicFormTicket(user: com.archeGlobal.one.model.UserData) {
        Log.d("RaiseConcern", ">>> submitDynamicFormTicket() entered")
        Log.d("RaiseConcern", "  - User Name: ${user.name}")
        Log.d("RaiseConcern", "  - User Email: ${user.email}")
        Log.d("RaiseConcern", "  - Manager Email: ${user.userDetails?.reporting_manager_mail}")
        Log.d("RaiseConcern", "  - Dynamic Fields to validate: ${dynamicFormFields.size}")

        // Validate dynamic form fields
        Log.d("RaiseConcern", "Starting field validation...")
        val fieldValidationErrors = mutableMapOf<String, String>()
        dynamicFormFields.forEach { field ->
            val value = dynamicFieldValues[field.fieldId] ?: ""
            Log.d("RaiseConcern", "Validating field: ${field.fieldName} (${field.fieldId})")
            Log.d("RaiseConcern", "  - Value: '$value' (length: ${value.length})")
            Log.d("RaiseConcern", "  - Required: ${field.isRequired}")
            Log.d("RaiseConcern", "  - Min Length: ${field.minLength}")
            Log.d("RaiseConcern", "  - Max Length: ${field.maxLength}")
            Log.d("RaiseConcern", "  - Validation Regex: ${field.validationRegex}")

            val error = validateDynamicField(field, value)
            if (error != null) {
                Log.e("RaiseConcern", "  ✗ VALIDATION FAILED: $error")
                fieldValidationErrors[field.fieldId] = error
            } else {
                Log.d("RaiseConcern", "  ✓ Validation passed")
            }
        }

        if (fieldValidationErrors.isNotEmpty()) {
            Log.e("RaiseConcern", "VALIDATION ERRORS FOUND:")
            fieldValidationErrors.forEach { (fieldId, error) ->
                val fieldName = dynamicFormFields.find { it.fieldId == fieldId }?.fieldName ?: "Unknown"
                Log.e("RaiseConcern", "  - $fieldName ($fieldId): $error")
            }
            dynamicFieldErrors = fieldValidationErrors
            Toast
                .makeText(
                    context,
                    "Please fill in all required fields correctly",
                    Toast.LENGTH_SHORT,
                ).show()
            isSubmitting = false
            return
        }

        Log.d("RaiseConcern", "✓ All field validations passed")

        // Clear any previous validation errors
        dynamicFieldErrors = emptyMap()

        try {
            // Prepare form details as JSON
            val formDetailsMap = mutableMapOf<String, String>()
            dynamicFormFields.forEach { field ->
                val value = dynamicFieldValues[field.fieldId] ?: ""
                formDetailsMap[field.fieldName] = value
            }
            val formDetailsJson = com.google.gson.Gson().toJson(formDetailsMap)

            // Create multipart request body parts
            val nameBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), user.name ?: "")
            val userEmailBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), user.email ?: "")
            val managerEmailBody = okhttp3.RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                user.userDetails?.reporting_manager_mail ?: "",
            )
            val categoryBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), selectedCategory ?: "")
            val subCategoryBody = okhttp3.RequestBody.create("text/plain".toMediaTypeOrNull(), selectedSubcategory ?: "")
            val queryBody = null // Optional query field
            val formDetailsBody = okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), formDetailsJson)

            Log.d("RaiseConcern", "==============================================")
            Log.d("RaiseConcern", "=== DYNAMIC FORM TICKET SUBMISSION START ===")
            Log.d("RaiseConcern", "==============================================")
            Log.d("RaiseConcern", "API Endpoint: POST /helpdsk/v1/raise-req")
            Log.d("RaiseConcern", "Content-Type: multipart/form-data")
            Log.d("RaiseConcern", "")
            Log.d("RaiseConcern", "Request Parameters:")
            Log.d("RaiseConcern", "  - name: '${user.name}'")
            Log.d("RaiseConcern", "  - userEmail: '${user.email}'")
            Log.d("RaiseConcern", "  - managerEmail: '${user.userDetails?.reporting_manager_mail}'")
            Log.d("RaiseConcern", "  - category: '$selectedCategory'")
            Log.d("RaiseConcern", "  - subCategory: '$selectedSubcategory'")
            Log.d("RaiseConcern", "  - query: null (not used for dynamic forms)")
            Log.d("RaiseConcern", "  - files: null (not implemented)")
            Log.d("RaiseConcern", "")
            Log.d("RaiseConcern", "Form Details JSON:")
            Log.d("RaiseConcern", formDetailsJson)
            Log.d("RaiseConcern", "")
            Log.d("RaiseConcern", "Individual Field Values:")
            dynamicFormFields.forEach { field ->
                val value = dynamicFieldValues[field.fieldId] ?: ""
                Log.d("RaiseConcern", "  - ${field.fieldName} (${field.fieldId}): '$value'")
            }
            Log.d("RaiseConcern", "")
            Log.d("RaiseConcern", "Making API call...")

            // Make API call
            val response = com.archeGlobal.one.network.RetrofitClient.apiService.submitDynamicFormRequest(
                name = nameBody,
                userEmail = userEmailBody,
                managerEmail = managerEmailBody,
                category = categoryBody,
                subCategory = subCategoryBody,
                query = queryBody,
                formDetails = formDetailsBody,
                files = null, // TODO: Add file upload support if needed
            )

            Log.d("RaiseConcern", "API Response received")
            Log.d("RaiseConcern", "  - HTTP Status Code: ${response.code()}")
            Log.d("RaiseConcern", "  - Is Successful: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Log.d("RaiseConcern", "  - Response Status: ${responseBody.status}")
                Log.d("RaiseConcern", "  - Response Message: '${responseBody.message}'")
                Log.d("RaiseConcern", "  - Ticket ID: '${responseBody.ticketId ?: "N/A"}'")

                if (responseBody.status == 200) {
                    Log.d("RaiseConcern", "✓ SUCCESS: Ticket submitted successfully")
                    Log.d("RaiseConcern", "==============================================")

                    // Store ticket ID, message and show success dialog
                    successTicketId = responseBody.ticketId
                    successMessage = responseBody.message
                    isSubmitting = false // Stop loading overlay before showing dialog
                    showDynamicFormSuccessDialog = true

                    // Note: Form will be reset when OK button is clicked
                } else {
                    Log.e("RaiseConcern", "✗ FAILED: Status code indicates failure")
                    Log.e("RaiseConcern", "==============================================")

                    Toast
                        .makeText(
                            context,
                            "Failed to submit request: ${responseBody.message}",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            } else {
                Log.e("RaiseConcern", "✗ ERROR: API call unsuccessful")
                Log.e("RaiseConcern", "  - HTTP Status: ${response.code()}")
                Log.e("RaiseConcern", "  - Error Message: ${response.message()}")
                Log.e("RaiseConcern", "  - Response Body: ${response.errorBody()?.string()}")
                Log.e("RaiseConcern", "==============================================")

                Toast
                    .makeText(
                        context,
                        "Failed to submit request. Please try again.",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        } catch (e: Exception) {
            Log.e("RaiseConcern", "✗ EXCEPTION: Error during dynamic form submission")
            Log.e("RaiseConcern", "  - Exception Type: ${e.javaClass.simpleName}")
            Log.e("RaiseConcern", "  - Exception Message: ${e.message}")
            Log.e("RaiseConcern", "  - Stack Trace:", e)
            Log.e("RaiseConcern", "==============================================")

            Toast
                .makeText(
                    context,
                    "Unable to submit request. Please check your internet connection.",
                    Toast.LENGTH_SHORT,
                ).show()
        } finally {
            isSubmitting = false
        }
    }

    // Submit function for help desk tickets
    suspend fun submitHelpDeskTicket() {
        Log.d("RaiseConcern", "============================================")
        Log.d("RaiseConcern", "submitHelpDeskTicket() called")
        Log.d("RaiseConcern", "  - Selected Category: $selectedCategory")
        Log.d("RaiseConcern", "  - Selected Subcategory: $selectedSubcategory")
        Log.d("RaiseConcern", "  - Available Subcategories: $availableSubcategories")
        Log.d("RaiseConcern", "  - Dynamic Fields Count: ${dynamicFormFields.size}")
        Log.d("RaiseConcern", "  - Is Help Desk Ticket: $isHelpDeskTicket")
        Log.d("RaiseConcern", "  - Source: $source")

        if (selectedCategory == null) {
            Log.e("RaiseConcern", "VALIDATION FAILED: Category is null")
            Toast
                .makeText(
                    context,
                    "Please select a category",
                    Toast.LENGTH_SHORT,
                ).show()
            return
        }

        if (availableSubcategories.isNotEmpty() && selectedSubcategory == null) {
            Log.e("RaiseConcern", "VALIDATION FAILED: Subcategory required but not selected")
            Log.e("RaiseConcern", "  - Available subcategories: $availableSubcategories")
            Toast
                .makeText(
                    context,
                    "Please select a subcategory",
                    Toast.LENGTH_SHORT,
                ).show()
            return
        }

        val user = userData
        if (user == null) {
            Log.e("RaiseConcern", "VALIDATION FAILED: User data is null")
            Toast.makeText(context, "User information not available", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RaiseConcern", "All validations passed")
        isSubmitting = true

        try {
            // Check if this is a dynamic form submission
            if (dynamicFormFields.isNotEmpty()) {
                Log.d("RaiseConcern", "ROUTING: Dynamic form detected - calling submitDynamicFormTicket()")
                // Dynamic form submission - use new API
                submitDynamicFormTicket(user)
            } else {
                Log.d("RaiseConcern", "ROUTING: Non-dynamic form - using existing API")
                // Non-dynamic form submission - use existing API
                if (issueDescription.isBlank()) {
                    Log.e("RaiseConcern", "VALIDATION FAILED: Issue description is blank")
                    Toast
                        .makeText(
                            context,
                            "Please describe your issue",
                            Toast.LENGTH_SHORT,
                        ).show()
                    isSubmitting = false
                    return
                }

                val request =
                    SOSRequest(
                        name = user.name ?: "",
                        email = user.email ?: "",
                        mobile = user.mobile ?: "",
                        category = selectedCategory ?: "Other Issue",
                        subcategory = selectedSubcategory?.takeIf { it.isNotBlank() },
                        query = issueDescription,
                        anonymous = false,
                    )

                Log.d("RaiseConcern", "=== NON-DYNAMIC HELP DESK TICKET SUBMISSION ===")
                Log.d("RaiseConcern", "API Endpoint: submitEncryptedHelpdeskRequest")
                Log.d("RaiseConcern", "Category: '$selectedCategory'")
                Log.d("RaiseConcern", "Subcategory: '$selectedSubcategory'")
                Log.d("RaiseConcern", "Issue Description: '$issueDescription'")

                val result = sosController.submitEncryptedHelpdeskRequest(request)

                result.fold(
                    onSuccess = { response ->
                        if (response.status) {
                            val successMessage =
                                if (source == "asset") {
                                    response.message
                                } else {
                                    "Ticket raised successfully"
                                }

                            Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()

                            // Show timer dialog for helpdesk tickets only
                            showTimerDialog = true
                            timerSeconds = 20
                        } else {
                            val errorMessage =
                                if (source == "asset") {
                                    response.message
                                } else {
                                    "Failed to submit ticket: ${response.message}"
                                }

                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { exception ->
                        val errorMessage =
                            if (source == "asset") {
                                "Unable to submit your concern. Please try again."
                            } else {
                                when (exception) {
                                    is APIError.Unauthorized -> "Authentication error. Please login again."
                                    is APIError.BadRequest -> "Invalid request. Please check your information."
                                    is APIError.ServerError -> exception.message?.takeIf { it.isNotBlank() } ?: "Server error. Please try again later."
                                    is APIError.EncryptionFailed -> "Security error. Please try again."
                                    is APIError.DecryptionFailed -> "Security error. Please try again."
                                    is APIError.SSLPinningFailed -> "Network security error. Please try again."
                                    is APIError.DecodingError -> "Response processing error. Please try again."
                                    else -> "Unable to submit your ticket. Please try again."
                                }
                            }

                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("RaiseConcern", "Error submitting help desk ticket: ${exception.message}")
                    },
                )
            }
        } catch (e: Exception) {
            val exceptionMessage =
                if (source == "asset") {
                    "Unable to submit your concern. Please check your internet connection and try again."
                } else {
                    "Unable to submit your ticket. Please check your internet connection and try again."
                }

            Toast
                .makeText(
                    context,
                    exceptionMessage,
                    Toast.LENGTH_SHORT,
                ).show()
            Log.e("RaiseConcern", "Exception during help desk ticket submission: ${e.message}")
        } finally {
            isSubmitting = false
        }
    }

    // Submit function for SOS concerns (existing functionality)
    suspend fun submitSOSConcern(anonymous: Boolean) {
        if (selectedCategory == null || issueDescription.isBlank()) {
            Toast
                .makeText(
                    context,
                    "Please select a category and describe your issue",
                    Toast.LENGTH_SHORT,
                ).show()
            return
        }

        // SOS concerns don't require subcategory - validation removed

        isSubmitting = true

        try {
            val user = userData
            if (user == null) {
                Toast.makeText(context, "User information not available", Toast.LENGTH_SHORT).show()
                isSubmitting = false
                return
            }

            val request =
                SOSRequest(
                    name = user.name ?: "",
                    email = user.email ?: "",
                    mobile = user.mobile ?: "",
                    category = selectedCategory ?: "Other Issue",
                    subcategory = null, // SOS concerns don't use subcategory
                    query = issueDescription,
                    anonymous = anonymous,
                )

            Log.d("RaiseConcern", "Submitting SOS concern: Category=$selectedCategory, Query=$issueDescription, Anonymous=$anonymous")

            val result = sosController.submitEncryptedSOSRequest(request)

            result.fold(
                onSuccess = { response ->
                    if (response.status) {
                        // Show success message based on anonymous status
                        val message =
                            if (anonymous) {
                                "Query submitted Successfully"
                            } else {
                                "Query submitted Successfully"
                            }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        // Reset form on success
                        selectedCategory = null
                        selectedSubcategory = null
                        issueDescription = ""

                        // Go back after successful submission
                        delay(2000)
                        onBackPressed()
                    } else {
                        Toast
                            .makeText(
                                context,
                                "Unable to submit your concern: ${response.message}",
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                },
                onFailure = { exception ->
                    val errorMessage =
                        when (exception) {
                            is APIError.Unauthorized -> "Authentication error. Please login again."
                            is APIError.BadRequest -> "Invalid request. Please check your information."
                            is APIError.ServerError -> "Server error. Please try again later."
                            is APIError.EncryptionFailed -> "Security error. Please try again."
                            is APIError.DecryptionFailed -> "Security error. Please try again."
                            is APIError.SSLPinningFailed -> "Network security error. Please try again."
                            is APIError.DecodingError -> "Response processing error. Please try again."
                            else -> "Unable to submit your concern. Please try again."
                        }

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("RaiseConcern", "Error submitting encrypted SOS: ${exception.message}")
                },
            )
        } catch (e: Exception) {
            Toast
                .makeText(
                    context,
                    "Unable to submit your concern. Please check your internet connection and try again.",
                    Toast.LENGTH_SHORT,
                ).show()
            Log.e("RaiseConcern", "Exception during encrypted submission: ${e.message}")
        } finally {
            isSubmitting = false
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(), // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749)),
                        ),
                    ),
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                // Top app bar (fixed at top, not scrollable)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                        )
                    }
                    Text(
                        text = if (dynamicFormFields.isNotEmpty() && selectedSubcategory != null) {
                            selectedSubcategory!! // Show subcategory for dynamic forms
                        } else {
                            title // Show default title for non-dynamic forms
                        },
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = GraphikFontFamily,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    // Empty space for alignment
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Scrollable content
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic Form Mode - Wrap everything in beige container
                    if (source != "asset" && isHelpDeskTicket && dynamicFormFields.isNotEmpty()) {
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)), // Beige background
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                            ) {
                                // Employee Details Section
                                Text(
                                    text = "Employee Details",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 12.dp),
                                )

                                // Employee details rows
                                if (userData != null) {
                                    DynamicEmployeeDetailRow(label = "Name:", value = userData.name)
                                    DynamicEmployeeDetailRow(label = "Employee ID:", value = userData.employeeId)
                                    DynamicEmployeeDetailRow(label = "Mobile No:", value = userData.mobile)
                                    DynamicEmployeeDetailRow(label = "Designation:", value = userData.designation)
                                    DynamicEmployeeDetailRow(label = "Department:", value = userData.department)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Form Details Section Header
                                Text(
                                    text = "Form Details",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )

                                // Dynamic form fields
                                val sortedFields = dynamicFormFields.sortedBy { it.displayOrder }
                                sortedFields.forEach { field ->
                                    DynamicFormFieldComponent(
                                        field = field,
                                        value = dynamicFieldValues[field.fieldId] ?: "",
                                        onValueChange = { newValue ->
                                            dynamicFieldValues = dynamicFieldValues.toMutableMap().apply {
                                                put(field.fieldId, newValue)
                                            }
                                            // Clear error when user starts typing
                                            if (dynamicFieldErrors.containsKey(field.fieldId)) {
                                                dynamicFieldErrors = dynamicFieldErrors.toMutableMap().apply {
                                                    remove(field.fieldId)
                                                }
                                            }
                                        },
                                        isError = dynamicFieldErrors.containsKey(field.fieldId),
                                        errorMessage = dynamicFieldErrors[field.fieldId],
                                        modifier = Modifier.padding(horizontal = 0.dp), // Remove extra padding for alignment
                                    )
                                }

                                // Approval Chain Section
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Approval Chain",
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )

                                // Reporting Manager Row
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "Reporting Manager:",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray,
                                        modifier = Modifier.weight(0.5f),
                                    )
                                    Text(
                                        text = userData?.userDetails?.reporting_manager ?: "N/A",
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        modifier = Modifier.weight(0.5f),
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                // Category dropdown (hide when in dynamic form mode or pre-filled from FAQ)
                if (!(source != "asset" && isHelpDeskTicket && dynamicFormFields.isNotEmpty()) && !isCategoryLocked) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .padding(horizontal = 15.dp),
                    ) {
                        OutlinedTextField(
                            value = selectedCategory ?: "",
                            onValueChange = { },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Select Issue category",
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 16.sp,
                                )
                            },
                            trailingIcon = {
                                if (!isCategoryLocked) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.dropdown),
                                        contentDescription = "Dropdown",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            colors =
                                TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.LightGray,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                            textStyle =
                                TextStyle(
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 16.sp,
                                ),
                            shape = RoundedCornerShape(8.dp),
                        )

                        // Invisible clickable box over the TextField to trigger dropdown
                        if (!isCategoryLocked) {
                            Box(
                                modifier =
                                    Modifier
                                        .matchParentSize()
                                        .padding(horizontal = 15.dp)
                                        .clickable { expanded = true },
                            )
                        }

                        // This will position the dropdown below the TextField
                        // with exact same width as parent
                        if (expanded) {
                            // Popup dialog instead of standard DropdownMenu to match the design
                            Dialog(
                                onDismissRequest = { expanded = false },
                                properties =
                                    DialogProperties(
                                        dismissOnBackPress = true,
                                        dismissOnClickOutside = true,
                                        usePlatformDefaultWidth = false,
                                    ),
                            ) {
                                // The main container with padding to match the screen layout
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(0.85f) // Make the dropdown width 85% of screen width
                                            .padding(horizontal = 8.dp), // Reduce horizontal padding
                                ) {
                                    // Dropdown menu card
                                    Card(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor = Color.White,
                                            ),
                                    ) {
                                        Column(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth(),
                                        ) {
                                            categories.forEach { category ->
                                                Column(
                                                    modifier =
                                                        Modifier
                                                            .fillMaxWidth(),
                                                ) {
                                                    Text(
                                                        text = category,
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    selectedCategory = category
                                                                    expanded = false
                                                                }.padding(
                                                                    vertical = 12.dp, // Reduce vertical padding
                                                                    horizontal = 12.dp, // Reduce horizontal padding
                                                                ),
                                                        fontSize = 16.sp,
                                                        color = Color.Black,
                                                    )

                                                    // Add divider between items except for the last one
                                                    if (category != categories.last()) {
                                                        Divider(
                                                            color = Color.LightGray,
                                                            thickness = 1.dp,
                                                            modifier = Modifier.fillMaxWidth(),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // End of category dropdown conditional

                // Subcategory dropdown (hide when in dynamic form mode, show when category is selected or pre-filled) - only for helpdesk tickets
                if (!(source != "asset" && isHelpDeskTicket && dynamicFormFields.isNotEmpty()) && selectedCategory != null && (isHelpDeskTicket || source == "asset")) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .padding(horizontal = 15.dp),
                    ) {
                        OutlinedTextField(
                            value = selectedSubcategory ?: "",
                            onValueChange = { },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Select Sub-Category",
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily,
                                    fontSize = 16.sp,
                                )
                            },
                            trailingIcon = {
                                if (!isSubcategoryLocked) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.dropdown),
                                        contentDescription = "Dropdown",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                            shape = RoundedCornerShape(8.dp),
                        )

                        // Invisible clickable box over the TextField to trigger dropdown
                        if (!isSubcategoryLocked) {
                            Box(
                                modifier =
                                    Modifier
                                        .matchParentSize()
                                        .padding(horizontal = 15.dp)
                                        .clickable { subcategoryExpanded = true },
                            )
                        }

                        // Subcategory dropdown menu
                        if (subcategoryExpanded) {
                            Dialog(
                                onDismissRequest = { subcategoryExpanded = false },
                                properties =
                                    DialogProperties(
                                        dismissOnBackPress = true,
                                        dismissOnClickOutside = true,
                                        usePlatformDefaultWidth = false,
                                    ),
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth(0.85f)
                                            .padding(horizontal = 8.dp),
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor = Color.White,
                                            ),
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            availableSubcategories.forEach { subcategory ->
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                ) {
                                                    Text(
                                                        text = subcategory,
                                                        modifier =
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    selectedSubcategory = subcategory
                                                                    subcategoryExpanded = false
                                                                }.padding(
                                                                    vertical = 12.dp,
                                                                    horizontal = 12.dp,
                                                                ),
                                                        fontSize = 16.sp,
                                                        color = Color.Black,
                                                    )

                                                    if (subcategory != availableSubcategories.last()) {
                                                        Divider(
                                                            color = Color.LightGray,
                                                            thickness = 1.dp,
                                                            modifier = Modifier.fillMaxWidth(),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Show loading indicator for dynamic fields (only for help desk, NOT for asset)
                if (source != "asset" && dynamicFieldsLoading) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFD32F2F),
                        )
                    }
                }

                // Show error message for dynamic fields loading (only for help desk, NOT for asset)
                if (source != "asset" && dynamicFieldsError != null) {
                    Text(
                        text = dynamicFieldsError ?: "",
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 15.dp, vertical = 8.dp),
                    )
                }

                // Issue description (hide when in dynamic form mode)
                if (!(source != "asset" && isHelpDeskTicket && dynamicFormFields.isNotEmpty())) {
                    OutlinedTextField(
                        value = issueDescription,
                        onValueChange = { issueDescription = it },
                        placeholder = {
                            Text(
                                "Please describe your issue",
                                fontWeight = FontWeight.Medium,
                                fontFamily = GraphikFontFamily,
                                fontSize = 16.sp,
                            )
                        },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                            .padding(horizontal = 15.dp)
                            .padding(bottom = 20.dp),
                    colors =
                        TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.LightGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    textStyle =
                        TextStyle(
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                            fontSize = 16.sp,
                        ),
                    minLines = 5,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions =
                        KeyboardActions(onDone = {
                            if (issueDescription.isNotBlank() &&
                                selectedCategory != null &&
                                (isHelpDeskTicket && (availableSubcategories.isEmpty() || selectedSubcategory != null) || !isHelpDeskTicket)
                            ) {
                                if (isHelpDeskTicket) {
                                    coroutineScope.launch { submitHelpDeskTicket() }
                                } else {
                                    showAnonymousDialog = true
                                }
                            }
                        }),
                    shape = RoundedCornerShape(8.dp),
                    )
                }

                // Submit button
                Button(
                    onClick = {
                        if (isHelpDeskTicket) {
                            coroutineScope.launch { submitHelpDeskTicket() }
                        } else {
                            showAnonymousDialog = true
                        }
                    },
                    enabled = !isSubmitting,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            disabledContainerColor = PrimaryRed,
                            contentColor = Color.White,
                            disabledContentColor = Color.White,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp)
                            .height(50.dp),
                    shape = RoundedCornerShape(30.dp),
                ) {
                    Text(
                        text = "Submit",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                    )
                }

                // Bottom padding for scrollable content
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

            // Anonymous submission dialog (only for SOS concerns)
            if (showAnonymousDialog && !isHelpDeskTicket) {
                Dialog(
                    onDismissRequest = { showAnonymousDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(360.dp) // Set wider width for the dialog
                                .background(Color(0xFFF6F4EE), shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.anonymous),
                                contentDescription = "Anonymous Icon",
                                tint = Color(0xFFDD3825),
                                modifier = Modifier.size(60.dp),
                            )

                            // Title
                            Text(
                                text = "Submit Anonymously?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )

                            // Description
                            Text(
                                text = "Would you like to submit this concern anonymously? Your identity will not be disclosed.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(bottom = 24.dp),
                            )

                            // Submit anonymously button
                            Button(
                                onClick = {
                                    showAnonymousDialog = false
                                    coroutineScope.launch { submitSOSConcern(anonymous = true) }
                                },
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor =
                                            Color(
                                                0xFFDD3825,
                                            ),
                                    ),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .padding(bottom = 2.dp),
                                shape = RoundedCornerShape(28.dp),
                            ) {
                                Text(
                                    text = "Submit Anonymously",
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily,
                                )
                            }

                            // Submit with identity button
                            Button(
                                onClick = {
                                    showAnonymousDialog = false
                                    coroutineScope.launch { submitSOSConcern(anonymous = false) }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFABABAB),
                                    ),
                            ) {
                                Text(
                                    text = "Submit with Identity",
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily,
                                )
                            }
                        }
                    }
                }
            }

            // Timer dialog for helpdesk tickets
            if (showTimerDialog && isHelpDeskTicket) {
                LaunchedEffect(showTimerDialog) {
                    while (timerSeconds > 0) {
                        delay(1000)
                        timerSeconds--
                    }
                    if (timerSeconds <= 0) {
                        showTimerDialog = false

                        // Reset form on success AFTER timer completes
                        selectedCategory = null
                        selectedSubcategory = null
                        issueDescription = ""

                        // Navigate to track tickets after timer completes
                        if (onNavigateToTrackTickets != null) {
                            onNavigateToTrackTickets("Helpdesk")
                        } else {
                            // Fallback: try controller navigation
                            localHelpDeskController.navigateToTrackTickets("Helpdesk")
                        }
                    }
                }

                Dialog(
                    onDismissRequest = { /* Don't allow dismissing during timer */ },
                    properties =
                        DialogProperties(
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                        ),
                ) {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth(0.75f)
                                .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Title
                            Text(
                                text = "Ticket Submitted!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )

                            // Description
                            Text(
                                text = "Your ticket status will be shown in",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp),
                            )

                            // Timer circle
                            Box(
                                modifier =
                                    Modifier
                                        .size(100.dp)
                                        .padding(bottom = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                // Red circular progress indicator (remaining time drains away)
                                CircularProgressIndicator(
                                    progress = { timerSeconds / 20f },
                                    modifier = Modifier.size(96.dp),
                                    color = Color(0xFFD32F2F),
                                    strokeWidth = 4.dp,
                                    trackColor = Color(0xFFE0E0E0),
                                )

                                // Timer text in center
                                Text(
                                    text = String.format("%02d:%02d", 0, timerSeconds),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                )
                            }
                        }
                    }
                }
            }

            // Dynamic Form Success Dialog
            if (showDynamicFormSuccessDialog) {
                Dialog(onDismissRequest = { /* Prevent dismissal by clicking outside */ }) {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth(0.9f)
                                .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Green checkmark icon
                            Box(
                                modifier =
                                    Modifier
                                        .size(64.dp)
                                        .background(Color(0xFF4CAF50), shape = androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Success message - use API response message
                            Text(
                                text = successMessage ?: "Your request has been submitted successfully. Your ticket ID is ${successTicketId ?: "TKT-XXXXX"}. You can check the status in 'Track Tickets'",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // OK Button
                            Button(
                                onClick = {
                                    showDynamicFormSuccessDialog = false
                                    // Reset form after OK is clicked
                                    selectedCategory = null
                                    selectedSubcategory = null
                                    dynamicFieldValues = emptyMap()
                                    dynamicFieldErrors = emptyMap()
                                    localHelpDeskController.clearDynamicFormFields()
                                    onBackPressed()
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD32F2F),
                                    ),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = "OK",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = GraphikFontFamily,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }

            // Show loading indicator when submitting (but not when success dialog is shown)
            if (isSubmitting && !showDynamicFormSuccessDialog) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

/**
 * Individual employee detail row for dynamic form
 */
@Composable
private fun DynamicEmployeeDetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.weight(0.6f),
        )
    }
}
