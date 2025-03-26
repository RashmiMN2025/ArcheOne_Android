package com.archeGlobal.one.ui.screens

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
import androidx.compose.ui.draw.clip
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
import com.archeGlobal.one.network.RetrofitClient.apiService
import com.archeGlobal.one.model.SOSRequest
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseConcernScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Get user data
    val userDataManager = remember { UserDataManager.getInstance(context) }
    val userData = remember { userDataManager.getUserData() }
    
    // Form state
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var issueDescription by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showAnonymousDialog by remember { mutableStateOf(false) }
    
    // Categories based on the screenshot
    val categories = listOf(
        "Medical Emergency",
        "Fire Safety",
        "Security Risk",
        "Workplace Safety",
        "Non-Compliance",
        "POSH",
        "Other Issue"
    )
    
    // Submit functions
    suspend fun submitConcern(anonymous: Boolean) {
        if (selectedCategory == null || issueDescription.isBlank()) {
            Toast.makeText(context, "Please select a category and describe your issue", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true
        
        try {
            // Simulate network request with a delay
            kotlinx.coroutines.delay(1000)
            
            // Show success message based on anonymous status
            if (anonymous) {
                Toast.makeText(context, "Concern submitted anonymously", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Concern submitted with your identity", Toast.LENGTH_SHORT).show()
            }
            
            // Reset form on success
            selectedCategory = null
            issueDescription = ""
            
            // Go back after successful submission
            onBackPressed()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isSubmitting = false
        }
    }

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
                    text = "Raise a Concern",
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
            
            // Category dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                OutlinedTextField(
                    value = selectedCategory ?: "",
                    onValueChange = { },
                    readOnly = true,
                    placeholder = { Text("Select Issue category") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = Color.Black
                        )
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
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                )
                
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
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
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
                                                    .padding(vertical = 16.dp, horizontal = 16.dp),
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
                placeholder = { Text("Please describe your issue") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(bottom = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = Color.Black),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions.Default,
                shape = RoundedCornerShape(8.dp)
            )
            
            // Submit button
            Button(
                onClick = { showAnonymousDialog = true },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Submit",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
        
        // Anonymous submission dialog
        if (showAnonymousDialog) {
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
                                coroutineScope.launch { submitConcern(anonymous = true) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
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
                        OutlinedButton(
                            onClick = { 
                                showAnonymousDialog = false
                                coroutineScope.launch { submitConcern(anonymous = false) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Text(
                                text = "Submit with Identity",
                                fontSize = 16.sp
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
