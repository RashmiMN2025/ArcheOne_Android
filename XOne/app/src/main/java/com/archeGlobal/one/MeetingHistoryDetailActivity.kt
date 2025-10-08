package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.ui.screens.MeetingHistoryDetailScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class MeetingHistoryDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XOneTheme {
                MeetingHistoryDetailScreen(
                    onBackPressed = { finish() }
                )
            }
        }
    }
}