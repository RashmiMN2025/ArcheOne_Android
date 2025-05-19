package com.archeGlobal.one.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.XOneTheme

@Composable
fun ServiceNotAvailableScreen(
    navController: NavController,
    serviceName: String? = null // Parameter kept for future use if needed
) {
    // We could use the serviceName parameter to customize the message in the future
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {            // Warning icon
            Image(
                painter = painterResource(id = R.drawable.ic_service_unavailable),
                contentDescription = "Service Unavailable",
                modifier = Modifier.size(96.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
              // Title
            Text(
                stringResource(id = R.string.service_not_available_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
              // Description
            Text(
                stringResource(id = R.string.service_not_available_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Website link text
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {                Text(
                    "Meanwhile, you can ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                Text(
                    "explore our website",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE84C3D),
                    modifier = Modifier.clickable { 
                        // Open the website in browser
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.archeglobal.com"))
                        navController.context.startActivity(intent)
                    }
                )
            }
        }
          // Button at the bottom
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            shape = MaterialTheme.shapes.medium
        ) {            Text(
                stringResource(id = R.string.go_back),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceNotAvailableScreenPreview() {
    // Mock NavController for preview
    val navController = rememberNavController()
    XOneTheme { // Using the app's theme for the preview
        ServiceNotAvailableScreen(
            navController = navController,
            serviceName = "My Career"
        )
    }
}
