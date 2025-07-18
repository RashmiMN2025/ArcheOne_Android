package com.archeGlobal.one

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.HolidayCalendarController
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.repository.UserRepository
import com.archeGlobal.one.ui.screens.HolidayCalendarScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class HolidayCalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val apiService = RetrofitClient.apiService
        val userRepository = UserRepository(this)
        val controller = HolidayCalendarController(apiService, userRepository, this)
        setContent {
            XOneTheme {
                HolidayCalendarScreen(
                    controller = controller,
                    onBackPressed = { finish() },
                    onMonthClick = { month ->
                        val intent = Intent(this, MonthDetailActivity::class.java)
                        intent.putExtra("month", month)
                        startActivity(intent)
                    },
                    onHolidayListClick = { }
                )
            }
        }
    }
}
