package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.OrderReceivedController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.screens.OrderReceivedScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class OrderReceivedActivity : ComponentActivity() {
    private lateinit var controller: OrderReceivedController
    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        navigator = AndroidNavigator(this)
        controller = OrderReceivedController(this, navigator)

        setContent {
            XOneTheme {
                OrderReceivedScreen(
                    model = controller.model,
                    controller = controller,
                )
            }
        }
    }

    fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right,
        )
    }
}
