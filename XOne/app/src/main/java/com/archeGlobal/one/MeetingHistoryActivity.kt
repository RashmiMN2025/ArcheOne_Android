package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.ui.screens.MeetingHistoryScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class MeetingHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XOneTheme {
                MeetingHistoryScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}