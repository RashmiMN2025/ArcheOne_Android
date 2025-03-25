package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.archeGlobal.one.controller.SocialDataProvider
import com.archeGlobal.one.ui.screens.XConnectScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import kotlinx.coroutines.delay

class XConnectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start preloading data immediately when activity is created
        SocialDataProvider.getInstance(applicationContext).preloadData()
        
        setContent {
            XOneTheme {
                var isDataReady by remember { mutableStateOf(false) }
                var showLoading by remember { mutableStateOf(true) }
                
                // Check if data is already preloaded
                val dataProvider = SocialDataProvider.getInstance(applicationContext)
                
                LaunchedEffect(key1 = true) {
                    // If data is already loaded, show content immediately
                    if (dataProvider.isLoaded) {
                        isDataReady = true
                        showLoading = false
                    } else {
                        // Wait for a short time to see if data becomes available quickly
                        delay(500)
                        
                        // Check if data is ready now
                        if (dataProvider.isLoaded) {
                            isDataReady = true
                            showLoading = false
                        } else {
                            // If data is still loading, show the UI anyway after a brief loading indicator
                            delay(1000)
                            showLoading = false
                        }
                    }
                }
                
                if (showLoading) {
                    // Show loading indicator
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    // Show content, data will continue loading in background if not ready yet
                    XConnectScreen(
                        onBackPressed = { finish() }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Make sure data is loading when activity is resumed
        SocialDataProvider.getInstance(applicationContext).preloadData()
    }
}
