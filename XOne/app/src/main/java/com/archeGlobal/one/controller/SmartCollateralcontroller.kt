package com.archeGlobal.one.controller

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.archeGlobal.one.network.SmartCollateralCategory

class SmartCollateralController(
    private val context: Context,
) {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var smartCollateralList: List<SmartCollateralCategory> = emptyList()

    fun setSmartCollateralData(list: List<SmartCollateralCategory>) {
        _isLoading.value = true
        smartCollateralList = list
        _isLoading.value = false
    }

    fun getSmartCollateralList(): List<SmartCollateralCategory> = smartCollateralList

    fun onCategoryClick(category: SmartCollateralCategory) {
        val intent =
            android.content.Intent(context, com.archeGlobal.one.CollateralDetailActivity::class.java).apply {
                putExtra("categoryName", category.name)
            }
        context.startActivity(intent)
    }
}
