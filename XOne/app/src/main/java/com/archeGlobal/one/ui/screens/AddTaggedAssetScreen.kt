package com.archeGlobal.one.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TagUserController
import com.archeGlobal.one.model.InventoryAsset
import com.archeGlobal.one.model.InventoryRequest
import com.archeGlobal.one.model.InventoryResponse
import com.archeGlobal.one.model.SuggestedAssetUser
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.archeGlobal.one.model.TagAssetCreateRequest
import com.archeGlobal.one.network.RetrofitClient
import androidx.compose.material3.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaggedAssetBottomSheet(
    tagName: String,
    employeeName: String,
    preSelectedUser: SuggestedAssetUser? = null,
    onDismiss: () -> Unit,
    onSave: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val context = LocalContext.current
    val controller = remember { TagUserController(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<SuggestedAssetUser?>(null) }
    var assetType by remember { mutableStateOf("") }
    var selectedAssetStr by remember { mutableStateOf("") }
    var selectedInventoryAsset by remember { mutableStateOf<InventoryAsset?>(null) }
    var dateOfIssue by remember { mutableStateOf("") }

    var inventory by remember { mutableStateOf<InventoryResponse?>(null) }
    var isLoadingInventory by remember { mutableStateOf(false) }
    var inventoryError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    var isTaggingInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(preSelectedUser) {
        if (preSelectedUser != null) {
            selectedUser = preSelectedUser
        }
    }

    // Fetch inventory on open
    LaunchedEffect(Unit) {
        isLoadingInventory = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getInventory(InventoryRequest())
            }
            inventory = response
        } catch (e: Exception) {
            inventoryError = "Failed to load inventory: ${e.message}"
        } finally {
            isLoadingInventory = false
        }
    }

    // Asset types: Laptop and Mobile
    val assetTypes = listOf("Laptop", "Mobile")

    // Assets for selected type
    val availableAssets = inventory?.data?.firstOrNull { it.deviceType.equals(assetType, ignoreCase = true) }?.assets ?: emptyList()
    val assetOptions = availableAssets.map { "${it.makeModel} - ${it.serialNumber}" }
    // Date Picker State
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }


    // Collect from controller
    val suggestedUsers by controller.suggestedUsers.collectAsState()
    val isLoadingSuggestions by controller.isLoadingSuggestions.collectAsState()
    val errorMessage by controller.errorMessage.collectAsState()

    // Real-time search with debounce
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            delay(400L)
            controller.fetchSuggestedUsers(searchQuery)
        } else {
            controller.fetchSuggestedUsers("")
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color(0xFFF6F4EE),
        dragHandle = null,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tag Asset",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = Color.Black
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (preSelectedUser == null) {
                // Search by Name Text
                Text(
                    text = "Search by Name",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

// Custom Search Dropdown
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded && searchQuery.length >= 2,
                    onExpandedChange = { /* Do nothing — we control it manually */ }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            expanded = it.length >= 2  // Show dropdown only when typing 2+ chars
                        },
                        placeholder = {
                            Text(
                                "Enter employee name",
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                        ),
                        singleLine = true
                    )

                    // Custom Dropdown Menu with background
                    ExposedDropdownMenu(
                        expanded = expanded && searchQuery.length >= 2,
                        onDismissRequest = { expanded = false },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .background(Color(0xFFF8F8F8))  // Light background
                            .exposedDropdownSize()         // Matches text field width
                    ) {
                        if (isLoadingSuggestions) {
                            DropdownMenuItem(
                                text = { Text("Loading...", fontFamily = GraphikFontFamily) },
                                onClick = { }
                            )
                        } else if (errorMessage != null) {
                            DropdownMenuItem(
                                text = { Text(errorMessage!!, color = Color.Red, fontFamily = GraphikFontFamily) },
                                onClick = { }
                            )
                        } else if (suggestedUsers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No users found", fontFamily = GraphikFontFamily) },
                                onClick = { }
                            )
                        } else {
                            suggestedUsers.forEach { user ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = user.username,
                                                    fontFamily = GraphikFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 10.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                user.emailId?.let {
                                                    Text(
                                                        text = it,
                                                        fontFamily = GraphikFontFamily,
                                                        fontWeight = FontWeight.Normal,
                                                        fontSize = 8.sp,
                                                        color = Color.Gray,
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedUser = user
                                            searchQuery = user.username  // Show name in text field
                                            expanded = false
                                        }
                                    )
                                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                } else {
                    // Show user details block when pre-selected
                    selectedUser?.let { user ->
                        UserDetailsCard(user = user)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // User Details Block
                selectedUser?.let { user ->
                    if (preSelectedUser == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "User Details",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                    IconButton(onClick = { selectedUser = null }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoRow("Name", user.username)
                                InfoRow("Employee Code", user.employeeCode)
                                InfoRow("Email", user.emailId)
                                InfoRow("Mobile", user.mobileNumber)
                                InfoRow("Location", user.location)
                                InfoRow("Designation", user.designation)
                                InfoRow("Division", user.division)
                                InfoRow("Department", user.department)
                            }
                        }
                    }
                }

            Spacer(modifier = Modifier.height(20.dp))

            // Asset Type
            Text("Asset Type", fontFamily = GraphikFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            var assetTypeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = assetTypeExpanded,
                onExpandedChange = { assetTypeExpanded = !assetTypeExpanded }
            ) {
                OutlinedTextField(
                    value = assetType,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select asset type", color = Color.Gray.copy(alpha = 0.6f)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetTypeExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .clickable {
                            assetTypeExpanded = true
                            if (inventory == null) {
                                coroutineScope.launch {
                                    isLoadingInventory = true
                                    try {
                                        val response = withContext(Dispatchers.IO) {
                                            RetrofitClient.apiService.getInventory(InventoryRequest())
                                        }
                                        inventory = response
                                    } catch (e: Exception) {
                                        inventoryError = "Failed to load inventory"
                                    } finally {
                                        isLoadingInventory = false
                                    }
                                }
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                    )
                )
                ExposedDropdownMenu(
                    expanded = assetTypeExpanded,
                    onDismissRequest = { assetTypeExpanded = false },
                    modifier = Modifier
                        .background(Color(0xFFF8F8F8))  // ← Light background
                        .exposedDropdownSize(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    assetTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type, fontFamily = GraphikFontFamily, color = Color.Black) },
                            onClick = {
                                assetType = type
                                selectedAssetStr = ""
                                selectedInventoryAsset = null
                                assetTypeExpanded = false
                            }
                        )
                    }
                }
            }

            if (isLoadingInventory) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (inventoryError != null) {
                Text(inventoryError ?: "", color = Color.Red)
            }

            if (assetType.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                // Select Asset
                Text("Select Asset", fontFamily = GraphikFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                var assetExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = assetExpanded,
                    onExpandedChange = { assetExpanded = !assetExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedAssetStr,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select asset from inventory", color = Color.Gray.copy(alpha = 0.6f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = assetExpanded,
                        onDismissRequest = { assetExpanded = false },
                        modifier = Modifier
                            .background(Color(0xFFF8F8F8))  // ← Light background
                            .exposedDropdownSize(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (assetOptions.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(
                                    "No assets available") },
                                onClick = { }
                            )
                        } else {
                            assetOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, fontFamily = GraphikFontFamily, color = Color.Black) },
                                    onClick = {
                                        selectedAssetStr = option
                                        val serialNumber = option.split(" - ").last()
                                        selectedInventoryAsset = availableAssets.firstOrNull { it.serialNumber == serialNumber }
                                        assetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            selectedInventoryAsset?.let { asset ->
                Spacer(modifier = Modifier.height(20.dp))

                // Asset Details Block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Asset Details", fontFamily = GraphikFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            IconButton(onClick = { selectedAssetStr = ""; selectedInventoryAsset = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow("Make & Model", asset.makeModel)
                        InfoRow("Serial No", asset.serialNumber)
                        InfoRow("Configuration", asset.configuration)
                        InfoRow("Location", asset.location)
                        InfoRow("Purchase Date", asset.purchaseDate)
                        InfoRow("Warranty Start", asset.warrantyStart)
                        InfoRow("Warranty End", asset.warrantyEnd)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date of Issue
            Text("Date of Issue", fontFamily = GraphikFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = dateOfIssue,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select date", color = Color.Gray.copy(alpha = 0.6f)) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendert),
                            contentDescription = "Calendar",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(25.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                )
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDateSelected = { dateMillis ->
                        dateMillis?.let {
                            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            dateOfIssue = sdf.format(Date(it))
                        }
                        showDatePicker = false
                    },
                    onDismiss = { showDatePicker = false }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF949494)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Cancel",
                        color = Color.White,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
                Button(
                    onClick = {
                        // Validation
                        if (selectedUser == null) {
                            Toast.makeText(context, "Please select an employee", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (assetType.isBlank()) {
                            Toast.makeText(context, "Please select asset type", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedInventoryAsset == null) {
                            Toast.makeText(context, "Please select an asset", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (dateOfIssue.isBlank()) {
                            Toast.makeText(context, "Please select date of issue", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isTaggingInProgress = true

                        coroutineScope.launch {
                            try {
                                // Format date to "27-Jun-25"
                                val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                val outputFormat = SimpleDateFormat("dd-MMM-yy", Locale.getDefault())
                                val parsedDate = inputFormat.parse(dateOfIssue)
                                val formattedDateOfIssue = outputFormat.format(parsedDate!!)

                                val request = TagAssetCreateRequest(
                                    serialNumber = selectedInventoryAsset!!.serialNumber,
                                    employeeCode = selectedUser!!.employeeCode,
                                    username = selectedUser!!.username,
                                    dateOfIssue = formattedDateOfIssue,
                                    location = selectedUser!!.location,
                                    designation = selectedUser!!.designation,
                                    assetType = assetType
                                )

                                val response = RetrofitClient.apiService.createTaggedAsset(request)

                                if (response.success) {
                                    // SUCCESS → Show Toast
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            response.message ?: "Asset tagged successfully!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    onSave()
                                    onDismiss()
                                } else {
                                    // FAILURE → Show error as Toast
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context,
                                            response.message ?: "Failed to tag asset",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                                Log.e("AddTaggedAsset", "Tagging failed", e)
                            } finally {
                                isTaggingInProgress = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = RoundedCornerShape(14.dp),
                    enabled = true  // Always enabled
                ) {
                    Text(
                        text = "Tag Asset",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GraphikFontFamily,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color.Gray
        )
        if (value != null) {
            Text(
                text = value,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun UserDetailsCard(
    user: SuggestedAssetUser
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("User Details", fontFamily = GraphikFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow("Name", user.username)
            InfoRow("Employee Code", user.employeeCode)
            InfoRow("Location", user.location)
            InfoRow("Designation", user.designation)
            InfoRow("Department", user.department)
            InfoRow("Division", user.division)
            InfoRow("Mobile Number", user.mobileNumber)
            InfoRow("Email", user.emailId)
            InfoRow("Reporting Manager", user.reportingManager)
            InfoRow("Divisional Head", user.divisionalHead)
        }
    }
}