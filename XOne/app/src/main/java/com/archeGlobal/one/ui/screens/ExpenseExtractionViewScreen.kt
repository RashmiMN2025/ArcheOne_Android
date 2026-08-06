package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import android.provider.OpenableColumns
import android.graphics.Bitmap
import android.net.Uri
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.DropdownMenu
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.controller.ExpenseController
import com.archeGlobal.one.network.ExpenseDetailUi
import com.archeGlobal.one.network.ExpenseUi
import com.archeGlobal.one.network.SubmittedExpenseUi
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun queryName(context: android.content.Context, uri: Uri): String? {
    val returnCursor = context.contentResolver.query(uri, null, null, null, null)
    returnCursor?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            return cursor.getString(nameIndex)
        }
    }
    return null
}

private fun formatUploadDateTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) {
            val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

private fun dateFormate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return ""
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}

private enum class ExpenseTab { Drafts, Submitted }

private data class DraftExpenseItem(
    val id: String,
    val billDate: String,
    val vendorName: String,
    val category: String,
    val amount: String,
    val status: String,
    val uploadedAt: String,
    val source: ExpenseUi,
)

@Composable
fun ExpenseExtractionViewScreen(onBack: () -> Unit) {
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf(ExpenseTab.Drafts) }
    var showAddPopup by rememberSaveable { mutableStateOf(false) }
    var showManualBottomSheet by rememberSaveable { mutableStateOf(false) }
    var selectedExpenseDetail by remember { mutableStateOf<ExpenseDetailUi?>(null) }
    var isDetailReadOnly by rememberSaveable { mutableStateOf(false) }
    var expensePendingDelete by remember { mutableStateOf<ExpenseUi?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val controller = remember { ExpenseController(context) }
    val travelController = remember { com.archeGlobal.one.controller.TravelExpenseController(context) }

    val allExpenses by controller.expenses
    val submittedExpenses by controller.submittedExpenses
    val projectOptions by travelController.projectOptions
    val tripOptions by travelController.tripOptions
    val isLoading by controller.isLoading
    val submittedLoading by controller.submittedLoading
    val detailLoading by controller.detailLoading
    val downloadLoading by controller.downloadLoading

    if (selectedExpenseDetail != null) {
        BackHandler {
            selectedExpenseDetail = null
            isDetailReadOnly = false
            if (selectedTab == ExpenseTab.Drafts) {
                controller.fetchExpenses()
            } else {
                controller.fetchSubmittedExpenses()
            }
        }
        ExpenseExtractionDetailViewScreen(
            expense = selectedExpenseDetail!!,
            isReadOnly = isDetailReadOnly,
            onBack = {
                selectedExpenseDetail = null
                isDetailReadOnly = false
                if (selectedTab == ExpenseTab.Drafts) {
                    controller.fetchExpenses()
                } else {
                    controller.fetchSubmittedExpenses()
                }
            },
        )
        return
    }

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        controller.fetchExpenses()
        travelController.fetchProjectOptions()
        travelController.fetchTripOptions()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == ExpenseTab.Submitted) {
            controller.fetchSubmittedExpenses()
        }
    }

    LaunchedEffect(allExpenses) {
        if (allExpenses.any { it.status.equals("Extracting", ignoreCase = true) }) {
            delay(5000)
            controller.fetchExpenses()
        }
    }

    fun openExpenseDetail(expenseId: String, readOnly: Boolean) {
        isDetailReadOnly = readOnly
        controller.fetchExpenseDetail(
            expenseId = expenseId,
            onSuccess = { detail -> selectedExpenseDetail = detail },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() },
        )
    }

    fun deleteExpense(expense: ExpenseUi) {
        controller.deleteExpense(
            expenseId = expense.id,
            onSuccess = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() },
        )
    }

    fun downloadExpense(expenseId: String, fileName: String?) {
        controller.downloadExpenseFile(
            expenseId = expenseId,
            fallbackFileName = fileName,
            onSuccess = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() },
        )
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            try {
                val baos = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                val bytes = baos.toByteArray()
                controller.uploadExpense(bytes, "camera.jpg", "image/jpeg",
                    onSuccess = { resp ->
                        Toast.makeText(context, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                    },
                    onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to capture image: ${'$'}{e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_LONG).show()
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        uris.forEach { uri ->
            try {
                val input = context.contentResolver.openInputStream(uri)
                val bytes = input?.use { stream -> stream.readBytes() }
                val filename = queryName(context, uri) ?: uri.lastPathSegment ?: "upload"
                if (bytes != null && bytes.isNotEmpty()) {
                    controller.uploadExpense(bytes, filename, "application/octet-stream",
                        onSuccess = { resp ->
                            Toast.makeText(context, "Uploaded $filename successfully", Toast.LENGTH_SHORT).show()
                        },
                        onError = { msg -> Toast.makeText(context, "Failed to upload $filename: $msg", Toast.LENGTH_LONG).show() }
                    )
                } else {
                    Toast.makeText(context, "Failed to read file: $filename", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${'$'}{e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val draftStatuses = setOf("Uploaded", "Extracted", "Extracting")
    val filteredDrafts = allExpenses.filter { expense ->
        expense.status in draftStatuses && (
            searchText.isBlank() ||
                expense.id.contains(searchText, ignoreCase = true) ||
                expense.vendorName.contains(searchText, ignoreCase = true) ||
                expense.name.contains(searchText, ignoreCase = true)
            )
    }
    val filteredSubmitted = submittedExpenses.filter { expense ->
        searchText.isBlank() ||
            expense.id.contains(searchText, ignoreCase = true) ||
            expense.vendorName.contains(searchText, ignoreCase = true) ||
            expense.category.contains(searchText, ignoreCase = true) ||
            expense.name.contains(searchText, ignoreCase = true) ||
            expense.status.contains(searchText, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,
                        WelcomeBackgroundMiddle,
                        WelcomeBackgroundBottom
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(48.dp))
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Expense Extraction",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                actions = { Spacer(modifier = Modifier.size(48.dp)) }
            )

            Text(
                text = "Expense Extraction",
                fontFamily = GraphikFontFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Keep track of expenses with real-time metrics",
                fontFamily = GraphikFontFamily,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                },
                placeholder = {
                    Text(
                        text = "Search expenses...",
                        color = Color.Gray,
                        fontFamily = GraphikFontFamily
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color(0xFFD4D4D4)
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpenseTabButton(
                    title = "Drafts",
                    isSelected = selectedTab == ExpenseTab.Drafts,
                    onClick = { selectedTab = ExpenseTab.Drafts }
                )
                ExpenseTabButton(
                    title = "Submitted",
                    isSelected = selectedTab == ExpenseTab.Submitted,
                    onClick = { selectedTab = ExpenseTab.Submitted }
                )
            }

            when (selectedTab) {
                ExpenseTab.Drafts -> {
                    DraftsListContent(
                        expenses = filteredDrafts.map { exp ->
                            DraftExpenseItem(
                                id = "EXP-${exp.id}",
                                billDate = exp.billDate,
                                vendorName = exp.vendorName,
                                category = exp.category,
                                amount = exp.totalAmount,
                                status = exp.status,
                                uploadedAt = exp.createdAt,
                                source = exp,
                            )
                        },
                        onViewDetails = { openExpenseDetail(it.id, readOnly = false) },
                        onDelete = { expensePendingDelete = it },
                        modifier = Modifier.weight(1f)
                    )
                }
                ExpenseTab.Submitted -> {
                    if (submittedLoading && filteredSubmitted.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = PrimaryRed)
                        }
                    } else {
                        SubmittedListContent(
                            expenses = filteredSubmitted,
                            onViewDetails = { openExpenseDetail(it.id, readOnly = true) },
                            onDownload = { downloadExpense(it.id, it.name) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        if (detailLoading || downloadLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PrimaryRed)
            }
        }

        Button(
            onClick = { showAddPopup = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = PrimaryRed,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Expense",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showAddPopup) {
        AddExpensePopup(
            onDismiss = { showAddPopup = false },
            onCamera = {
                if (hasCameraPermission) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                showAddPopup = false
            },
            onFiles = {
                fileLauncher.launch("*/*")
                showAddPopup = false
            },
            onManual = {
                showAddPopup = false
                showManualBottomSheet = true
            }
        )
    }

    expensePendingDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expensePendingDelete = null },
            title = { Text(text = "Delete Bill", fontFamily = GraphikFontFamily, fontWeight = FontWeight.SemiBold) },
            text = { Text(text = "Are you sure you want to delete this bill? This action cannot be undone.", fontFamily = GraphikFontFamily) },
            confirmButton = {
                TextButton(onClick = {
                    deleteExpense(expense)
                    expensePendingDelete = null
                }) {
                    Text(text = "Delete", color = PrimaryRed, fontFamily = GraphikFontFamily, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expensePendingDelete = null }) {
                    Text(text = "Cancel", fontFamily = GraphikFontFamily)
                }
            },
        )
    }

    if (showManualBottomSheet) {
        ManualExpenseEntryBottomSheet(
            projectOptions = projectOptions,
            tripOptions = tripOptions,
            onDismiss = { showManualBottomSheet = false },
            onProjectSelected = { projectId, customerId, soNumber, tripId ->
                showManualBottomSheet = false
                Toast.makeText(context, "Project: $projectId, Trip: $tripId", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
private fun ExpenseTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = title,
        fontFamily = GraphikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = if (isSelected) Color.White else Color.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (isSelected) PrimaryRed else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 12.dp)
    )
}

@Composable
private fun DraftsListContent(
    expenses: List<DraftExpenseItem>,
    onViewDetails: (ExpenseUi) -> Unit,
    onDelete: (ExpenseUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Uploaded files (${expenses.size})",
            fontFamily = GraphikFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (expenses.isEmpty()) {
            EmptyExpenseState(message = "No draft expenses found")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(expenses) { expense ->
                    DraftExpenseCard(
                        expense = expense,
                        onViewDetails = { onViewDetails(expense.source) },
                        onDelete = { onDelete(expense.source) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SubmittedListContent(
    expenses: List<SubmittedExpenseUi>,
    onViewDetails: (SubmittedExpenseUi) -> Unit,
    onDownload: (SubmittedExpenseUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Submitted expenses (${expenses.size})",
            fontFamily = GraphikFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (expenses.isEmpty()) {
            EmptyExpenseState(message = "No submitted expenses found")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(expenses) { expense ->
                    SubmittedExpenseCard(
                        expense = expense,
                        onViewDetails = { onViewDetails(expense) },
                        onDownload = { onDownload(expense) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DraftExpenseCard(
    expense: DraftExpenseItem,
    onViewDetails: () -> Unit,
    onDelete: () -> Unit,
) {
    ExpenseCardContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = expense.id,
                fontFamily = GraphikFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = expense.status,
                color = Color.White,
                fontFamily = GraphikFontFamily,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(Color(0xFF1976D2), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Divider(color = Color(0xFFEEEEEE))
        ExpenseDetailRow("Bill Date", dateFormate(expense.billDate))
        ExpenseDetailRow("Vendor Name", expense.vendorName)
        ExpenseDetailRow("Category", expense.category)
        ExpenseDetailRow("Uploaded At", formatUploadDateTime(expense.uploadedAt))
        Divider(color = Color(0xFFEEEEEE))
        ExpenseCardActionsRow(onViewDetails = onViewDetails, onDelete = onDelete)
    }
}

@Composable
private fun SubmittedExpenseCard(
    expense: SubmittedExpenseUi,
    onViewDetails: () -> Unit,
    onDownload: () -> Unit,
) {
    ExpenseCardContainer {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "EXP-${expense.id}",
                fontFamily = GraphikFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            )
            Text(
                text = expense.status,
                color = when (expense.status.lowercase()) {
                    "approved" -> Color(0xFF2E7D32)
                    "rejected" -> PrimaryRed
                    "submitted", "pending" -> Color(0xFFEF6C00)
                    else -> Color(0xFF1565C0)
                },
                fontFamily = GraphikFontFamily,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(
                        when (expense.status.lowercase()) {
                            "approved" -> Color(0xFFE8F5E9)
                            "rejected" -> PrimaryRed.copy(alpha = 0.12f)
                            "submitted", "pending" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFE3F2FD)
                        },
                        RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        Divider(color = Color(0xFFEEEEEE))
        ExpenseDetailRow("Category", expense.category)
        ExpenseDetailRow("Bill Date", dateFormate(expense.billDate))
        ExpenseDetailRow("Description", expense.vendorName)
        ExpenseDetailRow("Amount", expense.amount)
        ExpenseDetailRow("Submission Date", formatUploadDateTime(expense.submittedAt))
        Divider(color = Color(0xFFEEEEEE))
        SubmittedCardActionsRow(onViewDetails = onViewDetails, onDownload = onDownload)
    }
}

@Composable
private fun ExpenseCardContainer(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ExpenseDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ExpenseCardActionsRow(
    onViewDetails: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "View Details",
            tint = PrimaryRed,
            modifier = Modifier
                .size(22.dp)
                .clickable { onViewDetails() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = PrimaryRed,
            modifier = Modifier
                .size(22.dp)
                .clickable { onDelete() }
        )
    }
}

@Composable
private fun SubmittedCardActionsRow(
    onViewDetails: () -> Unit,
    onDownload: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "View Details",
            tint = PrimaryRed,
            modifier = Modifier
                .size(22.dp)
                .clickable { onViewDetails() },
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Download",
            tint = PrimaryRed,
            modifier = Modifier
                .size(22.dp)
                .clickable { onDownload() },
        )
    }
}

@Composable
private fun EmptyExpenseState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = message,
            fontFamily = GraphikFontFamily,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun AddExpensePopup(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onFiles: () -> Unit,
    onManual: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Expense",
                    fontFamily = GraphikFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                PopupOptionButton(
                    icon = Icons.Default.CameraAlt,
                    title = "Use Camera",
                    onClick = onCamera
                )
                PopupOptionButton(
                    icon = Icons.Default.Image,
                    title = "Add photos and files",
                    onClick = onFiles
                )
                PopupOptionButton(
                    icon = Icons.Default.Edit,
                    title = "Enter manually",
                    onClick = onManual
                )
            }
        }
    }
}

private data class ManualExpenseItemForm(
    val passengerName: String = "",
    val seatNumber: String = "",
    val purpose: String = "",
    val item: String = "",
    val quantity: String = "",
    val price: String = "",
    val total: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualExpenseEntryBottomSheet(
    projectOptions: List<com.archeGlobal.one.network.ProjectOption>,
    tripOptions: List<com.archeGlobal.one.network.TripOptionResponse>,
    onDismiss: () -> Unit,
    onProjectSelected: (projectId: String, customerId: String, soNumber: String?, tripId: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var projectId by rememberSaveable { mutableStateOf("") }
    var tripId by rememberSaveable { mutableStateOf("") }
    var customerId by rememberSaveable { mutableStateOf("") }
    var soNumber by rememberSaveable { mutableStateOf("") }
    var projectExpanded by remember { mutableStateOf(false) }
    var tripExpanded by remember { mutableStateOf(false) }
    var customerExpanded by remember { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf("") }
    var travelMode by rememberSaveable { mutableStateOf("") }
    var hotelMode by rememberSaveable { mutableStateOf("") }
    var accommodationType by rememberSaveable { mutableStateOf("") }
    var travelClass by rememberSaveable { mutableStateOf("") }
    var vendorName by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("") }
    var travelName by rememberSaveable { mutableStateOf("") }
    var bookingPlatform by rememberSaveable { mutableStateOf("") }
    var boardingDate by rememberSaveable { mutableStateOf("") }
    var boardingTime by rememberSaveable { mutableStateOf("") }
    var boardingAddress by rememberSaveable { mutableStateOf("") }
    var droppingDate by rememberSaveable { mutableStateOf("") }
    var droppingTime by rememberSaveable { mutableStateOf("") }
    var droppingAddress by rememberSaveable { mutableStateOf("") }
    var trainClass by rememberSaveable { mutableStateOf("") }
    var trainName by rememberSaveable { mutableStateOf("") }
    var trainNumber by rememberSaveable { mutableStateOf("") }
    var pnrNumber by rememberSaveable { mutableStateOf("") }
    var departure by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var fromLocation by rememberSaveable { mutableStateOf("") }
    var toLocation by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var gst by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var sgstPercent by rememberSaveable { mutableStateOf("") }
    var sgstAmount by rememberSaveable { mutableStateOf("") }
    var cgstPercent by rememberSaveable { mutableStateOf("") }
    var cgstAmount by rememberSaveable { mutableStateOf("") }
    var igstPercent by rememberSaveable { mutableStateOf("") }
    var igstAmount by rememberSaveable { mutableStateOf("") }
    var taxAmount by rememberSaveable { mutableStateOf("") }
    var gsint by rememberSaveable { mutableStateOf("") }
    var expenseDate by rememberSaveable { mutableStateOf("") }
    var detailItems by remember { mutableStateOf(listOf(ManualExpenseItemForm())) }

    val categoryOptions = listOf(
        "Travel",
        "Flight Receipt",
        "Flight Invoice",
        "Hotel Accommodation",
        "Meals food",
        "Stationery",
        "Fuel gas",
        "Entertainment",
        "Information Technology",
        "Other",
    )
    val travelModeOptions = listOf("general", "Auto Bike taxi", "Bus", "train", "Flight Receipt", "Flight Invoice")
    val hotelModeOptions = listOf("invoice", "receipt")
    val accommodationTypeOptions = listOf("Domestic", "International")
    val travelClassOptions = listOf("economy", "premium economy", "business")
    val trainClassOptions = listOf("tier 3", "tier 2", "tier 1")
    val customerOptions = projectOptions.mapNotNull { it.customerId }.distinct()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color(0xFFF6F4EE),
        dragHandle = null,
        tonalElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Enter Expense Details",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 19.sp,
                    color = Color.Black,
                )
                Text(
                    text = "Cancel",
                    color = PrimaryRed,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Reference details",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.Black,
                            )
                            ManualDropdownField(
                                label = "Project ID",
                                value = projectId,
                                options = projectOptions.map { it.code },
                                onValueChange = {
                                    projectId = it
                                    projectOptions.firstOrNull { option -> option.code.equals(it, ignoreCase = true) }?.let { selected ->
                                        customerId = selected.customerId.orEmpty()
                                        soNumber = selected.soNumber.orEmpty()
                                    }
                                },
                            )
                            ManualTextField(
                                label = "Customer ID",
                                value = customerId,
                                onValueChange = { customerId = it },
                                placeholder = "Enter customer ID",
                            )
                            ManualDropdownField(
                                label = "Trip ID",
                                value = tripId,
                                options = tripOptions.map { it.tripId },
                                onValueChange = { tripId = it },
                            )
                            ManualTextField(
                                label = "SO Number",
                                value = soNumber,
                                onValueChange = { soNumber = it },
                                placeholder = "Enter SO number",
                            )
                        }
                    }

                    SectionCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Expense details",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.Black,
                            )
                            ManualDropdownField(
                                label = "Category *",
                                value = category,
                                options = categoryOptions,
                                onValueChange = {
                                    category = it
                                    if (it != "Travel") {
                                        travelMode = ""
                                    }
                                    if (it != "Hotel Accommodation") {
                                        hotelMode = ""
                                        accommodationType = ""
                                    }
                                },
                            )

                            if (category == "Travel") {
                                ManualDropdownField(
                                    label = "Mode",
                                    value = travelMode,
                                    options = travelModeOptions,
                                    onValueChange = { travelMode = it },
                                )

                                when (travelMode) {
                                    "Bus" -> {
                                        ManualTextField(label = "Travel name *", value = travelName, onValueChange = { travelName = it }, placeholder = "Enter travel name")
                                        ManualTextField(label = "Booking platform *", value = bookingPlatform, onValueChange = { bookingPlatform = it }, placeholder = "Enter booking platform")
                                        ManualTextField(label = "Currency *", value = currency, onValueChange = { currency = it }, placeholder = "Enter currency")
                                        DateSelectorField(label = "Boarding date *", value = boardingDate, onValueChange = { boardingDate = it }, placeholder = "YYYY-MM-DD")
                                        ManualTextField(label = "Boarding time *", value = boardingTime, onValueChange = { boardingTime = it }, placeholder = "HH:MM")
                                        ManualTextField(label = "Boarding address *", value = boardingAddress, onValueChange = { boardingAddress = it }, placeholder = "Enter boarding address")
                                        DateSelectorField(label = "Dropping date *", value = droppingDate, onValueChange = { droppingDate = it }, placeholder = "YYYY-MM-DD")
                                        ManualTextField(label = "Dropping time *", value = droppingTime, onValueChange = { droppingTime = it }, placeholder = "HH:MM")
                                        ManualTextField(label = "Dropping address *", value = droppingAddress, onValueChange = { droppingAddress = it }, placeholder = "Enter dropping address")
                                        detailItems.forEachIndexed { index, item ->
                                            TravelItemBlock(
                                                index = index + 1,
                                                passengerName = item.passengerName,
                                                seatNumber = item.seatNumber,
                                                onPassengerNameChange = { updated -> detailItems = detailItems.toMutableList().apply { set(index, item.copy(passengerName = updated)) } },
                                                onSeatNumberChange = { updated -> detailItems = detailItems.toMutableList().apply { set(index, item.copy(seatNumber = updated)) } },
                                                onDelete = { detailItems = detailItems.filterIndexed { itemIndex, _ -> itemIndex != index } }
                                            )
                                        }
                                        TextButton(onClick = { detailItems = detailItems + ManualExpenseItemForm() }) {
                                            Text("Add new item", color = PrimaryRed, fontFamily = GraphikFontFamily)
                                        }
                                        ManualTextField(label = "GST", value = gst, onValueChange = { gst = it }, placeholder = "Enter GST")
                                        ManualTextField(label = "Amount", value = amount, onValueChange = { amount = it }, placeholder = "Enter amount")
                                        ManualTextField(label = "Notes", value = notes, onValueChange = { notes = it }, placeholder = "Enter notes")
                                    }
                                    "train" -> {
                                        ManualDropdownField(label = "Train class *", value = trainClass, options = trainClassOptions, onValueChange = { trainClass = it })
                                        ManualTextField(label = "Vendor name *", value = vendorName, onValueChange = { vendorName = it }, placeholder = "Enter vendor name")
                                        ManualTextField(label = "Currency *", value = currency, onValueChange = { currency = it }, placeholder = "Enter currency")
                                        ManualTextField(label = "Train name *", value = trainName, onValueChange = { trainName = it }, placeholder = "Enter train name")
                                        ManualTextField(label = "Train number *", value = trainNumber, onValueChange = { trainNumber = it }, placeholder = "Enter train number")
                                        ManualTextField(label = "PNR number *", value = pnrNumber, onValueChange = { pnrNumber = it }, placeholder = "Enter PNR")
                                        ManualTextField(label = "Departure *", value = departure, onValueChange = { departure = it }, placeholder = "Enter departure")
                                        ManualTextField(label = "Boarding *", value = fromLocation, onValueChange = { fromLocation = it }, placeholder = "Enter boarding")
                                        ManualTextField(label = "Destination *", value = destination, onValueChange = { destination = it }, placeholder = "Enter destination")
                                        ManualTextField(label = "Dropping *", value = toLocation, onValueChange = { toLocation = it }, placeholder = "Enter dropping")
                                        detailItems.forEachIndexed { index, item ->
                                            TravelItemBlock(
                                                index = index + 1,
                                                passengerName = item.passengerName,
                                                seatNumber = item.purpose,
                                                seatLabel = "Purpose / Project ID",
                                                onPassengerNameChange = { updated -> detailItems = detailItems.toMutableList().apply { set(index, item.copy(passengerName = updated)) } },
                                                onSeatNumberChange = { updated -> detailItems = detailItems.toMutableList().apply { set(index, item.copy(purpose = updated)) } },
                                                onDelete = { detailItems = detailItems.filterIndexed { itemIndex, _ -> itemIndex != index } }
                                            )
                                        }
                                        TextButton(onClick = { detailItems = detailItems + ManualExpenseItemForm() }) {
                                            Text("Add new item", color = PrimaryRed, fontFamily = GraphikFontFamily)
                                        }
                                        ManualTextField(label = "SGST Percentage *", value = sgstPercent, onValueChange = { sgstPercent = it }, placeholder = "Enter SGST %")
                                        ManualTextField(label = "SGST amount *", value = sgstAmount, onValueChange = { sgstAmount = it }, placeholder = "Enter SGST amount")
                                        ManualTextField(label = "CGST Percentage *", value = cgstPercent, onValueChange = { cgstPercent = it }, placeholder = "Enter CGST %")
                                        ManualTextField(label = "CGST amount *", value = cgstAmount, onValueChange = { cgstAmount = it }, placeholder = "Enter CGST amount")
                                        ManualTextField(label = "IGST Percentage *", value = igstPercent, onValueChange = { igstPercent = it }, placeholder = "Enter IGST %")
                                        ManualTextField(label = "IGST amount *", value = igstAmount, onValueChange = { igstAmount = it }, placeholder = "Enter IGST amount")
                                        ManualTextField(label = "GST *", value = gst, onValueChange = { gst = it }, placeholder = "Enter GST")
                                        ManualTextField(label = "Amount *", value = amount, onValueChange = { amount = it }, placeholder = "Enter amount")
                                        ManualTextField(label = "Notes", value = notes, onValueChange = { notes = it }, placeholder = "Enter notes")
                                    }
                                    "Flight Receipt", "Flight Invoice" -> {
                                        ManualDropdownField(label = "Flight class", value = travelClass,  options = travelClassOptions, onValueChange = { travelClass = it })
                                        ManualTextField(label = "Notes", value = notes, onValueChange = { notes = it }, placeholder = "Enter notes")
                                    }
                                    else -> {
                                        ManualTextField(label = "Notes", value = notes, onValueChange = { notes = it }, placeholder = "Enter notes")
                                    }
                                }
                            }

                            if (category == "Hotel Accommodation") {
                                ManualDropdownField(label = "Mode *", value = hotelMode, options = hotelModeOptions, onValueChange = { hotelMode = it })
                                ManualDropdownField(label = "Accommodation type *", value = accommodationType,  options = accommodationTypeOptions, onValueChange = { accommodationType = it })
                                ManualTextField(label = "Notes", value = notes, onValueChange = { notes = it }, placeholder = "Enter notes")
                            }

                            if (category == "Meals food") {
                                ManualTextField(label = "Vendor name *", value = vendorName, onValueChange = { vendorName = it }, placeholder = "Enter vendor name")
                                ManualTextField(label = "GSINT *", value = gsint, onValueChange = { gsint = it }, placeholder = "Enter GSINT")
                                DateSelectorField(label = "Date *", value = expenseDate, onValueChange = { expenseDate = it }, placeholder = "YYYY-MM-DD")
                                ManualTextField(label = "Currency *", value = currency, onValueChange = { currency = it }, placeholder = "Enter currency")
                                detailItems.forEachIndexed { index, item ->
                                    MealsItemBlock(
                                        index = index + 1,
                                        itemName = item.item,
                                        quantity = item.quantity,
                                        price = item.price,
                                        total = item.total,
                                        onItemChange = { updated -> detailItems = detailItems.toMutableList().apply { set(index, item.copy(item = updated)) } },
                                        onQuantityChange = { updated -> detailItems = detailItems.toMutableList().apply { set(index, item.copy(quantity = updated)) } },
                                        onPriceChange = { updated -> detailItems = detailItems.toMutableList().apply { set(index, item.copy(price = updated)) } },
                                        onTotalChange = { updated -> detailItems = detailItems.toMutableList().apply { set(index, item.copy(total = updated)) } },
                                        onDelete = { detailItems = detailItems.filterIndexed { itemIndex, _ -> itemIndex != index } }
                                    )
                                }
                                TextButton(onClick = { detailItems = detailItems + ManualExpenseItemForm() }) {
                                    Text("Add new item", color = PrimaryRed, fontFamily = GraphikFontFamily)
                                }
                                ManualTextField(label = "SGST Percentage *", value = sgstPercent, onValueChange = { sgstPercent = it }, placeholder = "Enter SGST %")
                                ManualTextField(label = "SGST amount *", value = sgstAmount, onValueChange = { sgstAmount = it }, placeholder = "Enter SGST amount")
                                ManualTextField(label = "CGST Percentage *", value = cgstPercent, onValueChange = { cgstPercent = it }, placeholder = "Enter CGST %")
                                ManualTextField(label = "CGST amount *", value = cgstAmount, onValueChange = { cgstAmount = it }, placeholder = "Enter CGST amount")
                                ManualTextField(label = "IGST Percentage *", value = igstPercent, onValueChange = { igstPercent = it }, placeholder = "Enter IGST %")
                                ManualTextField(label = "IGST amount *", value = igstAmount, onValueChange = { igstAmount = it }, placeholder = "Enter IGST amount")
                                ManualTextField(label = "Tax amount *", value = taxAmount, onValueChange = { taxAmount = it }, placeholder = "Enter tax amount")
                                ManualTextField(label = "Amount *", value = amount, onValueChange = { amount = it }, placeholder = "Enter amount")
                                ManualTextField(label = "Notes", value = notes, onValueChange = { notes = it }, placeholder = "Enter notes")
                            }

                            if (category in listOf("Flight Receipt", "Flight Invoice", "Stationery", "Fuel gas", "Entertainment", "Information Technology", "Other")) {
                                ManualTextField(label = "Notes", value = notes, onValueChange = { notes = it }, placeholder = "Enter notes")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    onProjectSelected(projectId, customerId, soNumber.ifBlank { null }, tripId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryRed,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "Save expense", fontFamily = GraphikFontFamily, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ManualDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value.ifBlank { "Select" },
                onValueChange = {},
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    disabledTextColor = if (value.isBlank()) Color.Gray else Color.Black,
                    disabledBorderColor = Color(0xFFD4D4D4),
                    disabledTrailingIconColor = Color.Gray
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(onClick = {
                        onValueChange(option)
                        expanded = false
                    }) {
                        Text(
                            option,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = placeholder, color = Color.Gray, fontFamily = GraphikFontFamily)
            },
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color(0xFFD4D4D4),
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectorField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "YYYY-MM-DD",
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        onValueChange(dateFormatter.format(Date(selectedMillis)))
                    }
                    showDatePicker = false
                }) {
                    Text("OK", fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", fontFamily = GraphikFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
            placeholder = {
                Text(text = placeholder, color = Color.Gray, fontFamily = GraphikFontFamily)
            },
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = PrimaryRed)
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color(0xFFD4D4D4),
            ),
        )
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

@Composable
private fun TravelItemBlock(
    index: Int,
    passengerName: String,
    seatNumber: String,
    seatLabel: String = "Seat number",
    onPassengerNameChange: (String) -> Unit,
    onSeatNumberChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFF8F8F8),
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Item $index",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.Black,
            )
            ManualTextField(label = "Passenger name", value = passengerName, onValueChange = onPassengerNameChange, placeholder = "Enter passenger name")
            ManualTextField(label = seatLabel, value = seatNumber, onValueChange = onSeatNumberChange, placeholder = "Enter value")
        }
    }
}

@Composable
private fun MealsItemBlock(
    index: Int,
    itemName: String,
    quantity: String,
    price: String,
    total: String,
    onItemChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onTotalChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFF8F8F8),
        elevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Item $index",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.Black,
            )
            ManualTextField(label = "Item", value = itemName, onValueChange = onItemChange, placeholder = "Enter item")
            ManualTextField(label = "Quantity", value = quantity, onValueChange = onQuantityChange, placeholder = "Enter quantity")
            ManualTextField(label = "Price", value = price, onValueChange = onPriceChange, placeholder = "Enter price")
            ManualTextField(label = "Total", value = total, onValueChange = onTotalChange, placeholder = "Enter total")
        }
    }
}

@Composable
private fun PopupOptionButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontFamily = GraphikFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}
