package com.example.xone.ui.screens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.xone.MainActivity
import com.example.xone.R

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Splash Screen UI
        setContent {
            SplashScreen()
        }

        // Delay for 2 seconds and then open MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.arche_one),
                contentDescription = "Company Logo",
                modifier = Modifier.size(200.dp)
            )
            // Tagline Below Logo
            Text(
                text = "Workplace, Simplified",
                fontSize = 14.sp,
                color = Color(0xFFDD3825), // Red Color
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-60).dp) // Moves **text up** for better positioning
            )
        }
    }
}
