package com.archeGlobal.one.ui.screens

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
import com.archeGlobal.one.R
import com.archeGlobal.one.ui.theme.GraphikFontFamily

@Composable
fun WelcomeScreen(
    onXOneClick: () -> Unit
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
            Spacer(modifier = Modifier.height(4.dp))
            // Middle Logo
            Image(
                painter = painterResource(id = R.drawable.arche2), // Replace with your logo
                contentDescription = "One Logo",
                modifier = Modifier
                    .size(110.dp) // Adjusted size
                    .fillMaxWidth()
                    .offset(y = (-1).dp) // Moves image **further up**
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Tagline Below Logo
            Text(
                text = "workplace, simplified",
                fontSize = 18.sp,
                color = Color(0xFFDD3825),
                fontFamily = GraphikFontFamily,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.offset(y = (-2).dp) // Moves **text up** for better positioning
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
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = GraphikFontFamily,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
