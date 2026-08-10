package com.archeGlobal.one.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.archeGlobal.one.ImageViewerActivity
import com.archeGlobal.one.PdfViewerActivity
import com.archeGlobal.one.controller.ExpenseController
import com.archeGlobal.one.controller.OtpVerificationController
import com.archeGlobal.one.controller.TravelExpenseController
import com.archeGlobal.one.network.ExpenseDetailUi
import com.archeGlobal.one.network.ExpenseSubmitErrorDetail
import com.archeGlobal.one.network.ExpenseSubmitRequest
import com.archeGlobal.one.network.ExpenseSubmitResponse
import com.archeGlobal.one.network.ExpenseUserOption
import com.archeGlobal.one.network.SplitExpenseRequest
import com.google.gson.JsonObject
import com.archeGlobal.one.network.SplitUpdatedData
import com.archeGlobal.one.network.SplitUserAmount
import com.archeGlobal.one.network.ensureSplitExtractedData
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.util.Locale
import kotlin.math.roundToLong

private fun shouldShowDetailField(isReadOnly: Boolean, value: String): Boolean =
    !isReadOnly || value.isNotBlank()

private fun String.normalizeExpenseCategory(): String = trim().lowercase(Locale.getDefault())

fun isPdfUrl(fileUrl: String?): Boolean {
    val normalized = fileUrl?.trim().orEmpty().lowercase(Locale.getDefault())
    return normalized.endsWith(".pdf") || normalized.contains(".pdf?") || normalized.contains(".pdf#")
}

fun isImageUrl(fileUrl: String?): Boolean {
    val normalized = fileUrl?.trim().orEmpty().lowercase(Locale.getDefault())
    return normalized.endsWith(".jpg") || normalized.endsWith(".jpeg") || normalized.endsWith(".png") ||
        normalized.endsWith(".gif") || normalized.endsWith(".webp") || normalized.endsWith(".bmp") ||
        normalized.contains(".jpg?") || normalized.contains(".jpeg?") || normalized.contains(".png?") ||
        normalized.contains(".gif?") || normalized.contains(".webp?") || normalized.contains(".bmp?")
}

@Composable
fun ExpenseExtractionDetailViewScreen(
    expense: ExpenseDetailUi,
    onBack: () -> Unit,
    isReadOnly: Boolean = false,
    approvalMode: Boolean = false,
) {
    val context = LocalContext.current
    val tripController = remember { TravelExpenseController(context) }
    val expenseController = remember { ExpenseController(context) }
    val projectOptions by tripController.projectOptions
    val trips by tripController.trips
    val userOptions by expenseController.userOptions
    val userOptionsLoading by expenseController.userOptionsLoading
    val splitLoading by expenseController.splitLoading
    val scrollState = rememberScrollState()

    var projectId by rememberSaveable(expense.id) { mutableStateOf(expense.projectId) }
    var tripId by rememberSaveable(expense.id) { mutableStateOf("") }
    var customerId by rememberSaveable(expense.id) { mutableStateOf(expense.customerId) }
    var soNumber by rememberSaveable(expense.id) {
        mutableStateOf(expense.soNumber.ifBlank { expense.documentId })
    }
    var showLimitExceededDialog by rememberSaveable { mutableStateOf(false) }
    var limitExceededDetail by rememberSaveable { mutableStateOf<ExpenseSubmitErrorDetail?>(null) }
    var limitDialogNote by rememberSaveable(expense.id) { mutableStateOf(expense.note) }
    var showSubmitConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showSubmitSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var submitSuccessMessage by rememberSaveable { mutableStateOf("") }
    var duplicateExpenseDetail by rememberSaveable { mutableStateOf<ExpenseSubmitErrorDetail?>(null) }
    var viewedDuplicateExpense by remember { mutableStateOf<ExpenseDetailUi?>(null) }
    var showApproveConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showRejectConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showApprovalResultDialog by rememberSaveable { mutableStateOf(false) }
    var approvalResultTitle by rememberSaveable { mutableStateOf("") }
    var approvalResultMessage by rememberSaveable { mutableStateOf("") }
    val userExpenseStatusLoading by expenseController.userExpenseStatusLoading
    val isApprovalDecided = expense.status.equals("approved", ignoreCase = true) ||
        expense.status.equals("rejected", ignoreCase = true)

    var category by rememberSaveable(expense.id) { mutableStateOf(expense.category) }
    var mode by rememberSaveable(expense.id) { mutableStateOf(expense.subCategory) }
    var accommodationType by rememberSaveable(expense.id) {
        mutableStateOf(expense.accommodationType)
    }
    var hotelName by rememberSaveable(expense.id) { mutableStateOf(expense.hotelName) }
    var gstinOfHotel by rememberSaveable(expense.id) { mutableStateOf(expense.gstinOfHotel) }
    var currency by rememberSaveable(expense.id) { mutableStateOf(expense.currency.ifBlank { "INR" }) }
    var checkIn by rememberSaveable(expense.id) { mutableStateOf(expense.checkIn) }
    var checkOut by rememberSaveable(expense.id) { mutableStateOf(expense.checkOut) }
    var noOfNights by rememberSaveable(expense.id) { mutableStateOf(expense.noOfNights) }
    var roomType by rememberSaveable(expense.id) { mutableStateOf(expense.roomType) }
    var serviceCharges by rememberSaveable(expense.id) { mutableStateOf(expense.serviceCharges) }
    var sgstAmount by rememberSaveable(expense.id) { mutableStateOf(expense.sgstAmount) }
    var cgstAmount by rememberSaveable(expense.id) { mutableStateOf(expense.cgstAmount) }
    var igstAmount by rememberSaveable(expense.id) { mutableStateOf(expense.igstAmount) }
    var amount by rememberSaveable(expense.id) { mutableStateOf(expense.amount) }
    var note by rememberSaveable(expense.id) { mutableStateOf(expense.note) }
    var showDocumentViewer by rememberSaveable { mutableStateOf(false) }
    var showSplitDialog by rememberSaveable { mutableStateOf(false) }

    if (viewedDuplicateExpense != null) {
        ExpenseExtractionDetailViewScreen(
            expense = viewedDuplicateExpense!!,
            onBack = { viewedDuplicateExpense = null },
            isReadOnly = true,
        )
        return
    }

    BackHandler {
        onBack()
    }

    LaunchedEffect(Unit) {
        tripController.fetchProjectOptions()
        tripController.fetchTrips()
    }

    val selectedProjectOption = projectOptions.firstOrNull { it.code.equals(projectId, ignoreCase = true) }
    val selectedProjectNumericId = selectedProjectOption?.id
    val selectedTripNumericId = trips.firstOrNull {
        it.tripCode.equals(tripId, ignoreCase = true) || it.requestId == tripId
    }?.requestId?.toIntOrNull()

    val projectCodes = projectOptions.map { it.code }.ifEmpty {
        listOfNotNull(expense.projectId.takeIf { it.isNotBlank() })
    }
    val tripCodes = trips.map { it.tripCode }.filter { it.isNotBlank() && it != "-" }

    fun buildSubmitRequest(submitBehavior: String? = null, noteOverride: String? = null): ExpenseSubmitRequest {
        val payloadData = expense.extractedData?.deepCopy() ?: JsonObject()
        return ExpenseSubmitRequest(
            projectId = selectedProjectNumericId,
            tripId = if (selectedProjectNumericId == null) selectedTripNumericId else null,
            flightClass = "economy",
            trainClass = if (expense.category.contains("train", ignoreCase = true)) {
                "tier_3"
            } else {
                null
            },
            accommodationType = accommodationType.lowercase(Locale.getDefault())
                .takeIf { it == "domestic" || it == "international" },
            data = payloadData,
            note = (noteOverride ?: note).ifBlank { null },
            submitBehavior = submitBehavior,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,
                        WelcomeBackgroundMiddle,
                        WelcomeBackgroundBottom,
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        text = "Expense Details",
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "EXP-${expense.userExpenseId}",
                    fontFamily = GraphikFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                )
                Text(
                    text = if (isReadOnly) {
                        "View submitted expense details"
                    } else {
                        "Review extracted bill details and complete required fields"
                    },
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                )

                OriginalFileBlock(
                    fileName = expense.name,
                    status = expense.status,
                    uploadedAt = expense.createdAt,
                    onOpenFile = {
                        val fileUrl = expense.fileUrl.orEmpty()
                        if (fileUrl.isBlank()) {
                            Toast.makeText(context, "File URL not available", Toast.LENGTH_SHORT).show()
                        } else if (isPdfUrl(fileUrl)) {
                            context.startActivity(Intent(context, PdfViewerActivity::class.java).apply {
                                putExtra(PdfViewerActivity.EXTRA_FILE_URL, fileUrl)
                                putExtra(PdfViewerActivity.EXTRA_TITLE, expense.name.ifBlank { "Document" })
                            })
                        } else if (isImageUrl(fileUrl)) {
                            context.startActivity(Intent(context, ImageViewerActivity::class.java).apply {
                                putExtra("fileUrl", fileUrl)
                                putExtra("title", expense.name.ifBlank { "Image" })
                            })
                        } else {
                            showDocumentViewer = true
                        }
                    },
                )

                DetailSectionCard(title = "Assignment Details") {
                    DetailDropdownField(
                        label = "Project ID",
                        value = projectId,
                        options = projectCodes,
                        onValueChange = { value ->
                            projectId = value
                            projectOptions.firstOrNull { it.code.equals(value, ignoreCase = true) }
                                ?.let { selected ->
                                    customerId = selected.customerId.orEmpty()
                                    soNumber = selected.soNumber?.takeIf { it.isNotBlank() }
                                        ?: soNumber
                                }
                        },
                        enabled = !isReadOnly,
                    )
                    DetailDropdownField(
                        label = "Trip ID",
                        value = tripId,
                        options = tripCodes,
                        onValueChange = { tripId = it },
                        enabled = !isReadOnly,
                    )
                    DetailLabeledField(
                        label = "Customer ID",
                        value = customerId,
                        onValueChange = { customerId = it },
                        placeholder = "Enter customer ID",
                        enabled = !isReadOnly,
                    )
                    if (expense.customerName.isNotBlank()) {
                        DetailLabeledField(
                            label = "Customer Name",
                            value = expense.customerName,
                            onValueChange = {},
                            placeholder = "",
                            enabled = false,
                        )
                    }
                    DetailLabeledField(
                        label = "SO Number",
                        value = soNumber,
                        onValueChange = { soNumber = it },
                        placeholder = "Enter SO number",
                        enabled = !isReadOnly,
                    )
                }

                DetailSectionCard(title = "Extracted Data") {
                    val normalizedCategory = category.normalizeExpenseCategory()
                    val normalizedMode = mode.normalizeExpenseCategory()
                    val isTravelCategory = normalizedCategory == "travel" || normalizedCategory.contains("travel")
                    val isHotelCategory = normalizedCategory == "hotel accommodation" || normalizedCategory.contains("hotel")

                    if (shouldShowDetailField(isReadOnly, category)) {
                        DetailLabeledField(
                            label = "Category",
                            value = category,
                            onValueChange = { category = it },
                            placeholder = "Category",
                            enabled = !isReadOnly,
                        )
                    }
                    if (shouldShowDetailField(isReadOnly, mode)) {
                        DetailLabeledField(
                            label = "Mode",
                            value = mode,
                            onValueChange = { mode = it },
                            placeholder = "Mode",
                            enabled = !isReadOnly,
                        )
                    }

                    if (isHotelCategory) {
                        if (shouldShowDetailField(isReadOnly, accommodationType)) {
                            DetailDropdownField(
                                label = "Accommodation Type",
                                value = accommodationType,
                                options = listOf("Domestic", "International"),
                                onValueChange = { accommodationType = it },
                                enabled = !isReadOnly,
                            )
                        }
                        if (shouldShowDetailField(isReadOnly, hotelName)) {
                            DetailLabeledField(
                                label = "Hotel Name",
                                value = hotelName,
                                onValueChange = { hotelName = it },
                                placeholder = "Hotel name",
                                enabled = !isReadOnly,
                            )
                        }
                        if (shouldShowDetailField(isReadOnly, gstinOfHotel)) {
                            DetailLabeledField(
                                label = "GSTIN of Hotel",
                                value = gstinOfHotel,
                                onValueChange = { gstinOfHotel = it },
                                placeholder = "GSTIN",
                                enabled = !isReadOnly,
                            )
                        }
                    }

                    if (shouldShowDetailField(isReadOnly, currency)) {
                        DetailLabeledField(
                            label = "Currency",
                            value = currency,
                            onValueChange = { currency = it },
                            placeholder = "Currency",
                            enabled = !isReadOnly,
                        )
                    }

                    if (isHotelCategory) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (shouldShowDetailField(isReadOnly, checkIn)) {
                                DetailLabeledField(
                                    label = "Check In",
                                    value = checkIn,
                                    onValueChange = { checkIn = it },
                                    placeholder = "YYYY-MM-DD",
                                    modifier = Modifier.weight(1f),
                                    enabled = !isReadOnly,
                                )
                            }
                            if (shouldShowDetailField(isReadOnly, checkOut)) {
                                DetailLabeledField(
                                    label = "Check Out",
                                    value = checkOut,
                                    onValueChange = { checkOut = it },
                                    placeholder = "YYYY-MM-DD",
                                    modifier = Modifier.weight(1f),
                                    enabled = !isReadOnly,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (shouldShowDetailField(isReadOnly, noOfNights)) {
                                DetailLabeledField(
                                    label = "No of Nights",
                                    value = noOfNights,
                                    onValueChange = { noOfNights = it },
                                    placeholder = "0",
                                    modifier = Modifier.weight(1f),
                                    enabled = !isReadOnly,
                                )
                            }
                            if (shouldShowDetailField(isReadOnly, roomType)) {
                                DetailLabeledField(
                                    label = "Room Type",
                                    value = roomType,
                                    onValueChange = { roomType = it },
                                    placeholder = "Room type",
                                    modifier = Modifier.weight(1f),
                                    enabled = !isReadOnly,
                                )
                            }
                        }
                    } else if (isTravelCategory) {
                        if (shouldShowDetailField(isReadOnly, checkIn)) {
                            DetailLabeledField(
                                label = if (normalizedMode == "train") "Departure" else "Boarding Date",
                                value = checkIn,
                                onValueChange = { checkIn = it },
                                placeholder = "YYYY-MM-DD",
                                enabled = !isReadOnly,
                            )
                        }
                        if (shouldShowDetailField(isReadOnly, checkOut)) {
                            DetailLabeledField(
                                label = if (normalizedMode == "train") "Destination" else "Dropping Date",
                                value = checkOut,
                                onValueChange = { checkOut = it },
                                placeholder = "YYYY-MM-DD",
                                enabled = !isReadOnly,
                            )
                        }
                    }

                    if (shouldShowDetailField(isReadOnly, serviceCharges)) {
                        DetailLabeledField(
                            label = "Service Charges",
                            value = serviceCharges,
                            onValueChange = { serviceCharges = it },
                            placeholder = "0.00",
                            enabled = !isReadOnly,
                        )
                    }
                    if (shouldShowDetailField(isReadOnly, sgstAmount)) {
                        DetailLabeledField(
                            label = "SGST Amount",
                            value = sgstAmount,
                            onValueChange = { sgstAmount = it },
                            placeholder = "0.00",
                            enabled = !isReadOnly,
                        )
                    }
                    if (shouldShowDetailField(isReadOnly, cgstAmount)) {
                        DetailLabeledField(
                            label = "CGST Amount",
                            value = cgstAmount,
                            onValueChange = { cgstAmount = it },
                            placeholder = "0.00",
                            enabled = !isReadOnly,
                        )
                    }
                    if (shouldShowDetailField(isReadOnly, igstAmount)) {
                        DetailLabeledField(
                            label = "IGST Amount",
                            value = igstAmount,
                            onValueChange = { igstAmount = it },
                            placeholder = "0.00",
                            enabled = !isReadOnly,
                        )
                    }
                    if (shouldShowDetailField(isReadOnly, amount)) {
                        DetailLabeledField(
                            label = "Amount",
                            value = amount,
                            onValueChange = { amount = it },
                            placeholder = "0.00",
                            enabled = !isReadOnly,
                        )
                    }
                    if (shouldShowDetailField(isReadOnly, note)) {
                        DetailLabeledField(
                            label = "Note",
                            value = note,
                            onValueChange = { note = it },
                            placeholder = "Add a note",
                            singleLine = false,
                            minLines = 3,
                            enabled = !isReadOnly,
                        )
                    }
                }

                if (approvalMode && !isApprovalDecided) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = { showRejectConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFF3E8E8),
                            contentColor = PrimaryRed,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !userExpenseStatusLoading,
                    ) {
                        Text(
                            text = "Reject",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                    Button(
                        onClick = { showApproveConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryRed,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !userExpenseStatusLoading,
                    ) {
                        if (userExpenseStatusLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Approve",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
                } else if (!isReadOnly) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            expenseController.fetchUserOptions(
                                onSuccess = { showSplitDialog = true },
                                onError = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                },
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFF6F4EE),
                            contentColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !userOptionsLoading && !splitLoading,
                    ) {
                        if (userOptionsLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryRed,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Split Bill",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (projectId.isBlank()) {
                                Toast.makeText(context, "Project ID is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (customerId.isBlank()) {
                                Toast.makeText(context, "Customer ID is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (soNumber.isBlank()) {
                                Toast.makeText(context, "SO Number is required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            showSubmitConfirmDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryRed,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = "Submit",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                }
                }
            }
        }
    }

    if (showDocumentViewer && !expense.fileUrl.isNullOrBlank()) {
        ExpenseDocumentViewerDialog(
            fileUrl = expense.fileUrl,
            fileName = expense.name,
            onDismiss = { showDocumentViewer = false },
        )
    }

    if (showSubmitConfirmDialog) {
        ExpenseConfirmationDialog(
            title = "Submit Expense",
            message = "Are you sure you want to submit this expense for approval?",
            confirmText = "Submit",
            onConfirm = {
                showSubmitConfirmDialog = false
                expenseController.submitExpense(
                    expenseId = expense.id,
                    request = buildSubmitRequest(),
                    onSuccess = { response ->
                        submitSuccessMessage = "Expense submitted successfully"
                        showSubmitSuccessDialog = true
                    },
                    onLimitExceeded = { detail ->
                        limitExceededDetail = detail
                        limitDialogNote = note
                        showLimitExceededDialog = true
                    },
                    onDuplicateExpense = { detail ->
                        duplicateExpenseDetail = detail
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    },
                )
            },
            onDismiss = { showSubmitConfirmDialog = false },
        )
    }

    if (showSubmitSuccessDialog) {
        ExpenseConfirmationDialog(
            title = "Success",
            message = submitSuccessMessage,
            confirmText = "OK",
            showDismissButton = false,
            onConfirm = {
                showSubmitSuccessDialog = false
                onBack()
            },
            onDismiss = {
                showSubmitSuccessDialog = false
                onBack()
            },
        )
    }

    if (showApproveConfirmDialog) {
        ExpenseConfirmationDialog(
            title = "Approve Expense?",
            message = "This expense exceeds the allowed limit. Do you want to proceed with approval?",
            confirmText = "Approve",
            onConfirm = {
                showApproveConfirmDialog = false
                val userExpenseId = expense.userExpenseId ?: expense.id.toIntOrNull()
                if (userExpenseId == null) {
                    Toast.makeText(context, "Unable to identify this expense", Toast.LENGTH_LONG).show()
                } else {
                    expenseController.updateUserExpenseStatus(
                        userExpenseId = userExpenseId,
                        status = "APPROVED",
                        onSuccess = {
                            approvalResultTitle = "Expense approved"
                            approvalResultMessage = "The full amount has been approved successfully. This request is now complete."
                            showApprovalResultDialog = true
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        },
                    )
                }
            },
            onDismiss = { showApproveConfirmDialog = false },
        )
    }

    if (showRejectConfirmDialog) {
        ExpenseConfirmationDialog(
            title = "Do you want to reject this expense?",
            message = "You're about to reject the expense. This action cannot be undone.",
            confirmText = "Reject",
            onConfirm = {
                showRejectConfirmDialog = false
                val userExpenseId = expense.userExpenseId ?: expense.id.toIntOrNull()
                if (userExpenseId == null) {
                    Toast.makeText(context, "Unable to identify this expense", Toast.LENGTH_LONG).show()
                } else {
                    expenseController.updateUserExpenseStatus(
                        userExpenseId = userExpenseId,
                        status = "REJECTED",
                        onSuccess = {
                            Toast.makeText(context, "Expense rejected", Toast.LENGTH_SHORT).show()
                            onBack()
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        },
                    )
                }
            },
            onDismiss = { showRejectConfirmDialog = false },
        )
    }

    if (showApprovalResultDialog) {
        ExpenseConfirmationDialog(
            title = approvalResultTitle,
            message = approvalResultMessage,
            confirmText = "OK",
            showDismissButton = false,
            onConfirm = {
                showApprovalResultDialog = false
                onBack()
            },
            onDismiss = {
                showApprovalResultDialog = false
                onBack()
            },
        )
    }

    if (showLimitExceededDialog && limitExceededDetail != null) {
        ExpenseLimitExceededDialog(
            detail = limitExceededDetail!!,
            currency = currency.ifBlank { expense.currency.ifBlank { "INR" } },
            note = limitDialogNote,
            onNoteChange = { limitDialogNote = it },
            onSubmitManager = {
                showLimitExceededDialog = false
                expenseController.submitExpense(
                    expenseId = expense.id,
                    request = buildSubmitRequest(submitBehavior = "submit_to_manager", noteOverride = limitDialogNote),
                    onSuccess = { response ->
                        submitSuccessMessage = "Expense submitted to manager"
                        showSubmitSuccessDialog = true
                    },
                    onLimitExceeded = { _ -> },
                    onDuplicateExpense = { detail ->
                        duplicateExpenseDetail = detail
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    },
                )
            },
            onSubmitLimit = {
                showLimitExceededDialog = false
                expenseController.submitExpense(
                    expenseId = expense.id,
                    request = buildSubmitRequest(noteOverride = limitDialogNote),
                    onSuccess = { response ->
                        submitSuccessMessage = "Expense submitted within limit"
                        showSubmitSuccessDialog = true
                    },
                    onLimitExceeded = { _ -> },
                    onDuplicateExpense = { detail ->
                        duplicateExpenseDetail = detail
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    },
                )
            },
            onDismiss = {
                showLimitExceededDialog = false
            },
        )
    }

    if (duplicateExpenseDetail != null) {
        val detail = duplicateExpenseDetail!!
        ExpenseDuplicateDialog(
            fileName = expense.name,
            billDate = formatDuplicateBillDate(expense.billDate),
            onViewExisting = {
                val existingId = detail.existingExpenseId
                if (existingId == null) {
                    duplicateExpenseDetail = null
                    Toast.makeText(context, "Existing expense could not be found", Toast.LENGTH_LONG).show()
                } else {
                    expenseController.fetchExpenseDetail(
                        expenseId = existingId.toString(),
                        onSuccess = { existing ->
                            duplicateExpenseDetail = null
                            viewedDuplicateExpense = existing
                        },
                        onError = { message ->
                            duplicateExpenseDetail = null
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        },
                    )
                }
            },
            onSkip = {
                duplicateExpenseDetail = null
                onBack()
            },
        )
    }

    if (showSplitDialog && !isReadOnly) {
        val selectedProjectNumericId = projectOptions.firstOrNull { it.code.equals(projectId, ignoreCase = true) }?.id
        val selectedTripNumericId = trips.firstOrNull {
            it.tripCode.equals(tripId, ignoreCase = true) || it.requestId == tripId
        }?.requestId?.toIntOrNull()

        // API allows either project_id OR trip_id — never both.
        val splitProjectId = if (selectedTripNumericId == null) selectedProjectNumericId else null
        val splitTripId = selectedTripNumericId

        SplitExpensesDialog(
            totalAmountText = amount.ifBlank { expense.amount },
            currency = currency.ifBlank { expense.currency }.ifBlank { "INR" },
            userOptions = userOptions,
            currentUserId = expense.ownerUserId
                ?: expenseController.getStoredExpenseUserId().takeIf { it > 0 },
            currentUserFallbackName = OtpVerificationController.getUserData()?.name
                ?: expense.ownerUserEmail
                ?: "You",
            isSubmitting = splitLoading,
            onDismiss = { showSplitDialog = false },
            onSplit = { currentUser, currentAmount, selectedUsers, selectedAmounts ->
                if (currentUser.id <= 0) {
                    Toast.makeText(
                        context,
                        "Unable to identify current user. Please reopen Travel Expense.",
                        Toast.LENGTH_LONG,
                    ).show()
                } else {
                    val existingDate = expense.extractedData
                        ?.getAsJsonObject("date")
                        ?.get("value")
                        ?.takeUnless { it.isJsonNull }
                        ?.asString
                        ?.trim()
                        .orEmpty()
                    val preferredDate = existingDate.ifBlank {
                        formatSplitDisplayDate(
                            checkIn.ifBlank { expense.checkIn }.ifBlank { expense.billDate },
                        )
                    }
                    val splitData = ensureSplitExtractedData(
                        source = expense.extractedData,
                        preferredDate = preferredDate,
                    )
                    val hasDate = splitData.getAsJsonObject("date")
                        ?.get("value")
                        ?.takeUnless { it.isJsonNull }
                        ?.asString
                        ?.isNotBlank() == true
                    if (!hasDate) {
                        Toast.makeText(
                            context,
                            "Date is required in expense data.",
                            Toast.LENGTH_LONG,
                        ).show()
                    } else {
                        val request = SplitExpenseRequest(
                            updatedData = SplitUpdatedData(
                                data = splitData,
                                note = note,
                                projectId = splitProjectId,
                                tripId = splitTripId,
                                accommodationType = accommodationType.lowercase(Locale.getDefault())
                                    .takeIf { it == "domestic" || it == "international" },
                                trainClass = if (expense.category.contains("train", ignoreCase = true)) {
                                    "tier_3"
                                } else {
                                    null
                                },
                            ),
                            users = selectedUsers.zip(selectedAmounts).map { (user, share) ->
                                SplitUserAmount(userId = user.id, amount = share)
                            },
                        )
                        expenseController.splitExpense(
                            expenseId = expense.id,
                            request = request,
                            onSuccess = {
                                showSplitDialog = false
                                Toast.makeText(context, "Expense split successfully", Toast.LENGTH_SHORT).show()
                                onBack()
                            },
                            onError = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            },
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun ExpenseConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    showDismissButton: Boolean = true,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            backgroundColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black,
                )
                Text(
                    text = message,
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (showDismissButton) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFFF0F0F0),
                                contentColor = Color.Black,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(text = "Cancel", fontFamily = GraphikFontFamily, fontWeight = FontWeight.Medium)
                        }
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryRed,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(text = confirmText, fontFamily = GraphikFontFamily, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseDuplicateDialog(
    fileName: String,
    billDate: String,
    onViewExisting: () -> Unit,
    onSkip: () -> Unit,
) {
    val details = listOfNotNull(
        fileName.takeIf { it.isNotBlank() },
        billDate.takeIf { it.isNotBlank() },
    ).joinToString(", ")

    Dialog(
        onDismissRequest = onSkip,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            backgroundColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(36.dp),
                )
                Text(
                    text = "Duplicate bill detected",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black,
                )
                Text(
                    text = "A bill with the details — $details already exists. You can either view the existing bill or skip this one.",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onViewExisting,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFF0F0F0),
                            contentColor = Color.Black,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "View existing bill",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Button(
                        onClick = onSkip,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryRed,
                            contentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "Skip duplicate",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseLimitExceededDialog(
    detail: ExpenseSubmitErrorDetail,
    currency: String,
    note: String,
    onNoteChange: (String) -> Unit,
    onSubmitManager: () -> Unit,
    onSubmitLimit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val totalAmount = detail.totalAmount ?: 0.0
    val approvalLimit = detail.approvalLimit ?: 0.0
    fun money(value: Double) = "$currency ${String.format(Locale.getDefault(), "%.2f", value)}"
    val explanation = when {
        detail.totalAmount != null -> "Your expense of ${money(totalAmount)} exceeds your grade limit of " +
            "${money(approvalLimit)}. You can auto-approve ${money(approvalLimit)} within your limit, " +
            "or send the full amount to your manager for review."
        detail.dailyLimit != null || detail.monthlyLimit != null -> buildString {
            append(detail.message?.takeIf { it.isNotBlank() } ?: "Your available spending limit has been fully used.")
            if (detail.dailyLimit != null) {
                append(" Daily: ${money(detail.dailySpent ?: 0.0)} of ${money(detail.dailyLimit)} used.")
            }
            if (detail.monthlyLimit != null) {
                append(" Monthly: ${money(detail.monthlySpent ?: 0.0)} of ${money(detail.monthlyLimit)} used.")
            }
            append(" You can send this expense to your manager for review.")
        }
        else -> detail.message?.takeIf { it.isNotBlank() } ?: "Your reimbursement amount exceeds your limit."
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            backgroundColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Reimbursement limit exceeded",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.Black,
                )
                Text(
                    text = "Your reimbursement amount exceeds your limit",
                    fontFamily = GraphikFontFamily,
                    fontSize = 15.sp,
                    color = Color.Black,
                )
                Text(
                    text = explanation,
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
                Text(
                    text = "Note",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = {
                        Text(
                            text = "Add a note for manager or approval",
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color(0xFFF6F4EE),
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = Color.LightGray,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onSubmitManager,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryRed,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = "Submit for manager review",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitExpensesDialog(
    totalAmountText: String,
    currency: String,
    userOptions: List<ExpenseUserOption>,
    currentUserId: Int?,
    currentUserFallbackName: String,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSplit: (
        currentUser: ExpenseUserOption,
        currentAmount: Double,
        selectedUsers: List<ExpenseUserOption>,
        selectedAmounts: List<Double>,
    ) -> Unit,
) {
    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf<List<ExpenseUserOption>>(emptyList()) }

    val currentUser = remember(userOptions, currentUserId, currentUserFallbackName) {
        userOptions.firstOrNull { it.id == currentUserId }
            ?: ExpenseUserOption(
                id = currentUserId ?: -1,
                firstName = currentUserFallbackName,
                lastName = null,
            )
    }

    val totalAmount = remember(totalAmountText) { parseAmount(totalAmountText) }
    val memberOptions = remember(userOptions, currentUser.id) {
        userOptions.filter { it.id != currentUser.id }
    }
    val filteredMembers = remember(memberOptions, searchText) {
        if (searchText.isBlank()) memberOptions
        else memberOptions.filter {
            it.displayName.contains(searchText, ignoreCase = true)
        }
    }

    val shares = remember(totalAmount, selectedMembers) {
        splitAmountEqually(totalAmount, selectedMembers.size + 1)
    }
    val currentShare = shares.firstOrNull() ?: totalAmount
    val selectedShares = if (shares.isEmpty()) emptyList() else shares.drop(1)

    fun toggleMember(member: ExpenseUserOption) {
        selectedMembers = if (selectedMembers.any { it.id == member.id }) {
            selectedMembers.filterNot { it.id == member.id }
        } else {
            selectedMembers + member
        }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            backgroundColor = Color.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Split Expenses",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = "Total Amount",
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    color = Color.Gray,
                )
                Text(
                    text = formatMoney(totalAmount, currency),
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = Color.Black,
                )

                Text(
                    text = "Select Member",
                    fontFamily = GraphikFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                )
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "Search member...",
                            color = Color.Gray,
                            fontFamily = GraphikFontFamily,
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color(0xFFFAFAFA),
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color(0xFFD4D4D4),
                    ),
                )

                if (filteredMembers.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        elevation = 2.dp,
                        backgroundColor = Color.White,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            filteredMembers.forEach { member ->
                                val isSelected = selectedMembers.any { it.id == member.id }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toggleMember(member) }
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { toggleMember(member) },
                                    )
                                    Text(
                                        text = member.displayName,
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 15.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(start = 4.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                SplitMemberAmountRow(
                    name = currentUser.displayName,
                    amountText = formatMoney(currentShare, currency),
                    subtitle = "You",
                )

                selectedMembers.forEachIndexed { index, member ->
                    SplitMemberAmountRow(
                        name = member.displayName,
                        amountText = formatMoney(selectedShares.getOrElse(index) { 0.0 }, currency),
                        subtitle = "Shared",
                        onRemove = { toggleMember(member) },
                    )
                }

                Button(
                    onClick = {
                        when {
                            currentUser.id <= 0 -> Unit
                            selectedMembers.isEmpty() -> Unit
                            else -> onSplit(currentUser, currentShare, selectedMembers, selectedShares)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = selectedMembers.isNotEmpty() && currentUser.id > 0 && !isSubmitting && totalAmount > 0,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = PrimaryRed,
                        contentColor = Color.White,
                        disabledBackgroundColor = Color(0xFFCCCCCC),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Split Expenses",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitMemberAmountRow(
    name: String,
    amountText: String,
    subtitle: String,
    onRemove: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF6F4EE), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = name,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.Black,
            )
            Text(
                text = subtitle,
                fontFamily = GraphikFontFamily,
                fontSize = 12.sp,
                color = Color.Gray,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amountText,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black,
            )
            if (onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove member",
                        tint = PrimaryRed,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private fun parseAmount(raw: String): Double {
    val cleaned = raw.replace("Rs", "", ignoreCase = true)
        .replace(",", "")
        .trim()
    return cleaned.toDoubleOrNull() ?: 0.0
}

private fun normalizeExpenseDate(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isBlank() || value == "-") return null

    val patterns = listOf(
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "MM/dd/yyyy",
        "d/M/yyyy",
        "dd MMM yyyy",
    )
    for (pattern in patterns) {
        try {
            val parser = java.text.SimpleDateFormat(pattern, Locale.getDefault())
            parser.isLenient = false
            val date = parser.parse(value) ?: continue
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return formatter.format(date)
        } catch (_: Exception) {
            // try next pattern
        }
    }
    // Already looks like ISO date
    if (value.matches(Regex("""\d{4}-\d{2}-\d{2}.*"""))) {
        return value.take(10)
    }
    return value
}

/** Split API samples use dd/MM/yyyy inside data.date.value */
private fun formatSplitDisplayDate(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isBlank() || value == "-") return null
    // Keep already formatted display dates
    if (value.matches(Regex("""\d{1,2}/\d{1,2}/\d{4}"""))) return value

    val iso = normalizeExpenseDate(value) ?: return value
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(iso) ?: return value
        java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        value
    }
}

private fun formatDuplicateBillDate(raw: String?): String {
    val value = raw?.trim().orEmpty()
    if (value.isBlank() || value == "-") return ""
    val iso = normalizeExpenseDate(value) ?: return value
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = parser.parse(iso) ?: return value
        java.text.SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        value
    }
}

private fun splitAmountEqually(total: Double, parts: Int): List<Double> {
    if (parts <= 0) return emptyList()
    val totalCents = (total * 100).roundToLong()
    val baseCents = totalCents / parts
    val remainder = totalCents - (baseCents * parts)
    return (0 until parts).map { index ->
        val cents = if (index < remainder) baseCents + 1 else baseCents
        cents / 100.0
    }
}

private fun formatMoney(amount: Double, currency: String): String {
    return String.format(Locale.getDefault(), "%s %.2f", currency.ifBlank { "INR" }, amount)
}

@Composable
private fun ExpenseDocumentViewerDialog(
    fileUrl: String,
    fileName: String,
    onDismiss: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp,
            backgroundColor = Color.White,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = fileName.ifBlank { "Document" },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5F5F5)),
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            @SuppressLint("SetJavaScriptEnabled")
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                    }
                                }
                                loadUrl(fileUrl)
                            }
                        },
                        update = { webView ->
                            if (webView.url != fileUrl) {
                                isLoading = true
                                webView.loadUrl(fileUrl)
                            }
                        },
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = PrimaryRed,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OriginalFileBlock(
    fileName: String,
    status: String,
    uploadedAt: String,
    onOpenFile: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Original File",
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .clickable { onOpenFile() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = "Open original file",
                        tint = PrimaryRed,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenFile() },
                ) {
                    Text(
                        text = fileName.ifBlank { "Uploaded document" },
                        fontFamily = GraphikFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uploaded: $uploadedAt",
                        fontFamily = GraphikFontFamily,
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                    Text(
                        text = "Tap to open file",
                        fontFamily = GraphikFontFamily,
                        fontSize = 12.sp,
                        color = PrimaryRed,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text(
                    text = status,
                    color = expenseStatusTextColor(status),
                    fontFamily = GraphikFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(expenseStatusBackgroundColor(status), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                fontFamily = GraphikFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            )
            content()
        }
    }
}

@Composable
private fun DetailLabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxWidth()) {
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
            singleLine = singleLine,
            minLines = minLines,
            enabled = enabled,
            readOnly = !enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.Gray,
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color(0xFFFAFAFA),
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color(0xFFD4D4D4),
                disabledTextColor = Color.Black,
                disabledBorderColor = Color(0xFFD4D4D4),
            ),
        )
    }
}

@Composable
private fun DetailDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp),
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
                        tint = Color.Gray,
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color(0xFFFAFAFA),
                    disabledTextColor = if (value.isBlank()) Color.Gray else Color.Black,
                    disabledBorderColor = Color(0xFFD4D4D4),
                    disabledTrailingIconColor = Color.Gray,
                ),
            )
            if (enabled) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                )
            }
            DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f),
            ) {
                if (options.isEmpty()) {
                    DropdownMenuItem(onClick = { expanded = false }) {
                        Text(
                            text = "No options available",
                            fontFamily = GraphikFontFamily,
                            color = Color.Gray,
                        )
                    }
                } else {
                    options.forEach { option ->
                        DropdownMenuItem(
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        ) {
                            Text(text = option, fontFamily = GraphikFontFamily)
                        }
                    }
                }
            }
        }
    }
}

private fun expenseStatusTextColor(status: String): Color =
    when (status.lowercase()) {
        "approved" -> Color(0xFF2E7D32)
        "rejected" -> PrimaryRed
        "submitted", "pending" -> Color(0xFFEF6C00)
        else -> Color(0xFF1565C0)
    }

private fun expenseStatusBackgroundColor(status: String): Color =
    when (status.lowercase()) {
        "approved" -> Color(0xFFE8F5E9)
        "rejected" -> PrimaryRed.copy(alpha = 0.12f)
        "submitted", "pending" -> Color(0xFFFFF3E0)
        else -> Color(0xFFE3F2FD)
    }
