package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.ui.theme.XOneTheme
import androidx.compose.ui.draw.clip

class MyDocumentsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XOneTheme {
                MyDocumentsScreen()
            }
        }
    }
}

@Composable
fun MyDocumentsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE0DCD1), // Light Beige
                        Color(0xFFC8C8CA), // Light Gray
                        Color(0xFF474749)  // Dark Gray
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with Back Arrow and Title (adjusted position)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp) // Move the header slightly down
            ) {
                IconButton(onClick = { /* Handle back navigation */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "My Documents",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(start = 80.dp) // Adjust left alignment
                        .align(Alignment.CenterVertically) // Ensure the title is vertically aligned
                )
            }

            Spacer(modifier = Modifier.height(32.dp)) // Space below header

            // White container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Centered Text inside the white container
                    Text(
                        text = "Upload or view your personal and professional documents here",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally) // Center text horizontally
                            .padding(bottom = 16.dp)
                    )

                    // Personal Documents Section
                    Section("Personal Documents", listOf("Aadhar Card", "Passport", "PAN Card"))

                    Spacer(modifier = Modifier.height(24.dp))

                    // Professional Documents Section
                    Section("Professional Documents", listOf("Offer Letter", "Certificate", "Experience Letter"))
                }
            }
        }
    }
}

@Composable
fun Section(title: String, items: List<String>) {
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Layout for 2x2 Grid - Empty spot for the 4th position if not enough cards
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Place two items in the first row
            items.getOrNull(0)?.let {
                DocumentCard(name = it)
            }
            items.getOrNull(1)?.let {
                DocumentCard(name = it)
            }
        }
        Spacer(modifier = Modifier.height(16.dp)) // Space between rows

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Place the third item in the second row and leave the 4th spot empty
            items.getOrNull(2)?.let {
                DocumentCard(name = it)
            }
            // Empty space for the 4th card
            Box(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun DocumentCard(name: String) {
    Card(
        shape = RoundedCornerShape(12.dp), // More rounded corners for a professional look
        modifier = Modifier
            .width(170.dp) // Fixed width for all cards
            .height(110.dp) // Fixed height for all cards
            .padding(bottom = 10.dp), // Padding between rows
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDD3825))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterStart)
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp) // Space between text and icons
                )
            }
            // Move the Download and View icons to the bottom right corner with reduced space
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp) // Reduced padding between the icons and text
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_download), // Download Icon
                    contentDescription = "Download",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp) // Reduced icon size
                )
                Spacer(modifier = Modifier.width(8.dp)) // Reduced space between icons
                Icon(
                    painter = painterResource(id = R.drawable.ic_view), // View Icon
                    contentDescription = "View",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp) // Reduced icon size
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyDocumentsPreview() {
    XOneTheme {
        MyDocumentsScreen()
    }
}
