package com.archeGlobal.one.ui.screens

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RaiseConcernScreen(
    onBackPressed: () -> Unit,
    title: String = "Raise a Concern",
    source: String = "helpdesk", // Add source parameter to track where we came from
    prefilledCategory: String? = null // FAQ category to prefill and lock
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
    var issueDescription by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val isCategoryLocked = prefilledCategory != null
    var isSubmitting by remember { mutableStateOf(false) }
    var showAnonymousDialog by remember { mutableStateOf(false) }

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
                query = issueDescription,
                anonymous = false // Help desk tickets are never anonymous
            )

            Log.d("RaiseConcern", "Submitting help desk ticket via helpdesk endpoint: Category=$selectedCategory, Description=$issueDescription")

            val result = sosController.submitEncryptedHelpdeskRequest(request)

            result.fold(
                onSuccess = { response ->
                    if (response.status) {
                        Toast.makeText(
                            context,
                            "Help desk ticket submitted successfully!",
                            Toast.LENGTH_LONG
                        ).show()

                        // Reset form on success
                        selectedCategory = null
                        issueDescription = ""

                        // Go back after successful submission
                        delay(2000)
                        onBackPressed()
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to submit ticket: ${response.message}",
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
                        else -> "Unable to submit your ticket. Please try again."
                    }

                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("RaiseConcern", "Error submitting help desk ticket: ${exception.message}")
                }
            )
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Unable to submit your ticket. Please check your internet connection and try again.",
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
                            "Query submitted Successfully"
                        } else {
                            "Query submitted Successfully"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        // Reset form on success
                        selectedCategory = null
                        issueDescription = ""

                        // Go back after successful submission
                        delay(2000)
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
                        fontFamily = GraphikFontFamily,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    // Empty space for alignment
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Category dropdown
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
                        placeholder = {
                            Text(
                                "Select Issue category",
                                fontWeight = FontWeight.Medium,
                                fontFamily = GraphikFontFamily,
                                fontSize = 16.sp
                            ) },
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
                            unfocusedTextColor = Color.LightGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontFamily = GraphikFontFamily,
                            fontSize = 16.sp
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

                // Issue description
                OutlinedTextField(
                    value = issueDescription,
                    onValueChange = { issueDescription = it },
                    placeholder = { Text(
                        "Please describe your issue",
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp
                    ) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp)
                        .padding(horizontal = 15.dp)
                        .padding(bottom = 20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.LightGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp
                    ),
                    minLines = 5,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (issueDescription.isNotBlank() && selectedCategory != null) {
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
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
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
                                fontFamily = GraphikFontFamily,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Description
                            Text(
                                text = "Would you like to submit this concern anonymously? Your identity will not be disclosed.",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Normal,
                                fontFamily = GraphikFontFamily,
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
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily,
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
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = GraphikFontFamily,
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
