package com.archeGlobal.one.ui.screens

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.HelpDeskController
import com.archeGlobal.one.controller.SOSController
import com.archeGlobal.one.model.APIError
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun RaiseConcernScreen(
    onBackPressed: () -> Unit,
    title: String = "Raise a Concern",
    source: String = "helpdesk", // Add source parameter to track where we came from
    prefilledCategory: String? = null, // FAQ category to prefill and lock
    prefilledSubcategory: String? = null // FAQ subcategory to prefill and lock
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Get user data and controllers
    val userDataManager = remember { UserDataManager.getInstance(context) }
    val userData = remember { userDataManager.getUserData() }
    val sosController = remember { SOSController(context.applicationContext as Application) }
    val helpDeskController = remember { HelpDeskController(context) }

    // Determine if this is a help desk ticket or SOS concern
    val isHelpDeskTicket = title.contains("Ticket", ignoreCase = true)

    // Form state
    var selectedCategory by remember { mutableStateOf<String?>(prefilledCategory) }
    var selectedSubcategory by remember { mutableStateOf<String?>(prefilledSubcategory) }
    var issueDescription by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    val isCategoryLocked = prefilledCategory != null
    val isSubcategoryLocked = prefilledSubcategory != null
    var isSubmitting by remember { mutableStateOf(false) }
    var showAnonymousDialog by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(20) }

    // Get categories based on context
    val helpDeskModel by helpDeskController.model.collectAsState()
    val categories = if (isHelpDeskTicket) {
        // Extract categories from help desk FAQ data in their original order
        val helpDeskCategories = helpDeskModel.faqItems
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
            "Other Issue"
        )
    }

    // Subcategory mapping - extract from FAQ data for help desk, use hardcoded for SOS
    val subcategoryMap = if (isHelpDeskTicket) {
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
        val finalSubcategoryMap = subcategoriesFromFAQ.mapValues { (_, subcategories) ->
            subcategories.toList().sorted()
        }.toMutableMap().apply {
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
            "Other Issue" to listOf("General Concern", "Anonymous Report", "Other")
        )
    }

    // Get available subcategories for selected category
    val availableSubcategories = selectedCategory?.let { subcategoryMap[it] } ?: emptyList()

    // Reset subcategory when category changes (unless it's pre-filled)
    LaunchedEffect(selectedCategory) {
        if (!isSubcategoryLocked) {
            selectedSubcategory = null
        }
    }

    // Submit function for help desk tickets (using SOS endpoint, no anonymous option)
    suspend fun submitHelpDeskTicket() {
        if (selectedCategory == null || issueDescription.isBlank()) {
            Toast.makeText(
                context,
                "Please select a category and describe your issue",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (availableSubcategories.isNotEmpty() && selectedSubcategory == null) {
            Toast.makeText(
                context,
                "Please select a subcategory",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val user = userData
        if (user == null) {
            Toast.makeText(context, "User information not available", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true

        try {
            val request = SOSRequest(
                name = user.name ?: "",
                email = user.email ?: "",
                mobile = user.mobile ?: "",
                category = selectedCategory ?: "Other Issue",
                subcategory = selectedSubcategory,
                query = issueDescription,
                anonymous = false // Help desk tickets are never anonymous
            )

            Log.d("RaiseConcern", "Submitting help desk ticket via helpdesk endpoint: Category=$selectedCategory, Subcategory=$selectedSubcategory, Description=$issueDescription")

            val result = sosController.submitEncryptedHelpdeskRequest(request)

            result.fold(
                onSuccess = { response ->
                    if (response.status) {
                        val successMessage = if (source == "asset") {
                            // For asset concerns, use API response message
                            response.message
                        } else {
                            // For helpdesk concerns, use hardcoded message
                            "Ticket raised successfully"
                        }
                        
                        Toast.makeText(
                            context,
                            successMessage,
                            Toast.LENGTH_LONG
                        ).show()

                        // Reset form on success
                        selectedCategory = null
                        selectedSubcategory = null
                        issueDescription = ""

                        // Show timer dialog for helpdesk tickets only
                        showTimerDialog = true
                        timerSeconds = 20
                    } else {
                        val errorMessage = if (source == "asset") {
                            // For asset concerns, use API response message
                            response.message
                        } else {
                            // For helpdesk concerns, use hardcoded message with API message
                            "Failed to submit ticket: ${response.message}"
                        }
                        
                        Toast.makeText(
                            context,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onFailure = { exception ->
                    val errorMessage = if (source == "asset") {
                        // For asset concerns, use generic message since no API response available
                        "Unable to submit your concern. Please try again."
                    } else {
                        // For helpdesk concerns, use detailed hardcoded messages
                        when (exception) {
                            is APIError.Unauthorized -> "Authentication error. Please login again."
                            is APIError.BadRequest -> "Invalid request. Please check your information."
                            is APIError.ServerError -> "Server error. Please try again later."
                            is APIError.EncryptionFailed -> "Security error. Please try again."
                            is APIError.DecryptionFailed -> "Security error. Please try again."
                            is APIError.SSLPinningFailed -> "Network security error. Please try again."
                            is APIError.DecodingError -> "Response processing error. Please try again."
                            else -> "Unable to submit your ticket. Please try again."
                        }
                    }

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("RaiseConcern", "Error submitting help desk ticket: ${exception.message}")
                }
            )
        } catch (e: Exception) {
            val exceptionMessage = if (source == "asset") {
                "Unable to submit your concern. Please check your internet connection and try again."
            } else {
                "Unable to submit your ticket. Please check your internet connection and try again."
            }
            
            Toast.makeText(
                context,
                exceptionMessage,
                Toast.LENGTH_SHORT
            ).show()
            Log.e("RaiseConcern", "Exception during help desk ticket submission: ${e.message}")
        } finally {
            isSubmitting = false
        }
    }

    // Submit function for SOS concerns (existing functionality)
    suspend fun submitSOSConcern(anonymous: Boolean) {
        if (selectedCategory == null || issueDescription.isBlank()) {
            Toast.makeText(
                context,
                "Please select a category and describe your issue",
                Toast.LENGTH_SHORT
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

            val request = SOSRequest(
                name = user.name ?: "",
                email = user.email ?: "",
                mobile = user.mobile ?: "",
                category = selectedCategory ?: "Other Issue",
                subcategory = null, // SOS concerns don't use subcategory
                query = issueDescription,
                anonymous = anonymous
            )

            Log.d("RaiseConcern", "Submitting SOS concern: Category=$selectedCategory, Query=$issueDescription, Anonymous=$anonymous")

            val result = sosController.submitEncryptedSOSRequest(request)

            result.fold(
                onSuccess = { response ->
                    if (response.status) {
                        // Show success message based on anonymous status
                        val message = if (anonymous) {
                            "Concern submitted anonymously with encryption"
                        } else {
                            "Concern submitted with your identity (encrypted)"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        // Reset form on success
                        selectedCategory = null
                        selectedSubcategory = null
                        issueDescription = ""

                        // Go back after successful submission
                        onBackPressed()
                    } else {
                        Toast.makeText(
                            context,
                            "Unable to submit your concern: ${response.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onFailure = { exception ->
                    val errorMessage = when (exception) {
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
                }
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to submit your concern. Please check your internet connection and try again.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("RaiseConcern", "Exception during encrypted submission: ${e.message}")
        } finally {
            isSubmitting = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding() // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top app bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 25.dp)
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        text = title,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    // Empty space for alignment
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Category dropdown (hide when pre-filled from FAQ)
                if (!isCategoryLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 15.dp)
                ) {
                    OutlinedTextField(
                        value = selectedCategory ?: "",
                        onValueChange = { },
                        readOnly = true,
                        placeholder = { Text("Select Issue category") },
                        trailingIcon = {
                            if (!isCategoryLocked) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color.Black
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Invisible clickable box over the TextField to trigger dropdown
                    if (!isCategoryLocked) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(horizontal = 15.dp)
                                .clickable { expanded = true }
                        )
                    }

                    // This will position the dropdown below the TextField
                    // with exact same width as parent
                    if (expanded) {
                        // Popup dialog instead of standard DropdownMenu to match the design
                        Dialog(
                            onDismissRequest = { expanded = false },
                            properties = DialogProperties(
                                dismissOnBackPress = true,
                                dismissOnClickOutside = true,
                                usePlatformDefaultWidth = false
                            )
                        ) {
                            // The main container with padding to match the screen layout
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f) // Make the dropdown width 85% of screen width
                                    .padding(horizontal = 8.dp) // Reduce horizontal padding
                            ) {
                                // Dropdown menu card
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        categories.forEach { category ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = category,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            selectedCategory = category
                                                            expanded = false
                                                        }
                                                        .padding(
                                                            vertical = 12.dp, // Reduce vertical padding
                                                            horizontal = 12.dp // Reduce horizontal padding
                                                        ),
                                                    fontSize = 16.sp,
                                                    color = Color.Black
                                                )

                                                // Add divider between items except for the last one
                                                if (category != categories.last()) {
                                                    Divider(
                                                        color = Color.LightGray,
                                                        thickness = 1.dp,
                                                        modifier = Modifier.fillMaxWidth()
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

                // Subcategory dropdown (show when category is selected or pre-filled) - only for helpdesk tickets
                if (selectedCategory != null && isHelpDeskTicket) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .padding(horizontal = 15.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedSubcategory ?: "",
                            onValueChange = { },
                            readOnly = true,
                            placeholder = { Text("Select Subcategory") },
                            trailingIcon = {
                                if (!isSubcategoryLocked) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = Color.Black
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Invisible clickable box over the TextField to trigger dropdown
                        if (!isSubcategoryLocked) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(horizontal = 15.dp)
                                    .clickable { subcategoryExpanded = true }
                            )
                        }

                        // Subcategory dropdown menu
                        if (subcategoryExpanded) {
                            Dialog(
                                onDismissRequest = { subcategoryExpanded = false },
                                properties = DialogProperties(
                                    dismissOnBackPress = true,
                                    dismissOnClickOutside = true,
                                    usePlatformDefaultWidth = false
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            availableSubcategories.forEach { subcategory ->
                                                Column(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = subcategory,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                selectedSubcategory = subcategory
                                                                subcategoryExpanded = false
                                                            }
                                                            .padding(
                                                                vertical = 12.dp,
                                                                horizontal = 12.dp
                                                            ),
                                                        fontSize = 16.sp,
                                                        color = Color.Black
                                                    )

                                                    if (subcategory != availableSubcategories.last()) {
                                                        Divider(
                                                            color = Color.LightGray,
                                                            thickness = 1.dp,
                                                            modifier = Modifier.fillMaxWidth()
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

                // Issue description
                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = { issueDescription = it },
                    placeholder = { Text("Please describe your issue") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp)
                        .padding(horizontal = 15.dp)
                        .padding(bottom = 20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    minLines = 5,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (issueDescription.isNotBlank() && selectedCategory != null && 
                            (isHelpDeskTicket && (availableSubcategories.isEmpty() || selectedSubcategory != null) || !isHelpDeskTicket)) {
                            if (isHelpDeskTicket) {
                                coroutineScope.launch { submitHelpDeskTicket() }
                            } else {
                                showAnonymousDialog = true
                            }
                        }
                    }),
                    shape = RoundedCornerShape(8.dp)
                )

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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .height(50.dp),

                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(
                        text = "Submit",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            // Anonymous submission dialog (only for SOS concerns)
            if (showAnonymousDialog && !isHelpDeskTicket) {
                Dialog(onDismissRequest = { showAnonymousDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Icon
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(bottom = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_anonymous),
                                    contentDescription = "Anonymous Icon",
                                    tint = Color(0xFFDD3825),
                                    modifier = Modifier.size(60.dp)
                                )
                            }

                            // Title
                            Text(
                                text = "Submit Anonymously?",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Description
                            Text(
                                text = "Would you like to submit this concern anonymously? Your identity will not be disclosed.",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Submit anonymously button
                            Button(
                                onClick = {
                                    showAnonymousDialog = false
                                    coroutineScope.launch { submitSOSConcern(anonymous = true) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFFDD3825
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(
                                    text = "Submit Anonymously",
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }

                            // Submit with identity button
                            Button(
                                onClick = {
                                    showAnonymousDialog = false
                                    coroutineScope.launch { submitSOSConcern(anonymous = false) }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFABABAB)
                                )
                            ) {
                                Text(
                                    text = "Submit with Identity",
                                    fontSize = 16.sp,
                                    color = Color.White
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
                        // Navigate to track tickets after timer completes
                        helpDeskController.navigateToTrackTickets("Helpdesk")
                    }
                }
                
                Dialog(
                    onDismissRequest = { /* Don't allow dismissing during timer */ },
                    properties = DialogProperties(
                        dismissOnBackPress = false,
                        dismissOnClickOutside = false
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Title
                            Text(
                                text = "Ticket Submitted!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Description
                            Text(
                                text = "Your ticket status will be shown in",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Timer circle
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(bottom = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Background circle
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .background(
                                            Color(0xFFE5E5E5),
                                            shape = CircleShape
                                        )
                                )
                                
                                // Red circular progress indicator
                                CircularProgressIndicator(
                                    progress = (20 - timerSeconds) / 20f,
                                    modifier = Modifier.size(96.dp),
                                    color = Color(0xFFD32F2F),
                                    strokeWidth = 4.dp,
                                    trackColor = Color.Transparent
                                )
                                
                                // Timer text in center
                                Text(
                                    text = String.format("%02d:%02d", 0, timerSeconds),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Show loading indicator when submitting
            if (isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}
