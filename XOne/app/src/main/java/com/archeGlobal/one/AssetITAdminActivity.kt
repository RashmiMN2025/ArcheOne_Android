package com.archeGlobal.one

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import com.archeGlobal.one.controller.AssetInventoryController
import com.archeGlobal.one.controller.AssetInventoryDetailController
import com.archeGlobal.one.controller.AssetTicketsController
import com.archeGlobal.one.controller.DownloadReportsController
import com.archeGlobal.one.controllers.AssetITAdminController
import com.archeGlobal.one.ui.screens.AssetConsumptionScreen
import com.archeGlobal.one.ui.screens.AssetInventoryDetailScreen
import com.archeGlobal.one.ui.screens.AssetTicketsScreen
import com.archeGlobal.one.ui.screens.AssetsInLocationScreen
import com.archeGlobal.one.ui.screens.DownloadReportsScreen
import com.archeGlobal.one.ui.screens.SelfTagRequestScreen
import com.archeGlobal.one.ui.screens.TaggedAssetsByEmployeeScreen
import com.archeGlobal.one.ui.screens.TaggedAssetsScreen
import com.archeGlobal.one.ui.screens.TrackAssetTicketsScreen
import com.archeGlobal.one.ui.screens.assetConsumptionItem
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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

                NavHost(
                    navController = navController,
                    startDestination = "dashboard",
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(500)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(500)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(500)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(500)
                        )
                    }
                ) {
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
                        val inventoryController: AssetInventoryController = viewModel()
                        AssetInventoryScreen(
                            controller = inventoryController,
                            onItemClick = { assetName ->
                                val safeName = assetName.lowercase().replace(" ", "_")
                                navController.navigate("inventory_detail/$safeName")
                            },
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                    composable("tickets") {
                        AssetTicketsScreen(
                            onBackPressed = { navController.popBackStack() },
                            onItemClick = { itemName ->
                                val route = itemName.lowercase().replace(" ", "_")
                                navController.navigate(route)
                            }
                        )
                    }
                    composable("open") {
                        val context = LocalContext.current
                        val controller = remember { AssetTicketsController(context, "Open") }
                        TrackAssetTicketsScreen(
                            controller = controller,
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                    composable("closed") {
                        val context = LocalContext.current
                        val controller = remember { AssetTicketsController(context, "Closed") }
                        TrackAssetTicketsScreen(
                            controller = controller,
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                    composable("inprogress") {
                        val context = LocalContext.current
                        val controller = remember { AssetTicketsController(context, "InProgress") }
                        TrackAssetTicketsScreen(
                            controller = controller,
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                    composable("self-tag_requests") {
                        SelfTagRequestScreen(
                            onBackPressed = { navController.popBackStack() },
                        )
                    }
                    composable("asset_consumption") {
                        AssetConsumptionScreen(
                            items = assetConsumptionItem(),
                            onBackPressed = { navController.popBackStack() },
                            onItemClick = { _, _ -> },
                            navController = navController
                        )
                    }
                    composable("download_reports") {
                        val controller: DownloadReportsController = viewModel()
                        DownloadReportsScreen(
                            controller = controller,
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                    composable(
                        "inventory_detail/{assetName}",
                        arguments = listOf(navArgument("assetName") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val assetName = backStackEntry.arguments?.getString("assetName")
                            ?.replace("_", " ")
                            ?.split(" ")
                            ?.joinToString(" ") { it.capitalize() } ?: "Asset"

                        // Custom factory for ViewModel with parameter
                        object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return AssetInventoryDetailController(assetName.lowercase()) as T
                            }
                        }.let { factory ->
                            val controller: AssetInventoryDetailController = viewModel(factory = factory)
                            AssetInventoryDetailScreen(
                                assetName = assetName,
                                controller = controller,
                                onBackPressed = { navController.popBackStack() },
                                onAddClick = {},
                                onItemClick = {}
                            )
                        }
                    }
                    composable("tagged_assets") {
                        TaggedAssetsScreen(
                            onBackPressed = { navController.popBackStack() },
                            navController = navController
                        )
                    }
                    composable(
                        "assets_in_location/{location}",
                        arguments = listOf(navArgument("location") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val location = backStackEntry.arguments?.getString("location")
                            ?.replace("_", " ")
                            ?.split(" ")
                            ?.joinToString(" ") { it.capitalize() } ?: "Location"
                        AssetsInLocationScreen(
                            locationName = location,
                            onBackPressed = { navController.popBackStack() },
                            navController = navController
                        )
                    }
                    composable(
                        "tagged_assets_employee/{tag}/{employeeName}",
                        arguments = listOf(
                            navArgument("tag") { type = NavType.StringType },
                            navArgument("employeeName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val tag = backStackEntry.arguments?.getString("tag")
                            ?.replace("_", " ")
                            ?: "Unknown Tag"
                        val employeeName = backStackEntry.arguments?.getString("employeeName")
                            ?.replace("_", " ")
                            ?: "Employee"
                        TaggedAssetsByEmployeeScreen(
                            tagName = tag,
                            employeeName = employeeName,
                            onBackPressed = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}