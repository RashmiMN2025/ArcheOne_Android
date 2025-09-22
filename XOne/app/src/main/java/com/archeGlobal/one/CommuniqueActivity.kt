package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.CommuniqueController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.ResponsiveCommuniqueScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class CommuniqueActivity : ComponentActivity() {
    private lateinit var controller: CommuniqueController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        controller = CommuniqueController(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                ResponsiveCommuniqueScreen(
                    model = controller.model,
                    onCommuniqueClick = controller::onCommuniqueClick,
                    onBackPressed = controller::onBackPressed,
                    isLoading = controller.isLoading.value,
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.onCleared()
    }

    fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left, // enter animation for previous activity
            R.anim.slide_out_right, // exit animation for current activity
        )
    }
}
