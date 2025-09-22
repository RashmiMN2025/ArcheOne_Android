package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.AdminDashboardController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.ui.screens.AdminDashboardScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class AdminDashboardActivity : ComponentActivity() {
    private lateinit var controller: AdminDashboardController
    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        navigator = AndroidNavigator(this)
        controller = AdminDashboardController(this, navigator)

        setContent {
            XOneTheme {
                AdminDashboardScreen(
                    model = controller.model,
                    controller = controller,
                )
            }
        }
    }

    fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right,
        )
    }
}
