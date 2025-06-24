package com.archeGlobal.one

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.archeGlobal.one.utils.UserDataManager

class AppLifecycleObserver(
    private val app: Application
) : DefaultLifecycleObserver {

    private var lastBackgroundTime: Long = 0
    private val BACKGROUND_THRESHOLD = 1000 * 30 // 30 seconds

    override fun onStart(owner: LifecycleOwner) {
        // App comes to foreground
        val userDataManager = UserDataManager.getInstance(app)
        val now = System.currentTimeMillis()
        if (lastBackgroundTime == 0L) {
            // Cold start: always lock
            userDataManager.preferencesManager.setAppLockState(true)
        } else if (now - lastBackgroundTime > BACKGROUND_THRESHOLD) {
            userDataManager.preferencesManager.setAppLockState(true)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // App goes to background
        lastBackgroundTime = System.currentTimeMillis()
    }
}
