package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.AssetController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.AssetScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class AssetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val controller = AssetController(this, AndroidNavigator(this))
        // Load asset data immediately when AssetActivity is created
        controller.onServiceAccessed()

        // Handle back gesture and back button
//        onBackPressedDispatcher.addCallback(
//            this,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    controller.onBackPressed()
//                }
//            }
//        )

        setContent {
            XOneTheme {
                AssetScreen(
                    model = controller.model,
                    controller = controller,
                    onBackPressed = { finish() },
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
