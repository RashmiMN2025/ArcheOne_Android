package com.example.xone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import com.example.xone.R
import com.example.xone.controller.PolicyController
import com.example.xone.model.PolicyModel
import com.example.xone.navigation.AndroidNavigator
import com.example.xone.ui.preview.PreviewNavigator
import com.example.xone.ui.theme.XOneTheme
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(
    model: PolicyModel,
    onPolicyClick: (String) -> Unit,
    onDownloadClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
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
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            TopAppBar(
                title = { 
                    Text(
                        text = "Policies",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // White Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
                    .weight(1f, false),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = model.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.padding(bottom = 25.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(model.policies) { policy ->
                            PolicyCard(
                                policyName = policy.name,
                                onViewClick = { onPolicyClick(policy.name) },
                                onDownloadClick = { onDownloadClick(policy.name) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PolicyCard(
    policyName: String,
    onViewClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFDD3825)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = policyName,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.95f
                ),
                modifier = Modifier.weight(1f)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onViewClick,
                    modifier = Modifier.size(22.5.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.view),
                        contentDescription = "View",
                        modifier = Modifier.size(18.5.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
                
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.size(22.5.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.download),
                        contentDescription = "Download",
                        modifier = Modifier.size(18.5.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PolicyScreenPreview() {
    val previewNavigator = PreviewNavigator()
    val controller = PolicyController(LocalContext.current, previewNavigator)
    
    XOneTheme {
        PolicyScreen(
            model = controller.model,
            onPolicyClick = controller::onPolicyClick,
            onDownloadClick = controller::onDownloadClick,
            onBackClick = controller::onBackClick
        )
    }
} 