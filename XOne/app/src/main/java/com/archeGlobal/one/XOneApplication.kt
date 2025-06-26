package com.archeGlobal.one

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.archeGlobal.one.controller.SocialDataProvider
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import com.archeGlobal.one.utils.forceAppFontScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class XOneApplication : Application() {

    lateinit var userDataManager: UserDataManager
        private set

    private lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        appLifecycleObserver = AppLifecycleObserver(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)

        // Force a consistent font scale across all devices
        resources.forceAppFontScale(1.0f)

        // Initialize RetrofitClient with application context
        try {
            RetrofitClient.initialize(applicationContext)
            Log.d("XOneApplication", "RetrofitClient initialized successfully")
        } catch (e: Exception) {
            Log.e("XOneApplication", "Error initializing RetrofitClient: ${e.message}", e)
        }

        // Initialize UserDataManager
        try {
            userDataManager = UserDataManager.getInstance(applicationContext)
            Log.d("XOneApplication", "UserDataManager initialized successfully")
        } catch (e: Exception) {
            Log.e("XOneApplication", "Error initializing UserDataManager: ${e.message}", e)
        }

        // Begin preloading social content data
        preloadSocialData()
    }

    // Override configuration changes to maintain our font scale
    override fun onConfigurationChanged(newConfig: Configuration) {
        // Create a new configuration with our forced font scale
        val forcedConfig = Configuration(newConfig)
        forcedConfig.fontScale = 1.0f

        // Apply the configuration
        val displayMetrics = resources.displayMetrics
        resources.updateConfiguration(forcedConfig, displayMetrics)

        super.onConfigurationChanged(forcedConfig)
    }

    private fun preloadSocialData() {
        // Start on background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("XOneApplication", "Starting social data preloading")
                SocialDataProvider.getInstance(applicationContext).preloadData()
            } catch (e: Exception) {
                Log.e("XOneApplication", "Error preloading social data: ${e.message}", e)
            }
        }
    }

    fun getAppLifecycleObserver(): AppLifecycleObserver {
        return appLifecycleObserver
    }

    companion object {
        private var instance: XOneApplication? = null

        fun getInstance(): XOneApplication {
            return instance ?: throw IllegalStateException("Application not created yet")
        }
    }

    init {
        instance = this
    }
}
