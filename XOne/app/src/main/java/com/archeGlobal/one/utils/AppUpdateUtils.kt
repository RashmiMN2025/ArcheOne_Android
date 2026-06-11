package com.archeGlobal.one.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

object AppUpdateUtils {
    private const val TAG = "AppUpdateUtils"
    private const val UPDATE_REQUEST_CODE = 1234

    /**
     * Performs an official IMMEDIATE in-app update check.
     * Use this for LoginActivity to update without leaving the app.
     */
    fun startImmediateUpdateFlow(activity: Activity) {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting immediate flow", e)
                }
            }
        }.addOnFailureListener { e ->
            if (e is com.google.android.play.core.install.InstallException && e.errorCode == -10) {
                Log.w(TAG, "In-App Update: App not installed from Play Store. Skipping proactive check.")
            } else {
                Log.e(TAG, "Error checking for immediate updates", e)
            }
        }
    }

    /**
     * Checks if an update is available and triggers a callback.
     * Use this for HomeActivity to show custom logout dialog.
     */
    fun checkForUpdates(activity: Activity, onUpdateAvailable: () -> Unit) {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                Log.d(TAG, "Update available on Play Store. Triggering callback.")
                onUpdateAvailable()
            }
        }.addOnFailureListener { e ->
            if (e is com.google.android.play.core.install.InstallException && e.errorCode == -10) {
                Log.w(TAG, "In-App Update: App not installed from Play Store. Skipping proactive check.")
            } else {
                Log.e(TAG, "Error checking for updates", e)
            }
        }
    }
}
