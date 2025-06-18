package com.archeGlobal.one.ui.screens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.archeGlobal.one.HomeActivity
import com.archeGlobal.one.LoginActivity
import com.archeGlobal.one.OnboardingActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.utils.PreferencesManager
import com.archeGlobal.one.utils.UserDataManager

class SplashActivity : ComponentActivity() {
    private lateinit var userDataManager: UserDataManager
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        userDataManager = UserDataManager.getInstance(applicationContext)
        preferencesManager = PreferencesManager(applicationContext)
        
        // Set Splash Screen UI
        setContent {
            XOneSplashScreen()
        }

        // Delay for 2 seconds and then check if it's first launch or user is logged in
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if this is the first launch of the app
            if (preferencesManager.isFirstLaunch()) {
                // First launch, show onboarding screens
                startActivity(Intent(this, OnboardingActivity::class.java))
            } else {
                // Not first launch, check if user is logged in
                val isLoggedIn = userDataManager.isLoggedIn()
                Log.d("SplashActivity", "Login status: $isLoggedIn, userData: ${userDataManager.getUserData()?.name}")
                
                if (isLoggedIn) {
                    // User is logged in, go to Home screen
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    // User is not logged in, go to Login screen
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
            finish()
        }, 2000)
    }
}

@Composable
fun XOneSplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.arche2),
                contentDescription = "Company Logo",
                modifier = Modifier.size(110.dp)
                    .offset(y = (-40).dp)
            )
        }
    }
} 