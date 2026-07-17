package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.provider.OpenableColumns
import android.graphics.Bitmap
import android.net.Uri
import android.Manifest
import android.R.attr.bitmap
import android.content.pm.PackageManager
import android.widget.Toast
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
import com.archeGlobal.one.network.ExpenseUi

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

private enum class ExpenseTab { Drafts, Submitted }

private data class DraftExpenseItem(
    val id: String,
    val billDate: String,
    val vendorName: String,
    val category: String,
    val amount: String,
    val status: String,
    val uploadedAt: String
)

private data class SubmittedExpenseItem(
    val id: String,
    val category: String,
    val description: String,
    val payment: String,
    val amount: String,
    val date: String,
    val status: String = "Submitted"
)

@Composable
fun ExpenseExtractionViewScreen(onBack: () -> Unit) {
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf(ExpenseTab.Drafts) }
    var showAddPopup by rememberSaveable { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val controller = remember { ExpenseController(context) }

    val allExpenses by controller.expenses
    val isLoading by controller.isLoading
    val errorMessage by controller.errorMessage

    LaunchedEffect(Unit) {
        controller.fetchExpenses()
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
                        Toast.makeText(context, "Uploaded: ${'$'}{resp.name}", Toast.LENGTH_SHORT).show()
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

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val input = context.contentResolver.openInputStream(it)
                val bytes = input?.use { stream -> stream.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    val filename = queryName(context, it) ?: it.lastPathSegment ?: "upload"
                    controller.uploadExpense(bytes, filename, "application/octet-stream",
                        onSuccess = { resp ->
                            Toast.makeText(context, "Uploaded: ${'$'}{resp.name}", Toast.LENGTH_SHORT).show()
                        },
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                    )
                } else {
                    Toast.makeText(context, "Failed to read file", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${'$'}{e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val draftStatuses = setOf("Uploaded", "Extracted", "Extracting")
    val filteredDrafts = allExpenses.filter { expense ->
        expense.status in draftStatuses && (searchText.isBlank() || expense.id.contains(searchText, ignoreCase = true) || expense.vendorName.contains(searchText, ignoreCase = true))
    }
    val filteredSubmitted = allExpenses.filter { expense ->
        expense.status !in draftStatuses && (searchText.isBlank() || expense.id.contains(searchText, ignoreCase = true) || expense.category.contains(searchText, ignoreCase = true))
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
                                uploadedAt = exp.createdAt
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                ExpenseTab.Submitted -> {
                    SubmittedListContent(
                        expenses = filteredSubmitted.map { exp ->
                            SubmittedExpenseItem(
                                id = "EXP-${exp.id}",
                                category = exp.category,
                                description = exp.name,
                                payment = "",
                                amount = exp.totalAmount,
                                date = exp.billDate
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
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
            onManual = { showAddPopup = false }
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
                    DraftExpenseCard(expense = expense)
                }
            }
        }
    }
}

@Composable
private fun SubmittedListContent(
    expenses: List<SubmittedExpenseItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "My reimbursements",
            fontFamily = GraphikFontFamily,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (expenses.isEmpty()) {
            EmptyExpenseState(message = "No ERP Ready Expenses found")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(expenses) { expense ->
                    SubmittedExpenseCard(expense = expense)
                }
            }
        }
    }
}

@Composable
private fun DraftExpenseCard(expense: DraftExpenseItem) {
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
        ExpenseDetailRow("Bill Date", expense.billDate)
        ExpenseDetailRow("Vendor Name", expense.vendorName)
        ExpenseDetailRow("Category", expense.category)
        ExpenseDetailRow("Uploaded At", expense.uploadedAt)
        Divider(color = Color(0xFFEEEEEE))
        ViewDetailsRow()
    }
}

@Composable
private fun SubmittedExpenseCard(expense: SubmittedExpenseItem) {
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
                color = Color(0xFF2E7D32),
                fontFamily = GraphikFontFamily,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Divider(color = Color(0xFFEEEEEE))
        ExpenseDetailRow("Category", expense.category)
        ExpenseDetailRow("Description", expense.description)
        ExpenseDetailRow("Payment", expense.payment)
        ExpenseDetailRow("Amount", expense.amount)
        ExpenseDetailRow("Date", expense.date)
        Divider(color = Color(0xFFEEEEEE))
        ViewDetailsRow()
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
private fun ViewDetailsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = null,
            tint = PrimaryRed,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "View Details",
            color = PrimaryRed,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
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
