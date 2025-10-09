package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.MeetingHistoryController
import com.archeGlobal.one.ui.screens.MeetingHistoryScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class MeetingHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val source = intent.getStringExtra("source")
        val userRole = when (source) {
            "history" -> "user"
            "admin" -> "admin"
            "line_manager" -> "line_manager"
            "ceo" -> "ceo"
            else -> "user"
        }
        val controller = MeetingHistoryController(this, userRole)
        setContent {
            XOneTheme {
                MeetingHistoryScreen(
                    controller = controller,
                    onBackPressed = { finish() },
                    source = source
                )
            }
        }
    }
}