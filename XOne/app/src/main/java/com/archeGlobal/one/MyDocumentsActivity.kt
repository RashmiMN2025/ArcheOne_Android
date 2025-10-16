package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.MyDocumentsController
import com.archeGlobal.one.ui.screens.MyDocumentsScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class MyDocumentsActivity : ComponentActivity() {
    private val controller = MyDocumentsController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            XOneTheme {
                MyDocumentsScreen(
                    controller = controller,
                    context = this,
                    onBackPressed = { finish() },
                )
            }
        }
    }
}
