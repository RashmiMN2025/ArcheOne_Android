package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.controller.CollateralController
import com.archeGlobal.one.network.SmartCollateralFile
import com.archeGlobal.one.ui.screens.CollateralDetailScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager

class CollateralDetailActivity : ComponentActivity() {
    private lateinit var controller: CollateralController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        controller = CollateralController(this)

        val categoryName = intent.getStringExtra("categoryName") ?: ""

        val userDataManager = UserDataManager.getInstance(this)
        val smartCollateralList = userDataManager.getSmartCollateralData() ?: emptyList()
        val category = smartCollateralList.firstOrNull { it.name == categoryName }
        val filesList = category?.files ?: emptyList()

        setContent {
            XOneTheme {
                CollateralDetailScreen(
                    categoryName = categoryName,
                    files = filesList,
                    controller = controller,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
