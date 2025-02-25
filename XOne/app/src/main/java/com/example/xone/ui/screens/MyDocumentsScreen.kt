package com.example.xone.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import com.example.xone.controller.MyDocumentsController
import androidx.compose.ui.draw.clip
import com.example.xone.R

@Composable
fun MyDocumentsScreen(controller: MyDocumentsController, context: Context, employeeId: String, onBackPressed: () -> Unit) { // ✅ Pass context
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFE0DCD1), Color(0xFFC8C8CA), Color(0xFF474749))
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 25.dp)
            ) {
                IconButton(
                    onClick = onBackPressed
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "My Documents",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 60.dp).align(Alignment.CenterVertically)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Upload or view your personal and professional documents here",
                        fontSize = 22.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .padding(bottom = 16.dp),
                    )

                    Section("Personal Documents", listOf("Aadhar Card", "Passport", "PAN Card"), controller, context, employeeId) // ✅ Pass context
                    Spacer(modifier = Modifier.height(24.dp))
                    Section("Professional Documents", listOf("Offer Letter", "Certificate", "Experience Letter"), controller, context, employeeId) // ✅ Pass context
                }
            }
        }
    }
}


@Composable
fun Section(title: String, items: List<String>, controller: MyDocumentsController, context: Context, employeeId: String) { // ✅ Add context
    Column {
        Text(text = title, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        Column {
            for (row in items.chunked(1)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)) {
                    row.forEach { item ->
                        DocumentCard(name = item, controller = controller, context = context, employeeId = employeeId) // ✅ Pass context
                    }
                }
            }
        }
    }
}


@Composable
fun DocumentCard(name: String, controller: MyDocumentsController, context: Context, employeeId: String) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            controller.onUploadClick(name, uri, employeeId)
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth() // Make card take full width
            .height(60.dp)
            .padding(bottom = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDD3825))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Name (Left Side)
            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f) // Push icons to the end
            )

            // Icons (Right Side)
            Row {
                // Upload Icon inside a White Circle
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .background(Color.White, shape = RoundedCornerShape(50)) // Circular background
                        .clickable { launcher.launch("application/pdf") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_upload), // Change to new upload icon
                        contentDescription = "Upload",
                        tint = Color(0xFFDD3825), // Match the card's color
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // View Icon (Without Background)
                Icon(
                    painter = painterResource(id = R.drawable.ic_view),
                    contentDescription = "View",
                    tint = Color.White,
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { controller.onViewClick(context, name) }
                )
            }
        }
    }
}



