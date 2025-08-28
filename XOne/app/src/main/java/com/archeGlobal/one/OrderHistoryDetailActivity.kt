package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.archeGlobal.one.controller.OrderHistoryController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.screens.OrderHistoryDetailScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class OrderHistoryDetailActivity : ComponentActivity() {
    private lateinit var controller: OrderHistoryController
    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create AndroidNavigator
        val androidNavigator = AndroidNavigator(this)
        
        // Create a custom navigator that properly handles back navigation for this activity
        navigator = object : Navigator by androidNavigator {
            override fun popBackStack() {
                // In separate activity, finish instead of using navController
                finishWithAnimation()
            }
        }
        controller = OrderHistoryController(this, navigator, "OrderHistoryActivity")

        // Handle back button to return to OrderHistoryActivity
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithAnimation()
            }
        })

        // Get the selected order from the companion object (set by OrderHistoryActivity)
        val orderItem = OrderHistoryController.selectedOrderForDetails

        setContent {
            XOneTheme {
                orderItem?.let { order ->
                    OrderHistoryDetailScreen(
                        controller = controller,
                        orderItem = order
                    )
                } ?: run {
                    // Fallback if no order data found
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Order not found")
                    }
                }
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