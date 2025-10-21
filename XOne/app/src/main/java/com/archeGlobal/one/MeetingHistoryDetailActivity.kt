package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.ui.screens.MeetingHistoryDetailScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.model.BookingHistoryItem
import android.app.Activity.RESULT_OK
import com.google.gson.Gson

class MeetingHistoryDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = intent.getStringExtra("action")
        val source = intent.getStringExtra("source")
        val bookingJson = intent.getStringExtra("booking_json")
        val booking = bookingJson?.let { Gson().fromJson(it, BookingHistoryItem::class.java) }
        setContent {
            XOneTheme {
                MeetingHistoryDetailScreen(
                    onBackPressed = { finish() },
                    action = action,
                    booking = booking,
                    source = source
                )
            }
        }
    }
}