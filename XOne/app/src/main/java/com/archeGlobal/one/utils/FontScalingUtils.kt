package com.archeGlobal.one.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density

/**
 * Custom density for font scaling that can override system font scale.
 */
data class AppFontScaleAdjustment(
    val fontScale: Float = 1.0f,
    val ignoreSystemFontScale: Boolean = true
)

// Create a CompositionLocal to provide the font scale adjustment
val LocalAppFontScaleAdjustment = compositionLocalOf { AppFontScaleAdjustment() }

/**
 * Composable that provides a consistent font scaling experience across different devices.
 * This wraps the content with a custom font density configuration that can optionally
 * ignore the system font scale settings.
 */
@Composable
fun FontScaleAdjusted(
    fontScaleAdjustment: AppFontScaleAdjustment = AppFontScaleAdjustment(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // Create a custom density that applies our font scale
    val customDensity = remember(density, fontScaleAdjustment) {
        val systemFontScale = if (fontScaleAdjustment.ignoreSystemFontScale) 1.0f else density.fontScale
        val adjustedFontScale = systemFontScale * fontScaleAdjustment.fontScale

        Density(
            density = density.density,
            fontScale = adjustedFontScale
        )
    }

    CompositionLocalProvider(
        LocalDensity provides customDensity,
        LocalAppFontScaleAdjustment provides fontScaleAdjustment,
        content = content
    )
}

/**
 * Extension function to adjust text style with a consistent scale factor.
 * Useful for ensuring text renders consistently across devices.
 */
fun TextStyle.withConsistentFontSize(sizeFactor: Float = 1.0f): TextStyle {
    return copy(fontSize = fontSize * sizeFactor)
}

/**
 * Helper function to get device-specific font adjustments.
 * This can be expanded to include more device-specific configurations.
 */
fun getDeviceSpecificFontAdjustment(context: Context): AppFontScaleAdjustment {
    val displayMetrics = context.resources.displayMetrics
    val density = displayMetrics.density

    // Adjust based on screen density and manufacturer/model if needed
    return when {
        Build.MANUFACTURER.contains("samsung", ignoreCase = true) -> {
            AppFontScaleAdjustment(fontScale = 0.95f)
        }
        density >= 3.5f -> {
            // For very high density devices
            AppFontScaleAdjustment(fontScale = 0.9f)
        }
        density <= 2.0f -> {
            // For lower density devices
            AppFontScaleAdjustment(fontScale = 1.1f)
        }
        else -> AppFontScaleAdjustment()
    }
}

/**
 * Force a specific font scale for the app, ignoring system settings.
 * This should be called in Application.onCreate() or early in the app lifecycle.
 */
fun Resources.forceAppFontScale(fontScale: Float) {
    val configuration = Configuration(configuration)
    configuration.fontScale = fontScale
    updateConfiguration(configuration, displayMetrics)
}
