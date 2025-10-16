package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.archeGlobal.one.controller.InventoryController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.InventoryScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class InventoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val navigator = AndroidNavigator(this)

        setContent {
            XOneTheme {
                val controller: InventoryController =
                    viewModel {
                        InventoryController(this@InventoryActivity, navigator)
                    }
                InventoryScreen(
                    model = controller.model,
                    controller = controller,
                )
            }
        }
    }

    override fun finish() {
        super.finish()
        // Add slide animation when going back
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun finishWithAnimation() {
        finish()
    }
}
