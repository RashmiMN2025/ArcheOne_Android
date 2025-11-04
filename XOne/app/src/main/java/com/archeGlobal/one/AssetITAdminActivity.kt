package com.archeGlobal.one

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import com.archeGlobal.one.ui.screens.AssetITAdminScreen
import com.archeGlobal.one.ui.screens.AssetInventoryScreen
import com.archeGlobal.one.ui.screens.assetAdminItem
import com.archeGlobal.one.ui.theme.XOneTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.archeGlobal.one.controllers.AssetITAdminController
import com.archeGlobal.one.ui.screens.AssetConsumptionScreen
import com.archeGlobal.one.ui.screens.AssetInventoryDetailScreen
import com.archeGlobal.one.ui.screens.AssetTicketsScreen
import com.archeGlobal.one.ui.screens.assetConsumptionItem
import com.archeGlobal.one.ui.screens.assetInventoryItem
import com.archeGlobal.one.ui.screens.assetTicketsItem

class AssetITAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContent {
            XOneTheme {
                val navController = rememberNavController()
                val controller: AssetITAdminController = viewModel()
                controller.initNavController(navController)

                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") {
                        AssetITAdminScreen(
                            items = assetAdminItem(),
                            onBackPressed = { finish() },
                            onItemClick = { itemName ->
                                // Normalize item name to route (e.g., "Asset Inventory" -> "asset_inventory")
                                val route = itemName.lowercase().replace(" ", "_")
                                navController.navigate(route)
                            }
                        )
                    }
                    composable("asset_inventory") {
                        AssetInventoryScreen(
                            onBackPressed = { navController.popBackStack() },
                            items = assetInventoryItem(),
                            onItemClick = { itemName ->
                                val route = itemName.lowercase().replace(" ", "_")
                                navController.navigate(route)
                            }
                        )
                    }
                    composable("tickets") {
                        AssetTicketsScreen(
                            onBackPressed = { navController.popBackStack() },
                            items = assetTicketsItem(),
                            onItemClick = { itemName ->
                                val route = itemName.lowercase().replace(" ", "_")
                                navController.navigate(route)
                            }
                        )
                    }
                    composable("asset_consumption") {
                        AssetConsumptionScreen(
                            onBackPressed = { navController.popBackStack() },
                            items = assetConsumptionItem(),
                            onItemClick = { itemName ->
                                val route = itemName.lowercase().replace(" ", "_")
                                navController.navigate(route)
                            }
                        )
                    }
                    composable(
                        "inventory_detail/{assetName}",
                        arguments = listOf(navArgument("assetName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("assetName")
                            ?.replace("_", " ")
                            ?.split(" ")
                            ?.joinToString(" ") { it.capitalize() } ?: "Asset"
                        AssetInventoryDetailScreen(
                            assetName = name,
                            onBackPressed = controller::navigateBack
                        )
                    }
                    composable("asset_inventory") {
                        AssetInventoryScreen(
                            items = assetInventoryItem(),
                            onItemClick = controller::onInventoryItemClick,
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                }
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