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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archeGlobal.one.R
import com.archeGlobal.one.controller.AddressController
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.WelcomeBackgroundBottom
import com.archeGlobal.one.ui.theme.WelcomeBackgroundMiddle
import com.archeGlobal.one.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressDetailsScreen(
    controller: AddressController,
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
                                        WelcomeBackgroundTop, // Light Beige/Grey (0xFFE0DCD1)
                                        WelcomeBackgroundMiddle, // Light Grey (0xFFC8C8CA)
                                        WelcomeBackgroundBottom, // Dark Grey (0xFF474749)
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
                            "Address Details",
                            fontSize = 20.sp,
                            fontFamily = GraphikFontFamily,
                            fontWeight = FontWeight.Bold,
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
                    // Single Card containing both addresses
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            // Present Address Section
                            AddressSection(
                                icon = R.drawable.home2,
                                title = "Present Address",
                                addressText = controller.model.presentAddress.ifEmpty { "-" },
                            )

                            Divider(color = Color(0xFFEEEEEE), thickness = 1.5.dp)

                            // Permanent Address Section
                            AddressSection(
                                icon = R.drawable.ic_home1,
                                title = "Permanent Address",
                                addressText = controller.model.permanentAddress.ifEmpty { "-" },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddressSection(
    icon: Int,
    title: String,
    addressText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(20.dp),
    ) {
        // Section Title with Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
        }

        // Address Text
        Text(
            text = addressText,
            fontSize = 16.sp,
            fontFamily = GraphikFontFamily,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            lineHeight = 20.sp,
        )
    }
}
