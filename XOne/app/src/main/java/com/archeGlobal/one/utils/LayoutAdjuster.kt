package com.archeGlobal.one.utils

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Utility class to provide consistent layout adjustments across different devices.
 * This helps maintain UI consistency regardless of the screen size or density.
 */
class LayoutAdjuster(
    private val context: Context,
) {
    private val displayMetrics = context.resources.displayMetrics
    private val density = displayMetrics.density

    /**
     * Adjusts a dimension value based on device characteristics.
     * @param original The original dimension value in dp
     * @return Adjusted dimension value accounting for device specifics
     */
    fun adjustDimension(original: Dp): Dp {
        val adjustment =
            when {
                Build.MANUFACTURER.contains("samsung", ignoreCase = true) -> 0.95f
                density >= 3.5f -> 0.9f
                density <= 2.0f -> 1.1f
                else -> 1.0f
            }

        return (original.value * adjustment).dp
    }

    /**
     * Gets the optimal font size for the given base size,
     * adjusted for the current device.
     * @param baseSizeSp Base font size in sp
     * @return Adjusted font size that will display consistently
     */
    fun getAdjustedFontSize(baseSizeSp: Float): Float {
        val screenWidthDp = displayMetrics.widthPixels / density

        // Adjust font size based on screen width and device type
        val scaleFactor =
            when {
                screenWidthDp < 320 -> 0.85f // Very small screens
                screenWidthDp < 360 -> 0.9f // Small screens
                screenWidthDp > 480 -> 1.05f // Large screens
                else -> 1.0f // Normal screens
            }

        // Additional adjustment based on manufacturer if needed
        val manufacturerFactor =
            when {
                Build.MANUFACTURER.contains("samsung", ignoreCase = true) -> 0.95f
                Build.MANUFACTURER.contains("huawei", ignoreCase = true) -> 0.97f
                Build.MANUFACTURER.contains("xiaomi", ignoreCase = true) -> 0.98f
                else -> 1.0f
            }

        return baseSizeSp * scaleFactor * manufacturerFactor
    }

    /**
     * Determine if we should use compact layout components
     * for smaller screens
     */
    fun shouldUseCompactLayout(): Boolean {
        val screenWidthDp = displayMetrics.widthPixels / density
        return screenWidthDp < 360
    }
}

/**
 * Composable function to provide a LayoutAdjuster instance
 * optimized for the current device.
 */
@Composable
fun rememberLayoutAdjuster(): LayoutAdjuster {
    val context = LocalContext.current
    return remember { LayoutAdjuster(context) }
}
