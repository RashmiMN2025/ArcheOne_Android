package com.example.xone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.xone.R
import com.example.xone.controller.BusinessCardController
import com.example.xone.model.BusinessCardModel
import com.example.xone.ui.theme.BackgroundColor
import com.example.xone.ui.theme.CardBackground
import com.example.xone.ui.theme.PrimaryRed
import com.example.xone.ui.theme.getColorForApp
import androidx.compose.foundation.lazy.LazyColumn
import com.example.xone.ui.theme.TextPrimary
import com.example.xone.ui.theme.WelcomeBackgroundBottom
import com.example.xone.ui.theme.WelcomeBackgroundMiddle
import com.example.xone.ui.theme.WelcomeBackgroundTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCardScreen(
    businessCard: BusinessCardModel,
    controller: BusinessCardController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WelcomeBackgroundTop,    // Color(0xFFE0DCD1)
                        WelcomeBackgroundMiddle, // Color(0xFFC8C8CA)
                        WelcomeBackgroundBottom  // Color(0xFF474749)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Top Bar
                TopAppBar(
                    title = { 
                        Text(
                            "My Business Card",
                            color = TextPrimary
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { controller.onBackPressed() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back),
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }

            item {
                // Card Content
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.9f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground  // Use consistent card background
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(
                                start = 32.dp,  // Increased left padding to move content right
                                end = 24.dp,
                                top = 24.dp,
                                bottom = 24.dp
                            )
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Image(
                            painter = painterResource(id = businessCard.companyLogo),
                            contentDescription = "Company Logo",
                            modifier = Modifier.height(40.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Profile Image Placeholder
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f) // Made slightly transparent
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = businessCard.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.Black
                        )

                        Text(
                            text = businessCard.designation,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )

                        Text(
                            text = businessCard.department,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // QR Code with white background for better visibility
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            color = Color.White
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.qr),
                                contentDescription = "QR Code",
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = businessCard.email,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = businessCard.phone,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = businessCard.location,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            item {
                // Bottom Buttons
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { controller.onDownloadCard() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                    ) {
                        Text(
                            "Download Business Card", 
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }

                    Button(
                        onClick = { controller.onShareCard() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                    ) {
                        Text(
                            "Share Business Card", 
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
} 