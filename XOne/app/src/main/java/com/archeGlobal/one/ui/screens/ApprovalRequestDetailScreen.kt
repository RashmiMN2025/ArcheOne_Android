package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import com.archeGlobal.one.network.RetrofitClient
import kotlinx.coroutines.launch
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovalRequestDetailScreen(
    eventId: String = "",
    employeeName: String = "Biswajit Dixit",
    leaveType: String = "Casual Leave",
    employeeCode: String = "NT1426",
    date: String = "2026-04-06",
    duration: String = "Full",
    reason: String = "Outdoor",
    description: String = "",
    status: String = "Pending",
    punchIn: String? = null,
    punchOut: String? = null,
    category: String = "approval request",
    secondApprover: String? = null,
    secondApproverEmail: String? = null,
    onBack: () -> Unit,
    onReject: () -> Unit = {},
    onApprove: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    val isDeletionRequest = category.equals("deletion request", ignoreCase = true)
    val statusLower = status.lowercase()
    val canAct = statusLower == "pending" || statusLower == "first level approved"

    val statusColor = when (statusLower) {
        "approved" -> Color(0xFF008000)
        "rejected" -> Color(0xFFFF0000)
        "pending", "first level approved" -> Color(0xFFFFA500)
        else -> Color.Gray
    }
    val statusBgColor = when (statusLower) {
        "approved" -> Color(0xFF008000).copy(alpha = 0.15f)
        "rejected" -> Color(0xFFFF0000).copy(alpha = 0.15f)
        "pending", "first level approved" -> Color(0xFFFFA500).copy(alpha = 0.15f)
        else -> Color.Gray.copy(alpha = 0.15f)
    }

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
                            Color(0xFFE0E0E0),
                            Color(0xFFBDBDBD),
                            Color(0xFF9E9E9E),
                        ),
                    ),
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isDeletionRequest) "Deletion Request" else "Request Details",
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

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            Column {
                                Text(
                                    text = employeeName,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = leaveType,
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
                                    text = status.replaceFirstChar { it.uppercase() },
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = statusColor,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Info rows
                        InfoRow(iconRes = R.drawable.hashh, label = "Employee Code", value = employeeCode)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(iconRes = R.drawable.calender22, label = "Date", value = date)
                        if (duration.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(iconRes = R.drawable.meettime, label = "Duration", value = duration)
                        }
                        
                        if (!punchIn.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(iconRes = R.drawable.punchin, label = "Punch In", value = punchIn)
                        }
                        
                        if (!punchOut.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(iconRes = R.drawable.punchout, label = "Punch Out", value = punchOut)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(iconRes = R.drawable.justification, label = "Reason", value = reason)

                        if (!secondApprover.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow(
                                iconRes = R.drawable.hashh,
                                label = "Second Approver",
                                value = secondApprover + (secondApproverEmail?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""),
                            )
                        }

                        if (isDeletionRequest) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFFDD3825).copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp),
                                    )
                                    .padding(12.dp),
                            ) {
                                Text(
                                    text = "This is a request to DELETE a previously approved item. Approving will permanently remove it; rejecting keeps the original approval intact.",
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = Color(0xFFDD3825),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Description
                        Text(
                            text = "Description",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color(0xFF888888),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description.ifEmpty { "—" },
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black,
                        )

                        // Action buttons — show while the request still needs action
                        // (pending = awaiting first approver, first level approved = awaiting second approver)
                        if (canAct) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Button(
                                    onClick = {
                                        if (eventId.isEmpty()) {
                                            Toast.makeText(context, "Request ID is missing", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (isProcessing) return@Button
                                        isProcessing = true
                                        scope.launch {
                                            try {
                                                val response = if (isDeletionRequest) {
                                                    RetrofitClient.apiService.rejectDeletionRequest(eventId)
                                                } else {
                                                    RetrofitClient.apiService.rejectRequest(eventId)
                                                }
                                                if (response.isSuccessful && response.body()?.success == true) {
                                                    Toast.makeText(context, response.body()?.message ?: "Rejected successfully", Toast.LENGTH_LONG).show()
                                                    onReject()
                                                    onBack()
                                                } else {
                                                    Toast.makeText(context, response.body()?.message ?: "Failed to reject", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isProcessing = false
                                            }
                                        }
                                    },
                                    enabled = !isProcessing,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)),
                                ) {
                                    Text(
                                        text = "Reject",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color.White,
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (eventId.isEmpty()) {
                                            Toast.makeText(context, "Request ID is missing", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (isProcessing) return@Button
                                        isProcessing = true
                                        scope.launch {
                                            try {
                                                val response = if (isDeletionRequest) {
                                                    RetrofitClient.apiService.approveDeletionRequest(eventId)
                                                } else {
                                                    RetrofitClient.apiService.approveRequest(eventId)
                                                }
                                                if (response.isSuccessful && response.body()?.success == true) {
                                                    Toast.makeText(context, response.body()?.message ?: "Approved successfully", Toast.LENGTH_LONG).show()
                                                    onApprove()
                                                    onBack()
                                                } else {
                                                    Toast.makeText(context, response.body()?.message ?: "Failed to approve", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isProcessing = false
                                            }
                                        }
                                    },
                                    enabled = !isProcessing,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E9B4E)),
                                ) {
                                    Text(
                                        text = "Approve",
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        color = Color.White,
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

@Composable
private fun InfoRow(
    iconRes: Int? = null,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp),
            )
        } else if (imageVector != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp),
            )
        } else {
            Text(
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
