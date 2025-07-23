package com.archeGlobal.one.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object DeviceInfoUtils {

    fun getPlatform(): String = "android"

    fun getDeviceModel(): String = "${Build.MANUFACTURER} ${Build.MODEL}"

    fun getOSVersion(): String = Build.VERSION.RELEASE

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }

    fun getAllDeviceInfo(context: Context): DeviceInfo {
        return DeviceInfo(
            platform = getPlatform(),
            deviceModel = getDeviceModel(),
            osVersion = getOSVersion(),
            appVersion = getAppVersion(context)
        )
    }
}

data class DeviceInfo(
    val platform: String,
    val deviceModel: String,
    val osVersion: String,
    val appVersion: String
)
