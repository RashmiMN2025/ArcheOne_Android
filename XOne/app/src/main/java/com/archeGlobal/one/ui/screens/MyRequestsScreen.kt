package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

data class UserApprovalRequest(
    val employeeName: String,
    val leaveType: String,
    val employeeCode: String,
    val date: String,
    val duration: String,
    val reason: String,
    val description: String,
    val status: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyRequestsScreen(
    onBack: () -> Unit,
    onRequestClick: (UserApprovalRequest) -> Unit = {},
) {
    // Mock data — replace with API data when available
    val requests = listOf(
        UserApprovalRequest(
            employeeName = "Biswajit Dixit",
            leaveType = "Casual Leave",
            employeeCode = "NT1426",
            date = "2026-04-06",
            duration = "Full",
            reason = "Outdoor",
            description = "personal",
            status = "Pending",
        ),
        UserApprovalRequest(
            employeeName = "Biswajit Dixit",
            leaveType = "Work From Home",
            employeeCode = "NT1426",
            date = "2026-03-28",
            duration = "Full",
            reason = "Personal work",
            description = "",
            status = "Approved",
        ),
        UserApprovalRequest(
            employeeName = "Biswajit Dixit",
            leaveType = "Regularisation",
            employeeCode = "NT1426",
            date = "2026-03-20",
            duration = "Half",
            reason = "Forgot to punch in",
            description = "",
            status = "Rejected",
        ),
    )

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
                            text = "My Requests",
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(requests) { request ->
                        MyRequestCard(
                            request = request,
                            onClick = { onRequestClick(request) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyRequestCard(
    request: UserApprovalRequest,
    onClick: () -> Unit,
) {
    val statusColor = when (request.status.lowercase()) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFDD3825)
        "pending" -> Color(0xFFE6A817)
        else -> Color(0xFF888888)
    }
    val statusBgColor = when (request.status.lowercase()) {
        "approved" -> Color(0xFFE8F5E9)
        "rejected" -> Color(0xFFFFEBEE)
        "pending" -> Color(0xFFFFF8E1)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Name + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.employeeName,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = request.leaveType,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF888888),
                    )
                }
                Box(
                    modifier = Modifier
                        .background(statusBgColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = request.status,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = statusColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date + Duration row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = null,
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = request.date,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF888888),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = request.duration,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reason
            Text(
                text = "Reason",
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color(0xFF888888),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = request.reason,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.Black,
            )
        }
    }
}
