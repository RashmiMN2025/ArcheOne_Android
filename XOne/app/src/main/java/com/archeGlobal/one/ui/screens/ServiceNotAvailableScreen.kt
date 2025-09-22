package com.archeGlobal.one.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily
import com.archeGlobal.one.ui.theme.XOneTheme

@Composable
fun ServiceNotAvailableScreen(
    navController: NavController,
    serviceName: String? = null,
) {
    // Create a gradient background from light gray to darker gray
    val gradientBackground =
        Brush.verticalGradient(
            colors =
                listOf(
                    Color(0xFFE6E6E2), // Light gray at top
                    Color(0xFF9E9E9E), // Darker gray at bottom
                ),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(), // <-- This ensures your content is not hidden by system bars
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(brush = gradientBackground),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Add less weight at the top to move content up
                Spacer(modifier = Modifier.weight(0.3f))

                // Red warning triangle icon
                Image(
                    painter = painterResource(id = R.drawable.warning),
                    contentDescription = "Service Unavailable",
                    modifier = Modifier.size(70.dp).align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFFE84C3D)), // Red tint
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Title - large bold text
                Text(
                    text = "Service Not Available",
                    fontSize = 24.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description text
                Text(
                    text = "This service is currently under development or\nnot available.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp)) // Website link text - single row with colored link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "Meanwhile, you can ",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        "explore our website",
                        fontSize = 14.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE84C3D),
                        modifier =
                            Modifier.clickable {
                                // Open the website in browser
                                val intent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://arche.global/"))
                                navController.context.startActivity(intent)
                            },
                    )
                }

                // More weight at the bottom to push content up and button down
                Spacer(modifier = Modifier.weight(1.4f))

                // Red rounded Go Back button
                Button(
                    onClick = { navController.popBackStack() },
                    modifier =
                        Modifier
                            .fillMaxWidth(0.7f)
                            .padding(bottom = 48.dp)
                            .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE84C3D), // Red button color
                        ),
                ) {
                    Text(
                        text = "Go Back",
                        fontSize = 18.sp,
                        fontFamily = GraphikFontFamily,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServiceNotAvailableScreenPreview() {
    // Mock NavController for preview
    val navController = rememberNavController()
    XOneTheme {
        // Using the app's theme for the preview
        ServiceNotAvailableScreen(
            navController = navController,
            serviceName = "My Career",
        )
    }
}
