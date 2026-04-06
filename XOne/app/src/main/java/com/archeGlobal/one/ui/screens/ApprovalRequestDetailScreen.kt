package com.archeGlobal.one.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ApprovalRequestDetailScreen(
    employeeName: String = "Biswajit Dixit",
    leaveType: String = "Casual Leave",
    employeeCode: String = "NT1426",
    date: String = "2026-04-06",
    duration: String = "Full",
    reason: String = "Outdoor",
    status: String = "Pending",
    onBack: () -> Unit,
) {
    val statusColor = when (status.lowercase()) {
        "approved" -> Color(0xFF4CAF50)
        "rejected" -> Color(0xFFDD3825)
        "pending" -> Color(0xFFFF9800)
        else -> Color(0xFF888888)
    }
    val statusBgColor = when (status.lowercase()) {
        "approved" -> Color(0xFFE8F5E9)
        "rejected" -> Color(0xFFFFEBEE)
        "pending" -> Color(0xFFFFF3E0)
        else -> Color(0xFFF5F5F5)
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
                            Color(0xFFE0E0E0), // roughly matches the grey gradient top
                            Color(0xFFBDBDBD),
                            Color(0xFF9E9E9E),
                        ),
                    ),
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(40.dp),
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = "Request Details",
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(end = 40.dp) // To center it nicely based on the back button width
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCF9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Top Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = employeeName,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = leaveType,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(statusBgColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = status,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = statusColor
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Middle Info
                        InfoRow(label = "Employee Code", value = employeeCode)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(iconRes = R.drawable.ic_calendar, label = "Date", value = date)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow(imageVector = Icons.Default.Info, label = "Duration", value = duration)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Reason
                        Text(
                            text = "Reason",
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reason,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Cancel Request Button
                        Button(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                        ) {
                            Text(
                                text = "Cancel Request",
                                fontFamily = GraphikFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
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
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp)
            )
        } else if (imageVector != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = Color(0xFF888888),
                modifier = Modifier.size(20.dp)
            )
        } else {
            // Fallback for hashtag
            Text(
                text = "#",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF888888),
                modifier = Modifier.size(20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color(0xFF888888)
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color.Black
        )
    }
}
