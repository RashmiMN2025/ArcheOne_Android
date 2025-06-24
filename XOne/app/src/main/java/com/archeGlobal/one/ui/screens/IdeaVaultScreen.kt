package com.archeGlobal.one.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.IdeaVaultController
import com.archeGlobal.one.network.ApiService
import com.archeGlobal.one.network.FeedbackRequest
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun IdeaVaultScreen(
    onBackPressed: () -> Unit,
    controller: IdeaVaultController,
    apiService: ApiService
) {
    val employeeData = controller.employeeData
    var isSubmitting by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1),
                        Color(0xFFC8C8CA),
                        Color(0xFF474749)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 15.dp)
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Text(
                    text = "IdeaVault",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "Your go-to spot for sharing those brilliant ideas, quirky thoughts, and game-changing suggestions!",
                                fontSize = 16.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                color = Color.Black,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp),
                                lineHeight = 22.sp
                            )

                            Text(
                                text = "How to Use IdeaVault:  Share any idea, big or small, with clear details, constructive feedback, and have fun!",
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp),
                                lineHeight = 22.sp
                            )

                            Text(
                                text = "Drop your thoughts in the box and watch the magic unfold. Your input could be the next big thing!",
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp),
                                lineHeight = 22.sp
                            )

                            Text(
                                text = "Happy Ideating! \uD83D\uDC4D",
                                fontSize = 14.sp,
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                color = Color.Gray,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Dropdown Menu for Categories
                            var expanded by remember { mutableStateOf(false) }
                            var selectedCategory by remember { mutableStateOf("Select Category") }
                            val categories = listOf(
                                "UI/UX",
                                "Performance",
                                "Features",
                                "Bug Report",
                                "Suggestions",
                                "Other"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = { },
                                    readOnly = true,
                                    placeholder = { Text("Select Category") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown",
                                            tint = Color.Black
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = true },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = Color.LightGray,
                                        cursorColor = Color.Gray,
                                        unfocusedTextColor = Color.Black,
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedCategory == "Select Category") Color.Gray else Color.Black // Gray for placeholder, black for selected text
                                    )
                                )

                                // Invisible clickable box over the TextField to trigger dropdown
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable { expanded = true }
                                )

                                // Dropdown menu as a dialog
                                if (expanded) {
                                    Dialog(
                                        onDismissRequest = { expanded = false },
                                        properties = DialogProperties(
                                            dismissOnBackPress = true,
                                            dismissOnClickOutside = true,
                                            usePlatformDefaultWidth = false
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White)
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    categories.forEach { category ->
                                                        Column(
                                                            modifier = Modifier.fillMaxWidth()
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
                                                                        vertical = 16.dp,
                                                                        horizontal = 16.dp
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

                            Spacer(modifier = Modifier.height(16.dp))

                            var feedbackText by remember { mutableStateOf("") }

                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it },
                                placeholder = { Text("Submit an idea or Feedback") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp), // Taller text field for feedback
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.LightGray,
                                    focusedBorderColor = Color.LightGray,
                                    cursorColor = Color.Gray,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Button
                            Button(
                                onClick = {
                                    // Validate feedback and rating
                                    if (feedbackText.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Please provide feedback before submitting.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    isSubmitting = true

                                    // Prepare the request body
                                    val feedbackRequest = FeedbackRequest(
                                        name = employeeData.name,
                                        email = employeeData.email,
                                        category = if (selectedCategory != "Select Category") selectedCategory else null,
                                        feedback = feedbackText,
                                        rating = 0,
                                        platform = "Android",
                                        deviceName = Build.MODEL,
                                        version = Build.VERSION.RELEASE
                                    )

                                    // Make the API call
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val response =
                                                apiService.submitFeedback(feedbackRequest)
                                            withContext(Dispatchers.Main) {
                                                isSubmitting = false
                                                if (response.isSuccessful) {
                                                    Toast.makeText(
                                                        context,
                                                        "Feedback submitted successfully!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // Clear fields after successful submission
                                                    selectedCategory = "Select Category"
                                                    feedbackText = ""
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed to submit feedback: ${response.message()}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isSubmitting = false
                                                Toast.makeText(
                                                    context,
                                                    "An error occurred: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDD3825),
                                    disabledContainerColor = Color(0xFFDD3825), // keep red even when disabled
                                    contentColor = Color.White,
                                    disabledContentColor = Color.White
                                ),
                                shape = RoundedCornerShape(24.dp),
                                enabled = !isSubmitting
                            ) {
                                Text(
                                    text = if (isSubmitting) "Submit" else "Submit",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
