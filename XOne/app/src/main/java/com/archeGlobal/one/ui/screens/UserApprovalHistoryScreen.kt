package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.model.MyRequestItem
import com.archeGlobal.one.ui.components.UniversalLoader
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserApprovalHistoryScreen(
    item: MyRequestItem,
    onBack: () -> Unit,
    onCancelRequest: ((onDone: () -> Unit) -> Unit)? = null,
    onRequestDeletion: ((reason: String, onDone: () -> Unit) -> Unit)? = null,
) {
    var isCancelling by remember { mutableStateOf(false) }
    var showDeletionDialog by remember { mutableStateOf(false) }
    var deletionReason by remember { mutableStateOf("") }
    val status = item.status?.lowercase() ?: "pending"
    val statusColor = when (status) {
        "approved" -> Color(0xFF008000)
        "rejected" -> Color(0xFFFF0000)
        "pending", "first level approved" -> Color(0xFFFFA500)
        else -> Color.Gray
    }
    val statusBgColor = when (status) {
        "approved" -> Color(0xFF008000).copy(alpha = 0.15f)
        "rejected" -> Color(0xFFFF0000).copy(alpha = 0.15f)
        "pending", "first level approved" -> Color(0xFFFFA500).copy(alpha = 0.15f)
        else -> Color.Gray.copy(alpha = 0.15f)
    }
    val displayStatus = status.replaceFirstChar { it.uppercase() }

    val dateDisplay = if (item.startDate != null && item.startDate == item.endDate) item.startDate!!
    else if (item.startDate != null && item.endDate != null) "${item.startDate} – ${item.endDate}"
    else item.startDate ?: item.endDate ?: "N/A"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WelcomeBackgroundTop,
                            WelcomeBackgroundMiddle,
                            WelcomeBackgroundBottom,
                        ),
                    ),
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Request Details",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.Black,
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black,
                    ),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F4EE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Name + status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.employeeName ?: "Unknown",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 20.sp,
                                        color = Color.Black,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.requestType ?: "N/A",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = Color(0xFF888888),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(statusBgColor, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = displayStatus,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = statusColor,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            DetailInfoRow(iconRes = R.drawable.hashh, label = "Employee Code", value = item.employeeCode ?: "N/A")
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailInfoRow(iconRes = R.drawable.calender22, label = "Date", value = dateDisplay)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (!item.leaveDuration.isNullOrEmpty()) {
                                DetailInfoRow(iconRes = R.drawable.meettime, label = "Duration", value = item.leaveDuration)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            if (!item.punchIn.isNullOrEmpty()) {
                                DetailInfoRow(iconRes = R.drawable.punchin, label = "Punch In", value = item.punchIn ?: "N/A")
                            }
                            if (!item.punchOut.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                DetailInfoRow(iconRes = R.drawable.punchout, label = "Punch Out", value = item.punchOut ?: "N/A")
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailInfoRow(iconRes = R.drawable.justification, label = "Reason", value = item.reason ?: "N/A")

                            if (!item.description.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Description",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = Color(0xFF888888),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.description ?: "N/A",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                )
                            }

                            // Cancel button — for pending: direct delete; for approved: raise deletion request
                            val isPending = status == "pending"
                            val isApproved = status == "approved"
                            val canCancelPending = isPending && onCancelRequest != null
                            val canRequestDeletion = isApproved && onRequestDeletion != null
                            if (canCancelPending || canRequestDeletion) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        if (isCancelling) return@Button
                                        if (canCancelPending) {
                                            isCancelling = true
                                            onCancelRequest!! { isCancelling = false }
                                        } else if (canRequestDeletion) {
                                            deletionReason = ""
                                            showDeletionDialog = true
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                                ) {
                                    Text(
                                        text = if (canRequestDeletion) "Cancel Request" else "Cancel Request",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color.White,
                                    )
                                }
                                if (canRequestDeletion) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Your manager will need to approve the cancellation.",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = Color(0xFF888888),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    UniversalLoader(isLoading = isCancelling)

    if (showDeletionDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCancelling) showDeletionDialog = false },
            confirmButton = {
                TextButton(
                    enabled = !isCancelling && deletionReason.trim().isNotEmpty(),
                    onClick = {
                        val reason = deletionReason.trim()
                        if (reason.isEmpty() || onRequestDeletion == null) return@TextButton
                        isCancelling = true
                        onRequestDeletion(reason) {
                            isCancelling = false
                            showDeletionDialog = false
                        }
                    },
                ) {
                    Text(
                        text = "Submit",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDD3825),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isCancelling,
                    onClick = { showDeletionDialog = false },
                ) {
                    Text(
                        text = "Close",
                        fontFamily = GraphikFontFamily,
                        color = Color.Gray,
                    )
                }
            },
            title = {
                Text(
                    text = "Cancel This Request",
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    color = Color(0xFFDD3825),
                )
            },
            titleContentColor = Color(0xFFDD3825),
            textContentColor = Color(0xFF333333),
            text = {
                Column {
                    Text(
                        text = "Tell your manager why you want this approved request cancelled. They will need to approve the cancellation.",
                        fontFamily = GraphikFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deletionReason,
                        onValueChange = { deletionReason = it },
                        placeholder = {
                            Text(
                                text = "Reason for cancellation",
                                fontFamily = GraphikFontFamily,
                                color = Color(0xFFAAAAAA),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = GraphikFontFamily,
                            fontSize = 14.sp,
                            color = Color.Black,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 6,
                        enabled = !isCancelling,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            disabledContainerColor = Color.White,
                            cursorColor = Color(0xFFDD3825),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                        ),
                    )
                }
            },
            containerColor = Color(0xFFF6F4EE),
        )
    }
}

@Composable
private fun DetailInfoRow(
    iconRes: Int? = null,
    imageVector: ImageVector? = null,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            iconRes != null -> Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp),
            )
            imageVector != null -> Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp),
            )
            else -> Text(
                text = "#",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF888888),
                modifier = Modifier.size(20.dp),
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color(0xFF888888),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color.Black,
        )
    }
}
