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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
fun MyDocumentsScreen(controller: MyDocumentsController, context: Context, employeeId: String, onBackPressed: () -> Unit) {
    val personalDocs by controller.personalDocs.observeAsState(emptyMap())
    val professionalDocs by controller.professionalDocs.observeAsState(emptyMap())

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
                IconButton(onClick = onBackPressed) {
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
                    modifier = Modifier.padding(start = 60.dp)
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Upload or view your personal and professional documents here",
                        fontSize = 22.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 20.dp),
                    )

                    Section("Personal Documents", listOf("Aadhar Card", "Passport", "PAN Card"), personalDocs, controller, context, employeeId, true)
                    Spacer(modifier = Modifier.height(24.dp))
                    Section("Professional Documents", listOf("Offer Letter", "Certificate", "Experience Letter"), professionalDocs, controller, context, employeeId, false)
                }
            }
        }
    }
}

@Composable
fun Section(title: String, items: List<String>, filePaths: Map<String, String>, controller: MyDocumentsController, context: Context, employeeId: String, isPersonal: Boolean) {
    Column {
        Text(text = title, fontSize = 18.sp, modifier = Modifier.padding(bottom = 10.dp))
        Column {
            items.forEach { item ->
                DocumentCard(item, filePaths[item], controller, context, employeeId, isPersonal)
            }
        }
    }
}

@Composable
fun DocumentCard(name: String, filePath: String?, controller: MyDocumentsController, context: Context, employeeId: String, isPersonal: Boolean) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            controller.onUploadClick(name, uri, employeeId)
        }
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(bottom = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDD3825))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Row {
                // Upload button
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .background(Color.White, shape = RoundedCornerShape(50))
                        .clickable { launcher.launch("application/pdf") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_upload),
                        contentDescription = "Upload",
                        tint = Color(0xFFDD3825),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // View button (always visible, but shows a toast if filePath is empty)
                Icon(
                    painter = painterResource(id = R.drawable.ic_view),
                    contentDescription = "View",
                    tint = Color.White, // Make the icon white
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            controller.onViewClick(context, name, isPersonal)
                        }
                )
            }
        }
    }
}