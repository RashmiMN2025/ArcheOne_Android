// controllers/AssetITAdminController.kt
package com.archeGlobal.one.controllers

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.archeGlobal.one.ui.screens.AssetAdminItem
import com.archeGlobal.one.ui.screens.assetAdminItem

class AssetITAdminController : ViewModel() {
    val items: List<AssetAdminItem> = assetAdminItem()

    private var navController: NavController? = null

    fun initNavController(navController: NavController) {
        this.navController = navController
    }

    fun navigateBack() {
        navController?.popBackStack()
    }

    fun onInventoryItemClick(assetName: String) {
        val safeName = assetName.lowercase().replace(" ", "_")
        navController?.navigate("inventory_detail/$safeName")
    }
}