package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.CollateralDetailActivity
import com.archeGlobal.one.WebViewActivity
import com.archeGlobal.one.network.SmartCollateralCategory
import com.archeGlobal.one.network.SmartCollateralFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CollateralController(
    private val context: Context,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _categories = mutableStateOf<List<SmartCollateralCategory>>(emptyList())
    val categories: List<SmartCollateralCategory> get() = _categories.value

    fun loadCollateral(smartCollateral: List<SmartCollateralCategory>?) {
        _isLoading.value = true
        coroutineScope.launch {
            try {
                if (!smartCollateral.isNullOrEmpty()) {
                    _categories.value = smartCollateral
                    Log.d("CollateralController", "Loaded ${smartCollateral.size} categories")
                } else {
                    Log.e("CollateralController", "No Smart Collateral data available")
                    Toast.makeText(context, "No Smart Collateral data available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CollateralController", "Error loading collateral: ${e.message}", e)
                Toast.makeText(context, "Failed to load collateral", Toast.LENGTH_SHORT).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onCategoryClick(category: SmartCollateralCategory) {
        val intent =
            Intent(context, CollateralDetailActivity::class.java).apply {
                putExtra("categoryName", category.name)
            }
        context.startActivity(intent)
    }

    fun onFileClick(file: SmartCollateralFile) {
        val fileUrl = "${file.fileUrl}"
        val intent =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra("fileUrl", fileUrl)
                putExtra("title", file.fileName)
                putExtra("isPdf", true)
                putExtra("usePdfJs", true)
            }
        context.startActivity(intent)
    }

    fun onBackPressed(activity: android.app.Activity) {
        activity.finish()
    }

    fun onCleared() {
        coroutineScope.cancel()
    }
}
