package com.example.xone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.R
import com.example.xone.model.WelcomeModel

private val BackgroundColor = Color(0xFFF8F3E7)
private val NavyBlue = Color(0xFF0A1172)
private val TextColor = Color.Black

@Composable
fun WelcomeScreen(
    model: WelcomeModel,
    onXOneClick: () -> Unit,
    onPulseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundColor),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.netcon),
                contentDescription = "Netcon Logo",
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyBlue
                )
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyBlue
                )
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