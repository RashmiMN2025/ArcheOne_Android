package com.archeGlobal.one

import android.app.Application
import android.util.Log
import com.archeGlobal.one.controller.SocialDataProvider
import com.archeGlobal.one.network.RetrofitClient
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class XOneApplication : Application() {
    
    lateinit var userDataManager: UserDataManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
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