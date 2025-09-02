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
    private lateinit var userDataReadyCallback: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        userDataManager = UserDataManager.getInstance(this)
        controller = DeskCartController(this, AndroidNavigator(this))
        
        // Register callback to refresh data when user data becomes ready (for fresh installs)
        userDataReadyCallback = {
            Log.d("DeskCartActivity", "User data ready callback triggered, starting controller load")
            controller.onLoginCompleted()
        }
        userDataManager.addUserDataReadyCallback(userDataReadyCallback)

        // Also try to start initial load immediately if data is already ready
        if (userDataManager.isUserDataReady()) {
            Log.d("DeskCartActivity", "User data already ready, starting initial load")
            controller.onLoginCompleted()
        }
        
        setContent {
            XOneTheme {
                DeskCartScreen(
                    model = controller.model,
                    controller = controller
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up callback to prevent memory leaks
        if (::userDataManager.isInitialized && ::userDataReadyCallback.isInitialized) {
            userDataManager.removeUserDataReadyCallback(userDataReadyCallback)
            Log.d("DeskCartActivity", "Cleaned up user data ready callback")
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
