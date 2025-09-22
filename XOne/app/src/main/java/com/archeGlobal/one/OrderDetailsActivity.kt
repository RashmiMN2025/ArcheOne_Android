package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.OrderHistoryDetailsController
import com.archeGlobal.one.controller.OrderReceivedController
import com.archeGlobal.one.model.OrderHistoryItem
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.screens.OrderDetailsScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class OrderDetailsActivity : ComponentActivity() {
    private lateinit var controller: OrderHistoryDetailsController
    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        navigator = AndroidNavigator(this)

        val orderItem: OrderHistoryItem? = OrderReceivedController.selectedOrderForDetails
        if (orderItem == null) {
            finish()
            return
        }

        controller = OrderHistoryDetailsController(this, navigator)

        setContent {
            XOneTheme {
                OrderDetailsScreen(
                    controller = controller,
                    orderItem = orderItem,
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
