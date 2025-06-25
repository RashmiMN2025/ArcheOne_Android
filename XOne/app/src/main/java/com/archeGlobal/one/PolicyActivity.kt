package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.PolicyController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.PolicyScreen
import com.archeGlobal.one.ui.screens.ResponsivePolicyScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class PolicyActivity : ComponentActivity() {
    private lateinit var controller: PolicyController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = PolicyController(this, AndroidNavigator(this))
        setContent {
            XOneTheme {
                ResponsivePolicyScreen(
                    model = controller.model.value,
                    onPolicyClick = controller::onPolicyClick,
                    onBackClick = controller::onBackClick,
                    isLoading = controller.isLoading.value
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
            R.anim.slide_out_right // exit animation for current activity
        )
    }
}