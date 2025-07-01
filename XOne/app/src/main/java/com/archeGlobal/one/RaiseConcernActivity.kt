package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.ui.screens.RaiseConcernScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class RaiseConcernActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            XOneTheme {
                RaiseConcernScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
