package com.archeGlobal.one.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    const val REQUEST_STORAGE_PERMISSION = 1001
    
    private val STORAGE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            STORAGE_PERMISSIONS.all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                activity,
                STORAGE_PERMISSIONS,
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    fun shouldShowStoragePermissionRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            false
        } else {
            STORAGE_PERMISSIONS.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }
        }
    }
}