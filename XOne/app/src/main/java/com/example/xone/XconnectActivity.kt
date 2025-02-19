package com.example.xone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.xone.ui.screens.XConnectScreen
import com.example.xone.ui.theme.XOneTheme

class XConnectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XOneTheme {
                XConnectScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
