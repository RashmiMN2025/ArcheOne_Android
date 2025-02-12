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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.activity.ComponentActivity
import com.example.xone.ui.theme.XOneTheme
import com.example.xone.navigation.AndroidNavigator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCardScreen(
    businessCard: BusinessCardModel,
    controller: BusinessCardController
) {
    var isPortraitView by remember { mutableStateOf(true) }
    
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
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    when {
                        dragAmount < -50 && isPortraitView -> isPortraitView = false // Swipe left
                        dragAmount > 50 && !isPortraitView -> isPortraitView = true  // Swipe right
                    }
                }
            }
    ) {
        if (isPortraitView) {
            PortraitBusinessCard(businessCard, controller)
        } else {
            LandscapeBusinessCard(businessCard, controller)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitBusinessCard(
    businessCard: BusinessCardModel,
    controller: BusinessCardController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TopAppBar(
                title = { 
                    Text(
                        "My Business Card",
                        color = TextPrimary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
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
                    .fillMaxWidth(0.85f),
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
                        modifier = Modifier.height(20.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = businessCard.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize * 0.9f
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.designation,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.9f
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.department,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.9f
                        ),
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
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.9f
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = businessCard.phone,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.9f
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = businessCard.location,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.9f
                        ),
                        color = Color.Black
                    )
                }
            }
        }

        item {
            Text(
                text = "<-- Swipe left to change View",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * 0.9f
                ),
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandscapeBusinessCard(
    businessCard: BusinessCardModel,
    controller: BusinessCardController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { 
                Text(
                    "My Business Card",
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
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

        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.99f)
                .height(220.dp),  // Reduced height slightly more
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Image(
                        painter = painterResource(id = businessCard.companyLogo),
                        contentDescription = "Company Logo",
                        modifier = Modifier
                            .height(20.dp)
                            .offset(y = (-4).dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = businessCard.name,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize * 0.7f
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.designation,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.7f
                        ),
                        color = Color.Black
                    )

                    Text(
                        text = businessCard.department,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.7f
                        ),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = businessCard.email,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.7f
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = businessCard.phone,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.7f
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = businessCard.location,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.7f
                        ),
                        color = Color.Black
                    )
                }

                // Right side - QR code only
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        color = Color.White
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.qr),
                            contentDescription = "QR Code",
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "--> Swipe right to change View",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = MaterialTheme.typography.bodyMedium.fontSize * 0.9f
            ),
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Updated bottom buttons to match portrait layout
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

@Preview(showBackground = true)
@Composable
fun BusinessCardScreenPreview() {
    val previewCard = BusinessCardModel(
        companyLogo = R.drawable.arche,
        name = "John Doe",
        designation = "Software Engineer",
        department = "Engineering",
        email = "john.doe@company.com",
        phone = "+91 9876543210",
        location = "Bangalore",
        qrCode = ""
    )
    
    XOneTheme {
        BusinessCardScreen(
            businessCard = previewCard,
            controller = BusinessCardController(
                context = LocalContext.current,
                navigator = AndroidNavigator(ComponentActivity())
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PortraitBusinessCardPreview() {
    val previewCard = BusinessCardModel(
        companyLogo = R.drawable.arche,
        name = "John Doe",
        designation = "Software Engineer",
        department = "Engineering",
        email = "john.doe@company.com",
        phone = "+91 9876543210",
        location = "Bangalore",
        qrCode = ""
    )
    
    XOneTheme {
        PortraitBusinessCard(
            businessCard = previewCard,
            controller = BusinessCardController(
                context = LocalContext.current,
                navigator = AndroidNavigator(ComponentActivity())
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 800)
@Composable
fun LandscapeBusinessCardPreview() {
    val previewCard = BusinessCardModel(
        companyLogo = R.drawable.arche,
        name = "John Doe",
        designation = "Software Engineer",
        department = "Engineering",
        email = "john.doe@company.com",
        phone = "+91 9876543210",
        location = "Bangalore",
        qrCode = ""
    )
    
    XOneTheme {
        LandscapeBusinessCard(
            businessCard = previewCard,
            controller = BusinessCardController(
                context = LocalContext.current,
                navigator = AndroidNavigator(ComponentActivity())
            )
        )
    }
} 