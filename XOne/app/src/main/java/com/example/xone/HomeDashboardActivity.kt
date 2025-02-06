package com.example.xone

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.xone.ui.screens.HomeDashboardScreen
import com.example.xone.ui.theme.XOneTheme


class HomeDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XOneTheme {
                HomeDashboardScreen(
                    onNavigateToMyDocuments = {
                        val intent = Intent(this, MyDocumentsActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
