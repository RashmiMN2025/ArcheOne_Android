package com.archeGlobal.one

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.archeGlobal.one.controller.DeskCartController
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.DeskCartScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager

class DeskCartActivity : ComponentActivity() {
    private lateinit var controller: DeskCartController
    private lateinit var userDataManager: UserDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        userDataManager = UserDataManager.getInstance(this)
        controller = DeskCartController(this, AndroidNavigator(this))

        // Load DeskCart data immediately since user is accessing the service
        Log.d("DeskCartActivity", "DeskCart service accessed, loading data on demand")
        controller.onServiceAccessed()

        setContent {
            XOneTheme {
                DeskCartScreen(
                    model = controller.model,
                    controller = controller,
                )
            }
        }
    }

    fun finishWithAnimation() {
        finish()
        overridePendingTransition(
            R.anim.slide_in_left, // enter animation for previous activity
            R.anim.slide_out_right, // exit animation for current activity
        )
    }
}
