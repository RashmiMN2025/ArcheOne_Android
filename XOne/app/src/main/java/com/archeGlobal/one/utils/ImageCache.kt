package com.archeGlobal.one.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import coil.request.CachePolicy
import coil.request.ImageRequest

/**
 * Utility class to manage profile image caching and invalidation
 */
object ImageCache {
    private const val TAG = "ImageCache"

    // Version key to invalidate all images
    private var _profileImageVersion = mutableStateOf(0)
    val profileImageVersion: State<Int> = _profileImageVersion

    // Cache timeout is not used directly but kept for reference
    private const val CACHE_TIMEOUT = 5 * 60 * 1000L

    /**
     * Invalidates profile image cache, forcing a reload from source
     */
    fun invalidateProfileImageCache() {
        try {
            _profileImageVersion.value += 1
            Log.d(TAG, "Profile image cache invalidated with version: ${_profileImageVersion.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error invalidating profile image cache", e)
        }
    }

    /**
     * Creates an ImageRequest for profile images with appropriate caching
     */
    fun createProfileImageRequest(
        context: Context,
        url: String?,
        forceRefresh: Boolean = false
    ): ImageRequest {
        try {
            val safeUrl = url ?: ""
            val cacheKey = getCacheKey(safeUrl)
            Log.d(TAG, "Creating profile image request for: $url with cache key: $cacheKey")

            return ImageRequest.Builder(context)
                .data(safeUrl)
                .crossfade(true)
                .placeholder(com.archeGlobal.one.R.drawable.ic_person)
                .error(com.archeGlobal.one.R.drawable.ic_person)
                .memoryCacheKey(cacheKey)
                .diskCacheKey(cacheKey)
                .memoryCachePolicy(if (forceRefresh) CachePolicy.DISABLED else CachePolicy.ENABLED)
                .diskCachePolicy(if (forceRefresh) CachePolicy.DISABLED else CachePolicy.ENABLED)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating profile image request", e)

            // Fallback to basic request with placeholder
            return ImageRequest.Builder(context)
                .data("")
                .placeholder(com.archeGlobal.one.R.drawable.ic_person)
                .error(com.archeGlobal.one.R.drawable.ic_person)
                .build()
        }
    }

    /**
     * Creates a cache key that includes version for proper invalidation
     */
    private fun getCacheKey(url: String): String {
        return "profile_${url}_${_profileImageVersion.value}"
    }
}