package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.HolidayCalendarController
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.repository.UserRepository
import com.archeGlobal.one.ui.screens.MonthDetailScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class MonthDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val month = intent.getIntExtra("month", 1)
        val apiService = RetrofitClient.apiService
        val userRepository = UserRepository(this)
        val controller = HolidayCalendarController(apiService, userRepository)
        setContent {
            XOneTheme {
                MonthDetailScreen(
                    month = month,
                    controller = controller,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
