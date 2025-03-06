package com.example.xone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.R
import com.example.xone.ui.theme.XOneTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WelcomeScreen(
    onXOneClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Full-Screen Background Image
        Image(
            painter = painterResource(id = R.drawable.background_image), // Replace with actual image name
            contentDescription = "Background Image",
            modifier = Modifier
                .fillMaxSize() // Ensures it covers the full screen
                .align(Alignment.Center),
            contentScale = ContentScale.FillBounds // 🔥 Stretches to fit the entire screen exactly
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Middle Logo
            Image(
                painter = painterResource(id = R.drawable.arche_one), // Replace with your logo
                contentDescription = "One Logo",
                modifier = Modifier
                    .size(400.dp) // Adjusted size
                    .fillMaxWidth()
                    .offset(y = (-50).dp) // Moves image **further up**
            )

            // Tagline Below Logo
            Text(
                text = "Workplace, Simplified",
                fontSize = 14.sp,
                color = Color(0xFFDD3825), // Red Color
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-180).dp) // Moves **text up** for better positioning
            )

            Spacer(modifier = Modifier.weight(1f)) // Push button down

            // Get Started Button
            Button(
                onClick = onXOneClick,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 40.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)) // Red Button
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    XOneTheme {
        WelcomeScreen(
            onXOneClick = {},
        )
    }
}
