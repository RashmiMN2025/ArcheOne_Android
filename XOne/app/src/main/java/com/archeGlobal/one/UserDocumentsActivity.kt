package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.UserDocumentsController
import com.archeGlobal.one.ui.screens.UserDocumentsScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class UserDocumentsActivity : ComponentActivity() {
    private val controller = UserDocumentsController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XOneTheme {
                UserDocumentsScreen(
                    controller = controller,
                    context = this,
                    onBackPressed = { finish() },
                )
            }
        }
    }
}
