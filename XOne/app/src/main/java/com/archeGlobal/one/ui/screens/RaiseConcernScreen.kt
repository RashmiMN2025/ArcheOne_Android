package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.network.RetrofitClient.apiService
import com.archeGlobal.one.model.SOSRequest
import kotlinx.coroutines.launch

@Composable
fun RaiseConcernScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Select Issue Category") }
    var issueDescription by remember { mutableStateOf("") }

    val categories = listOf("Technical Issue", "HR Issue", "Security Concern", "Other")
    var expanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    suspend fun submitConcern() {
        if (email.isBlank() || issueDescription.isBlank()) {
            Toast.makeText(context, "Email and Issue Description are required", Toast.LENGTH_SHORT).show()
            return
        }

        isSubmitting = true
        val request = SOSRequest(name, email, mobile, selectedCategory, issueDescription)

        try {
            val response = apiService.submitSOS(request)

            if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    Toast.makeText(context, responseBody.message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Success, but no message received!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                Toast.makeText(context, "Failed: $errorMessage", Toast.LENGTH_SHORT).show()
            }
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
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp)
            ) {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Raise a Concern",
                    color = Color.Black,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 80.dp)
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            @Composable
            fun customOutlinedTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions.Default,
                    shape = MaterialTheme.shapes.medium
                )
            }

            customOutlinedTextField(name, { name = it }, "Enter your name")
            customOutlinedTextField(email, { email = it }, "Enter your email")
            customOutlinedTextField(mobile, { mobile = it }, "Enter mobile number")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedCategory, color = Color.Black, modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.Black)
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = issueDescription,
                onValueChange = { issueDescription = it },
                placeholder = { Text("Please describe your issue") },
                modifier = Modifier.fillMaxWidth().height(140.dp).padding(bottom = 16.dp),
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
                keyboardActions = KeyboardActions.Default
            )

            Button(
                onClick = { coroutineScope.launch { submitConcern() } },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = "Submit",
                    color = Color.White)
            }
        }
    }
}
