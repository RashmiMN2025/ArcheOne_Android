package com.archeGlobal.one.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.R
import com.archeGlobal.one.model.DynamicFieldType
import com.archeGlobal.one.model.DynamicFormField
import com.archeGlobal.one.model.UserData
import com.archeGlobal.one.ui.theme.GraphikFontFamily

/**
 * Composable that renders a dynamic form field based on its type
 */
@Composable
fun DynamicFormFieldComponent(
    field: DynamicFormField,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        when (field.fieldType) {
            DynamicFieldType.TEXT -> {
                DynamicTextField(
                    field = field,
                    value = value,
                    onValueChange = onValueChange,
                    isError = isError,
                )
            }
            DynamicFieldType.DROPDOWN -> {
                DynamicDropdownField(
                    field = field,
                    selectedValue = value,
                    onValueChange = onValueChange,
                    isError = isError,
                )
            }
            DynamicFieldType.LONG_TEXT -> {
                DynamicLongTextField(
                    field = field,
                    value = value,
                    onValueChange = onValueChange,
                    isError = isError,
                )
            }
        }

        // Show error message if validation fails
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFD32F2F),
                fontSize = 12.sp,
                fontFamily = GraphikFontFamily,
                modifier = Modifier.padding(start = 0.dp, top = 4.dp),
            )
        }
    }
}

/**
 * Single-line text field
 */
@Composable
private fun DynamicTextField(
    field: DynamicFormField,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .padding(bottom = 16.dp),
    ) {
        // Label above the text field
        Text(
            text = buildString {
                append(field.fieldName)
                if (field.isRequired) append(" *")
            },
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Apply maxLength validation if specified
                if (field.maxLength == null || newValue.length <= field.maxLength) {
                    onValueChange(newValue)
                }
            },
            placeholder = {
                Text(
                    text = field.placeholder ?: field.fieldName,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.Black,
                    focusedIndicatorColor = if (isError) Color(0xFFD32F2F) else Color.Gray,
                    unfocusedIndicatorColor = if (isError) Color(0xFFD32F2F) else Color.LightGray,
                    errorIndicatorColor = Color(0xFFD32F2F),
                ),
            textStyle =
                TextStyle(
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            isError = isError,
        )
    }
}

/**
 * Multi-line text field for long text
 */
@Composable
private fun DynamicLongTextField(
    field: DynamicFormField,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .padding(bottom = 16.dp),
    ) {
        // Label above the text field
        Text(
            text = buildString {
                append(field.fieldName)
                if (field.isRequired) append(" *")
            },
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Apply maxLength validation if specified
                if (field.maxLength == null || newValue.length <= field.maxLength) {
                    onValueChange(newValue)
                }
            },
            placeholder = {
                Text(
                    text = field.placeholder ?: field.fieldName,
                    fontWeight = FontWeight.Medium,
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorTextColor = Color.Black,
                    focusedIndicatorColor = if (isError) Color(0xFFD32F2F) else Color.Gray,
                    unfocusedIndicatorColor = if (isError) Color(0xFFD32F2F) else Color.LightGray,
                    errorIndicatorColor = Color(0xFFD32F2F),
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
            shape = RoundedCornerShape(8.dp),
            isError = isError,
        )
    }
}

/**
 * Dropdown field with dialog-based selection
 */
@Composable
private fun DynamicDropdownField(
    field: DynamicFormField,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = field.dropdownOptions ?: emptyList()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .padding(bottom = 16.dp),
    ) {
        // Label above the text field
        Text(
            text = buildString {
                append(field.fieldName)
                if (field.isRequired) append(" *")
            },
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = { },
                readOnly = true,
                placeholder = {
                    Text(
                        text = field.placeholder ?: "Select ${field.fieldName}",
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp,
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.dropdown),
                        contentDescription = "Dropdown",
                        tint = Color.Gray,
                        modifier = Modifier.size(15.dp),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = if (isError) Color(0xFFD32F2F) else Color.Gray,
                        unfocusedIndicatorColor = if (isError) Color(0xFFD32F2F) else Color.LightGray,
                    ),
                textStyle =
                    TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp,
                    ),
                shape = RoundedCornerShape(8.dp),
                isError = isError,
            )

            // Invisible clickable box to trigger dropdown
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clickable { expanded = true },
            )
        }

        // Dropdown dialog
        if (expanded && options.isNotEmpty()) {
            Dialog(
                onDismissRequest = { expanded = false },
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            options.forEach { option ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = option,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onValueChange(option)
                                                    expanded = false
                                                }.padding(vertical = 12.dp, horizontal = 12.dp),
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        color = Color.Black,
                                    )

                                    if (option != options.last()) {
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

/**
 * Validates a dynamic field value based on field configuration
 * Returns error message if validation fails, null if valid
 */
fun validateDynamicField(
    field: DynamicFormField,
    value: String,
): String? {
    // Check required field
    if (field.isRequired && value.isBlank()) {
        return field.errorMessage ?: "${field.fieldName} is required"
    }

    // Skip further validation if field is empty and not required
    if (value.isBlank()) return null

    // Check minimum length
    if (field.minLength != null && value.length < field.minLength) {
        return field.errorMessage ?: "${field.fieldName} must be at least ${field.minLength} characters"
    }

    // Check maximum length
    if (field.maxLength != null && value.length > field.maxLength) {
        return field.errorMessage ?: "${field.fieldName} must not exceed ${field.maxLength} characters"
    }

    // Check regex validation (skip if regex is null or empty string)
    if (!field.validationRegex.isNullOrBlank()) {
        try {
            val regex = Regex(field.validationRegex)
            if (!regex.matches(value)) {
                return field.errorMessage ?: "${field.fieldName} format is invalid"
            }
        } catch (e: Exception) {
            // Invalid regex pattern, skip validation
            android.util.Log.e("DynamicFormFields", "Invalid regex pattern: ${field.validationRegex}", e)
        }
    }

    return null
}

/**
 * Employee Details Section Component
 * Displays employee information in a card layout matching the UI reference
 */
@Composable
fun EmployeeDetailsSection(
    user: UserData?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            // Section Title
            Text(
                text = "Employee Details",
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Employee details rows
            if (user != null) {
                EmployeeDetailRow(label = "Name:", value = user.name)
                EmployeeDetailRow(label = "Employee ID:", value = user.employeeId)
                EmployeeDetailRow(label = "Mobile No:", value = user.mobile)
                EmployeeDetailRow(label = "Designation:", value = user.designation)
                EmployeeDetailRow(label = "Department:", value = user.department, isLast = true)
            } else {
                Text(
                    text = "Employee information not available",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}

/**
 * Individual employee detail row
 */
@Composable
private fun EmployeeDetailRow(
    label: String,
    value: String,
    isLast: Boolean = false,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(0.6f),
        )
    }
}
