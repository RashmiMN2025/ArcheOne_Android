package com.archeGlobal.one.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.EmergencyContactController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactScreen(
    controller: EmergencyContactController,
    modifier: Modifier = Modifier,
    onBackPressed: (() -> Unit)? = null,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(), // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        WelcomeBackgroundTop, // Light Beige/Grey
                                        WelcomeBackgroundMiddle, // Light Grey
                                        WelcomeBackgroundBottom, // Dark Grey
                                    ),
                            ),
                    ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Top AppBar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Emergency Contact",
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { (onBackPressed ?: { controller.onBackPressed() })() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.Black,
                            navigationIconContentColor = Color.Black,
                        ),
                )

                // Content
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Card containing emergency contact details
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Emergency Contact Header with Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 6.dp),
                            ) {
                                // Person icon in a red circle
                                Box(
                                    modifier = Modifier,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.profile), // Use painterResource to load the drawable
                                        contentDescription = "Emergency Contact",
                                        tint = Color(0xFFE53935), // Apply tint color
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Emergency Contact",
                                    fontSize = 18.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                )
                            }

                            // Contact Details
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier =
                                    modifier
                                        .padding(start = 24.dp),
                            ) {
                                // Name
                                LabeledValue(
                                    label = "Name",
                                    value = controller.model.name.ifEmpty { "-" },
                                )

                                // Relationship
                                LabeledValue(
                                    label = "Relationship",
                                    value = controller.model.relationship.ifEmpty { "N/A" },
                                )

                                // Phone Number (with underline)
                                Column {
                                    Text(
                                        text = "Phone Number",
                                        fontSize = 14.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray,
                                    )
                                    Text(
                                        text = controller.model.phoneNumber.ifEmpty { "-" },
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black,
                                        textDecoration =
                                            if (controller.model.phoneNumber.isNotEmpty()) {
                                                TextDecoration.Underline
                                            } else {
                                                TextDecoration.None
                                            },
                                    )
                                }
                            }

                            Divider(
                                color = Color(0xFFEEEEEE),
                                thickness = 1.5.dp,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )

                            // Important Note Section
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, // Align icon and text in one line
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.warning),
                                        contentDescription = "Important Note",
                                        tint = Color(0xFFE53935),
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Important Note",
                                        fontSize = 16.sp,
                                        fontFamily = GraphikFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    )
                                }
                                Text(
                                    text = "These contacts will be used in case of any emergency. Please ensure the information is up to date.",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    lineHeight = 17.sp,
                                    fontFamily = GraphikFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LabeledValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
        )
    }
}
