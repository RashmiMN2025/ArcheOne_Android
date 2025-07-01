package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.PasswordResetScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class PasswordResetActivity : ComponentActivity() {
    private lateinit var navigator: AndroidNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        navigator = AndroidNavigator(this)

        setContent {
            XOneTheme {
                PasswordResetContent(navigator)
            }
        }
    }
}

@Composable
fun PasswordResetContent(navigator: AndroidNavigator) {
    PasswordResetScreen(navigator)
}
