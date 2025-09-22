package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.CollateralController
import com.archeGlobal.one.controller.SmartCollateralController
import com.archeGlobal.one.ui.screens.SmartCollateralScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager

class SmartCollateralActivity : ComponentActivity() {
    private lateinit var controller: SmartCollateralController
    private lateinit var collateralController: CollateralController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collateralController = CollateralController(this)
        val userDataManager = UserDataManager.getInstance(this)
        val smartCollateralData = userDataManager.getSmartCollateralData() ?: emptyList()
        controller = SmartCollateralController(this) // <-- You missed this line!
        controller.setSmartCollateralData(smartCollateralData)
        setContent {
            XOneTheme {
                SmartCollateralScreen(
                    controller = controller,
                    onBackPressed = { finish() },
                )
            }
        }
    }
}
