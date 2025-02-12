package com.example.xone.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arche),
                    contentDescription = "Arche Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = model.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextColor
                    )

                    Text(
                        text = model.subtitle,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextColor
                    )

                    Text(
                        text = model.description1,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextColor
                    )

                    Text(
                        text = model.description2,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextColor
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 48.dp)
            ) {
                Button(
                    onClick = onXOneClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)) // Red Button
                ) {
                    Text(
                        text = model.buttons[0].text,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onPulseClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3825)) // Red Button
                ) {
                    Text(
                        text = model.buttons[1].text,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
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
