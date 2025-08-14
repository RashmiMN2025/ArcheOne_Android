package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.SmartCollateralController
import com.archeGlobal.one.ui.screens.SmartCollateralScreen
import com.archeGlobal.one.ui.theme.XOneTheme

class SmartCollateralActivity : ComponentActivity() {

    private lateinit var controller: SmartCollateralController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize controller
        controller = SmartCollateralController(this)

        setContent {
            XOneTheme {
                SmartCollateralScreen(
                    controller = controller,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
