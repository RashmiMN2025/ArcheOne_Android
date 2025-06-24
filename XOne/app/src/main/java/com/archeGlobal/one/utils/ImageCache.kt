package com.archeGlobal.one.utils

import android.content.Context
import android.util.Log
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Utility class to manage profile image caching and invalidation
 */
object ImageCache {
    private const val TAG = "ImageCache"

    // Version key to invalidate all images
    private var _profileImageVersion = MutableStateFlow(System.currentTimeMillis())
    val profileImageVersion: StateFlow<Long> = _profileImageVersion.asStateFlow()

    // Cache timeout in milliseconds (5 minutes)
    private const val CACHE_TIMEOUT = 5 * 60 * 1000L

    /**
     * Invalidates profile image cache, forcing a reload from source
     */
    fun invalidateProfileImageCache() {
        try {
            val newVersion = System.currentTimeMillis()
            _profileImageVersion.value = newVersion
            Log.d(TAG, "Profile image cache invalidated with version: $newVersion")
        } catch (e: Exception) {
            Log.e(TAG, "Error invalidating profile image cache", e)
        }
    }

    /**
     * Creates an ImageRequest for profile images with appropriate caching
     */
    fun createProfileImageRequest(
        context: Context,
        url: String,
        forceRefresh: Boolean = false
    ): ImageRequest {
        try {
            val cacheKey = getCacheKey(url)
            Log.d(TAG, "Creating profile image request for: $url with cache key: $cacheKey")

            return ImageRequest.Builder(context)
                .data(url)
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

            // Fallback to basic request in case of error
            return ImageRequest.Builder(context)
                .data(url)
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
