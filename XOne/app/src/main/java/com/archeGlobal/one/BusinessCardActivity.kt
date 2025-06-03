package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.ui.screens.BusinessCardScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.controller.BusinessCardControllerImpl
import com.archeGlobal.one.navigation.AndroidNavigator

class BusinessCardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create the controller
        val controller = BusinessCardControllerImpl(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                BusinessCardScreen(
                    businessCard = controller.businessCard,
                    controller = controller
                )
            }
        }        
    }

    fun finishWithAnimation() {
    finish()
    overridePendingTransition(
        R.anim.slide_in_left, // enter animation for previous activity
        R.anim.slide_out_right // exit animation for current activity
    )
}
}