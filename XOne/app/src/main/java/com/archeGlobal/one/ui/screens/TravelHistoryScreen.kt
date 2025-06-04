package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.TravelController
import com.archeGlobal.one.controller.TravelHistoryState
import com.archeGlobal.one.model.TravelRequest
import com.archeGlobal.one.model.TravelStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TravelHistoryScreen(
    controller: TravelController
) {
    val state = controller.travelHistoryState
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,    // Light Beige/Grey (0xFFE0DCD1)
                        WelcomeBackgroundMiddle, // Light Grey (0xFFC8C8CA)
                        WelcomeBackgroundBottom  // Dark Grey (0xFF474749)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Add space at the top to push everything down
            Spacer(modifier = Modifier.height(48.dp))
            
            TopAppBar(
                title = { 
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Travel History",
                            color = Color.Black,
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { controller.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            )
            
            // Add more space after the TopAppBar
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (val currentState = state) {
                    is TravelHistoryState.Loading -> {
                        androidx.compose.material.CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is TravelHistoryState.Success -> {
                        TravelHistoryList(
                            travelRequests = currentState.travelRequests,
                            onTravelRequestClick = { travelId -> controller.navigateToTravelDetails(travelId) }
                        )
                    }
                    is TravelHistoryState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = currentState.message)
                            androidx.compose.material.Button(
                                onClick = { controller.loadTravelHistory() },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TravelHistoryList(
    travelRequests: List<TravelRequest>,
    onTravelRequestClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(travelRequests) { request ->
            TravelRequestCard(
                travelRequest = request,
                onClick = { onTravelRequestClick(request.id) }
            )
        }
    }
}

@Composable
fun TravelRequestCard(
    travelRequest: TravelRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 1.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // ID and Status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${travelRequest.id}",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                
                StatusTag(status = travelRequest.status)
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())
            
            // Project
            DetailItem(
                icon = R.drawable.ic_work,
                label = "Project",
                value = travelRequest.project
            )
            
            // Destination
            DetailItem(
                icon = R.drawable.ic_location,
                label = "Destination",
                value = travelRequest.destination
            )
            
            // Approver
            DetailItem(
                icon = Icons.Default.Person,
                label = "Approver",
                value = travelRequest.approver
            )
            
            // Add divider line before Created date
            Divider(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth())
            
            // Created date
            DetailItem(
                icon = R.drawable.ic_calendar,
                label = "Created",
                value = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                    .format(travelRequest.createdDate)
            )
        }
    }
}

@Composable
fun StatusTag(status: TravelStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TravelStatus.APPROVED -> Triple(Color(0xFFE6F4EA), Color(0xFF34A853), "Approved")
        TravelStatus.REJECTED -> Triple(Color(0xFFFCE8E6), Color(0xFFEA4335), "Rejected")
        TravelStatus.PENDING -> Triple(Color(0xFFFEF7E0), Color(0xFFFBBC05), "Pending")
    }
    
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DetailItem(
    icon: Int,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium
        )
    }
}
