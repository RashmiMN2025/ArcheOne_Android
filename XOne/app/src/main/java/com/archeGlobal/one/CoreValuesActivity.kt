package com.archeGlobal.one.ui.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent

class CoreValuesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CoreValuesScreen(
                onBackPressed = { finish() }
            ) // Replace with your actual Composable function
        }
    }
}