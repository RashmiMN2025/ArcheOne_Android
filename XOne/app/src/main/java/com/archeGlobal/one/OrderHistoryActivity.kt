package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.OrderHistoryController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.screens.OrderHistoryScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class OrderHistoryActivity : ComponentActivity() {
    private lateinit var controller: OrderHistoryController
    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        navigator = AndroidNavigator(this)
        controller = OrderHistoryController(this, navigator, "DeskCartActivity")

        // Force refresh to show fresh data when opened from history button
        controller.refreshOrderHistory()

        setContent {
            XOneTheme {
                OrderHistoryScreen(
                    model = controller.model,
                    controller = controller
                )
            }
        }
    }

    fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
}