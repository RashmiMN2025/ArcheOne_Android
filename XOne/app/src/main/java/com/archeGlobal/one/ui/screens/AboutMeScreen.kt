package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.archeGlobal.one.controller.AboutMeController
import com.archeGlobal.one.model.FooterNavigationModel
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutMeScreen(
    controller: AboutMeController,
    modifier: Modifier = Modifier,
    footerNavigation: FooterNavigationModel = FooterNavigationModel(showProfile = true),
    onFooterHomeClick: () -> Unit = { controller.onBackPressed() },
    onFooterChatClick: () -> Unit = {},
    onFooterSOSClick: () -> Unit = {},
    onFooterProfileClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = modifier
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top AppBar
            CenterAlignedTopAppBar(
                title = { Text("About Me", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { controller.onBackPressed() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Single Card containing all sections
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Personal Details Section
                        SectionContent(
                            icon = R.drawable.id,
                            title = "Personal Details",
                            items = listOf(
                                LabeledInfo("PAN Number", controller.model.panNumber),
                                LabeledInfo("UAN Number", controller.model.uanNumber),
                                LabeledInfo("Blood Group", controller.model.bloodGroup)
                            )
                        )
                        
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        
                        // Reporting Structure Section
                        SectionContent(
                            icon = R.drawable.timesheet,
                            title = "Reporting Structure",
                            items = listOf(
                                LabeledInfo("Reporting Manager", controller.model.reportingManager, true),
                                LabeledInfo("Divisional Head", controller.model.divisionalHead, true)
                            )
                        )
                        
                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        
                        // Work Information Section
                        SectionContent(
                            icon = R.drawable.locations,
                            title = "Work Information",
                            items = listOf(
                                LabeledInfo("Department", controller.model.department),
                                LabeledInfo("Designation", controller.model.designation),
                                LabeledInfo("Location", controller.model.location)
                            )
                        )
                    }
                }
            }
        }
    }
}

data class LabeledInfo(
    val label: String,
    val value: String,
    val showPersonIcon: Boolean = false
)

@Composable
fun SectionContent(
    icon: Int,
    title: String,
    items: List<LabeledInfo>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section Title with Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        
        // Section Content
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items.forEach { item ->
                Column {
                    Text(
                        text = item.label,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (item.showPersonIcon) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = item.value.ifEmpty { "-" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
} 