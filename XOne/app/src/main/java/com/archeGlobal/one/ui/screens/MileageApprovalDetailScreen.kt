package com.archeGlobal.one.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.archeGlobal.one.controller.TravelExpenseController
import com.archeGlobal.one.network.MileageExpenseDetailResponse
import com.archeGlobal.one.network.MileageExpenseNoteResponse
import com.archeGlobal.one.network.MileageExpenseVehicleDetail
import com.archeGlobal.one.network.isTravelExpenseDraft
import com.archeGlobal.one.network.isTravelExpenseSubmitted
import com.archeGlobal.one.network.travelExpenseStatusLabel
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.PrimaryRed
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import java.text.SimpleDateFormat
import java.util.Locale

private const val COMMENT_HINT =
    "Please add a comment before you proceed to reject or request info.."

/** Minimum comment length accepted by the approve/reject endpoints. */
private const val MIN_COMMENT_LENGTH = 3

/**
 * Approver view of a single mileage claim. Loads the claim, its notes and the
 * project options, and offers request info / reject / approve actions while the
 * claim is still pending.
 */
@Composable
fun MileageApprovalDetailScreen(
    expenseId: Int,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val controller = remember { TravelExpenseController(context) }
    val scrollState = rememberScrollState()
    val projectOptions by controller.projectOptions

    var detail by remember { mutableStateOf<MileageExpenseDetailResponse?>(null) }
    var notes by remember { mutableStateOf<List<MileageExpenseNoteResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var comment by rememberSaveable { mutableStateOf("") }
    var noteText by rememberSaveable { mutableStateOf("") }
    var isSendingNote by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showRequestInfoDialog by remember { mutableStateOf(false) }
    var showRequestInfoConfirm by remember { mutableStateOf(false) }
    var requestInfoComment by rememberSaveable { mutableStateOf("") }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showRejectConfirm by remember { mutableStateOf(false) }
    var rejectComment by rememberSaveable { mutableStateOf("") }
    var showApproveConfirm by remember { mutableStateOf(false) }

    fun loadNotes() {
        controller.fetchMileageExpenseNotes(
            expenseId = expenseId,
            onSuccess = { notes = it },
            onError = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() },
        )
    }

    LaunchedEffect(expenseId) {
        isLoading = true
        controller.fetchProjectOptions()
        controller.fetchMileageExpenseDetail(
            expenseId = expenseId,
            onSuccess = {
                detail = it
                isLoading = false
            },
            onError = { message ->
                isLoading = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
        loadNotes()
    }

    fun sendNote() {
        val text = noteText.trim()
        if (text.isBlank()) {
            Toast.makeText(context, "Please enter a note", Toast.LENGTH_SHORT).show()
            return
        }
        isSendingNote = true
        controller.addMileageExpenseNote(
            expenseId = expenseId,
            notes = text,
            onSuccess = { message ->
                isSendingNote = false
                noteText = ""
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                loadNotes()
            },
            onError = { message ->
                isSendingNote = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    fun requestInfo(note: String) {
        isSubmitting = true
        controller.addMileageExpenseNote(
            expenseId = expenseId,
            notes = note,
            onSuccess = { message ->
                isSubmitting = false
                requestInfoComment = ""
                comment = ""
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                loadNotes()
            },
            onError = { message ->
                isSubmitting = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    fun reject(reason: String) {
        isSubmitting = true
        controller.rejectMileageExpense(
            expenseId = expenseId,
            comment = reason,
            onSuccess = { message ->
                isSubmitting = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                onBack()
            },
            onError = { message ->
                isSubmitting = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    fun approve() {
        val text = comment.trim()
        isSubmitting = true
        controller.approveMileageExpense(
            expenseId = expenseId,
            comment = text.takeIf { it.isNotBlank() },
            onSuccess = { message ->
                isSubmitting = false
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                onBack()
            },
            onError = { message ->
                isSubmitting = false
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            },
        )
    }

    BackHandler { onBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,
                        WelcomeBackgroundMiddle,
                        WelcomeBackgroundBottom,
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
                            text = "Review Mileage Claim",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                actions = { Spacer(modifier = Modifier.size(48.dp)) },
            )

            if (isLoading || detail == null) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            } else {
                val record = detail!!
                val status = record.status?.trim().orEmpty()
                val isPending = isTravelExpenseSubmitted(status)
                val routePoints = record.route.filter { it.name.isNotBlank() }
                val startAddress = routePoints.firstOrNull()?.name ?: "-"
                val endAddress = routePoints.lastOrNull()?.name ?: "-"
                val travelDate = if (record.fromDate == record.toDate || record.toDate.isNullOrBlank()) {
                    formatApprovalDate(record.fromDate)
                } else {
                    "${formatApprovalDate(record.fromDate)} - ${formatApprovalDate(record.toDate)}"
                }
                val vehicle = record.companyVehicle ?: record.personalVehicle
                val operator = vehicle?.operator
                val employeeName = listOfNotNull(operator?.firstName, operator?.lastName)
                    .joinToString(" ")
                    .trim()
                val projectName = record.projectName?.takeIf { it.isNotBlank() }
                    ?: projectOptions.firstOrNull { it.id == record.projectId }?.code
                    ?: "-"

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MLG-${record.id}",
                                fontFamily = GraphikFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                            )
                            Text(
                                text = projectName,
                                fontFamily = GraphikFontFamily,
                                fontSize = 14.sp,
                                color = Color.Gray,
                            )
                        }
                        ApprovalStatusBadge(status = status)
                    }

                    MileageApprovalCardSection(title = "Claim Summary") {
                        MileageApprovalRow("Employee", employeeName.ifBlank { "-" })
                        MileageApprovalRow("Customer", record.customerName?.takeIf { it.isNotBlank() } ?: "-")
                        MileageApprovalRow("Project", projectName)
                        MileageApprovalRow("Date of Travel", travelDate)
                        MileageApprovalRow("Claim Amount", "Rs ${record.amount ?: "0.00"}", isBold = true)
                    }

                    MileageApprovalCardSection(title = "Trip Details") {
                        MileageApprovalRow("From Address", startAddress)
                        MileageApprovalRow("To Address", endAddress)
                        MileageApprovalRow("Travel Distance", "${record.distance ?: "0"} km")
                        MileageApprovalRow("Duration", formatDurationMinutes(record.durationSeconds))
                        MileageApprovalRow("Carbon Emission", "${record.carbonEmission ?: "0"} Kg CO₂e")
                    }

                    MileageApprovalCardSection(title = "Vehicle") {
                        MileageApprovalRow(
                            "Ownership",
                            record.vehicleType?.replaceFirstChar { it.uppercase() } ?: "-",
                        )
                        MileageApprovalRow(
                            "Vehicle",
                            record.vehicle?.replaceFirstChar { it.uppercase() } ?: "-",
                        )
                        MileageApprovalRow("Make / Model", vehicle?.makeModel ?: "-")
                        MileageApprovalRow("Asset Code", vehicle?.assetCode ?: "-")
                        MileageApprovalRow("Fuel Type", vehicle?.fuelType?.replaceFirstChar { it.uppercase() } ?: "-")
                        MileageApprovalRow("Engine CC", vehicleCcText(vehicle))
                    }

                    if (!record.mapImageUrl.isNullOrBlank()) {
                        MileageApprovalCardSection(title = "Route") {
                            AsyncImage(
                                model = record.mapImageUrl,
                                contentDescription = "Route map",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                            )
                        }
                    }

                    MileageApprovalCardSection(title = "Notes") {
                        if (notes.isEmpty()) {
                            Text(
                                text = "No notes yet",
                                fontFamily = GraphikFontFamily,
                                fontSize = 13.sp,
                                color = Color.Gray,
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                notes.forEach { note ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF6F4EE), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = note.createdBy?.takeIf { it.isNotBlank() } ?: "-",
                                                fontFamily = GraphikFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = Color.Black,
                                            )
                                            Text(
                                                text = formatApprovalTimestamp(note.createdAt),
                                                fontFamily = GraphikFontFamily,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                            )
                                        }
                                        Text(
                                            text = note.notes?.takeIf { it.isNotBlank() } ?: "-",
                                            fontFamily = GraphikFontFamily,
                                            fontSize = 13.sp,
                                            color = Color.Black,
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        text = "Enter a note",
                                        color = Color.Gray,
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 14.sp,
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = Color.White,
                                    focusedBorderColor = Color.LightGray,
                                    unfocusedBorderColor = Color(0xFFD4D4D4),
                                ),
                                enabled = !isSendingNote,
                            )
                            IconButton(onClick = { sendNote() }, enabled = !isSendingNote) {
                                if (isSendingNote) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = PrimaryRed,
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send note",
                                        tint = PrimaryRed,
                                    )
                                }
                            }
                        }
                    }

                    if (isPending) {
                        MileageApprovalCardSection(title = "Comment") {
                            OutlinedTextField(
                                value = comment,
                                onValueChange = { comment = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = COMMENT_HINT,
                                        color = Color.Gray,
                                        fontFamily = GraphikFontFamily,
                                        fontSize = 13.sp,
                                    )
                                },
                                minLines = 3,
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = Color.White,
                                    focusedBorderColor = Color.LightGray,
                                    unfocusedBorderColor = Color(0xFFD4D4D4),
                                ),
                                enabled = !isSubmitting,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                onClick = {
                                    requestInfoComment = comment
                                    showRequestInfoDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFFF6F4EE),
                                    contentColor = Color.Black,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isSubmitting,
                            ) {
                                Text(
                                    text = "Request Info",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            Button(
                                onClick = {
                                    rejectComment = comment
                                    showRejectDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFFF3E8E8),
                                    contentColor = PrimaryRed,
                                ),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isSubmitting,
                            ) {
                                Text(
                                    text = "Reject",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Button(
                            onClick = {
                                // The API rejects a comment shorter than 3 characters; catch it before the dialog.
                                val text = comment.trim()
                                if (text.isNotBlank() && text.length < MIN_COMMENT_LENGTH) {
                                    Toast.makeText(
                                        context,
                                        "Comment must be at least 3 characters",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                } else {
                                    showApproveConfirm = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = PrimaryRed,
                                contentColor = Color.White,
                            ),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isSubmitting,
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Text(
                                    text = "Approve Reimbursement",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        MileageApprovalCardSection(title = null) {
                            Text(
                                text = "This claim is ${travelExpenseStatusLabel(status).lowercase(Locale.getDefault())}. No further action is required.",
                                fontFamily = GraphikFontFamily,
                                fontSize = 13.sp,
                                color = Color.Gray,
                            )
                            if (!record.comment.isNullOrBlank()) {
                                MileageApprovalRow("Reviewer Comment", record.comment)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showRequestInfoDialog) {
        AlertDialog(
            onDismissRequest = { showRequestInfoDialog = false },
            title = {
                Text(
                    text = "Additional Details Required",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Please add a comment explaining what information you need from the employee. Your note is required to continue.",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                    OutlinedTextField(
                        value = requestInfoComment,
                        onValueChange = { requestInfoComment = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Enter your comment",
                                color = Color.Gray,
                                fontFamily = GraphikFontFamily,
                                fontSize = 13.sp,
                            )
                        },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color(0xFFD4D4D4),
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (requestInfoComment.trim().length < MIN_COMMENT_LENGTH) {
                        Toast.makeText(
                            context,
                            "Please add a comment to continue",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        showRequestInfoDialog = false
                        showRequestInfoConfirm = true
                    }
                }) {
                    Text("Request Info", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestInfoDialog = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
        )
    }

    if (showRequestInfoConfirm) {
        AlertDialog(
            onDismissRequest = { showRequestInfoConfirm = false },
            title = {
                Text(
                    text = "Are you sure?",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "you want to request info for this record?",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                    Text(
                        text = "Your comment",
                        fontFamily = GraphikFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                    )
                    Text(
                        text = requestInfoComment.trim(),
                        fontFamily = GraphikFontFamily,
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF6F4EE), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRequestInfoConfirm = false
                        requestInfo(requestInfoComment.trim())
                    },
                    enabled = !isSubmitting,
                ) {
                    Text("Request", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestInfoConfirm = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = {
                Text(
                    text = "Provide Reason for Rejection",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "A comment is required to explain why this mileage submission is being rejected. Add your note to proceed.",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                    OutlinedTextField(
                        value = rejectComment,
                        onValueChange = { rejectComment = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Enter reason for rejection",
                                color = Color.Gray,
                                fontFamily = GraphikFontFamily,
                                fontSize = 13.sp,
                            )
                        },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            backgroundColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color(0xFFD4D4D4),
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (rejectComment.trim().length < MIN_COMMENT_LENGTH) {
                        Toast.makeText(
                            context,
                            "Please add a comment to continue",
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        showRejectDialog = false
                        showRejectConfirm = true
                    }
                }) {
                    Text("Reject", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
        )
    }

    if (showRejectConfirm) {
        AlertDialog(
            onDismissRequest = { showRejectConfirm = false },
            title = {
                Text(
                    text = "Are you sure?",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "you want to reject this record?",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                    Text(
                        text = "Your comment",
                        fontFamily = GraphikFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                    )
                    Text(
                        text = rejectComment.trim(),
                        fontFamily = GraphikFontFamily,
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF6F4EE), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRejectConfirm = false
                        reject(rejectComment.trim())
                    },
                    enabled = !isSubmitting,
                ) {
                    Text("Reject", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectConfirm = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
        )
    }

    if (showApproveConfirm) {
        AlertDialog(
            onDismissRequest = { showApproveConfirm = false },
            title = {
                Text(
                    text = "Approve Mileage Submission?",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = "Once approved, this submission will be marked as completed and the employee will be notified.",
                    fontFamily = GraphikFontFamily,
                    fontSize = 14.sp,
                    color = Color.Gray,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showApproveConfirm = false
                        approve()
                    },
                    enabled = !isSubmitting,
                ) {
                    Text("Approve", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirm = false }) {
                    Text("Cancel", color = PrimaryRed, fontFamily = GraphikFontFamily)
                }
            },
        )
    }
}

@Composable
private fun MileageApprovalCardSection(
    title: String?,
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    fontFamily = GraphikFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                )
                Divider(color = Color(0xFFEAEAEA))
            }
            content()
        }
    }
}

@Composable
private fun MileageApprovalRow(
    label: String,
    value: String,
    isBold: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(120.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ApprovalStatusBadge(status: String) {
    val label = travelExpenseStatusLabel(status.takeIf { it.isNotBlank() } ?: "pending")
    val normalized = label.lowercase(Locale.getDefault())
    val (background, textColor) = when {
        isTravelExpenseDraft(normalized) -> Pair(Color(0xFFFFF9C4), Color(0xFF827717))
        isTravelExpenseSubmitted(normalized) -> Pair(Color(0xFFF5F5F5), Color(0xFF616161))
        normalized.contains("reject") -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
    }

    Text(
        text = label,
        color = textColor,
        fontFamily = GraphikFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

private fun vehicleCcText(vehicle: MileageExpenseVehicleDetail?): String {
    val cc = vehicle?.vehicleCc ?: return "-"
    return "$cc cc"
}

private fun formatDurationMinutes(durationSeconds: String?): String {
    val seconds = durationSeconds?.toDoubleOrNull() ?: return "-"
    val totalMinutes = (seconds / 60).toInt()
    return when {
        totalMinutes < 60 -> "$totalMinutes min"
        totalMinutes % 60 == 0 -> "${totalMinutes / 60} hr"
        else -> "${totalMinutes / 60} hr ${totalMinutes % 60} min"
    }
}

private fun formatApprovalDate(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)
        parsed?.let { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it) } ?: value
    } catch (_: Exception) {
        value
    }
}

private fun formatApprovalTimestamp(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    return try {
        val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(value.take(19))
        parsed?.let { SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(it) } ?: value
    } catch (_: Exception) {
        value
    }
}
