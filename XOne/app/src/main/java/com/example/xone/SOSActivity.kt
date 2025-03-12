package com.example.xone.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.xone.SOSDetailActivity
import com.example.xone.controller.SOSController

class SOSActivity : ComponentActivity() {
    private val controller: SOSController by viewModels() // ✅ Use ViewModel correctly

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SOSScreen(
                controller = controller,
                onNavigateToRaiseConcern = { /* Navigate to Raise Concern */ },
                onBackPressed = { finish() },
                onSOSBlogClick = { blog ->
                    val intent = Intent(this, SOSDetailActivity::class.java)
                    intent.putExtra("blog", blog) // Now `blog` is Parcelable
                    startActivity(intent)
                }
            )
        }
    }
}
