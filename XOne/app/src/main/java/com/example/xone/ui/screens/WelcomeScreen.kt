package com.example.xone.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.R
import com.example.xone.model.WelcomeModel
import com.example.xone.ui.theme.XOneTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.ComponentActivity
import com.example.xone.navigation.AndroidNavigator
import com.example.xone.ui.preview.PreviewNavigator
import androidx.compose.foundation.shape.RoundedCornerShape

private val TextColor = Color.Black

@Composable
fun WelcomeScreen(
    model: WelcomeModel,
    onXOneClick: () -> Unit,
    onPulseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
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
                .fillMaxSize(),  // Removed horizontal padding from Column
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),  // Fixed height for top section
                contentAlignment = Alignment.Center
            ) {
                // Top Arche Logo
                Image(
                    painter = painterResource(id = R.drawable.arche),
                    contentDescription = "Arche Logo",
                    modifier = Modifier
                        .size(140.dp)
                )
            }

            // Adjusted weight to move One logo up slightly
            Spacer(modifier = Modifier.weight(0.7f))

            // Center Arche One Logo
            Image(
                painter = painterResource(id = R.drawable.arche_one),
                contentDescription = "Arche One Logo",
                modifier = Modifier
                    .size(450.dp)
            )

            // Increased weight to push button down further
            Spacer(modifier = Modifier.weight(2.8f))

            // Get Started Button
            Button(
                onClick = onXOneClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDD3825)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    XOneTheme {
        WelcomeScreen(
            model = WelcomeModel(),
            onXOneClick = {},
            onPulseClick = {},
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WelcomeScreenDarkPreview() {
    val previewModel = WelcomeModel(
        title = "Welcome to",
        subtitle = "XOne",
        description1 = "Your one-stop solution",
        description2 = "for everything",
        buttons = listOf(
            WelcomeModel.Button("Login to XOne"),
            WelcomeModel.Button("Login to Pulse")
        )
    )
    
    XOneTheme {
        WelcomeScreen(
            model = previewModel,
            onXOneClick = {},
            onPulseClick = {},
            modifier = Modifier
        )
    }
}
