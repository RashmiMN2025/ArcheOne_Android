package com.example.xone.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.xone.controller.SOSController

class SOSActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var showRaiseConcern by remember { mutableStateOf(false) }
            val controller = SOSController(context = this)

            if (showRaiseConcern) {
                RaiseConcernScreen(onBackPressed = { showRaiseConcern = false })
            } else {
                SOSScreen(
                    controller,
                    onNavigateToRaiseConcern = { showRaiseConcern = true },
                    onBackPressed = { finish() })
            }
        }
    }
}
