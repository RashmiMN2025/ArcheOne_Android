package com.archeGlobal.one.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.archeGlobal.one.R
import java.io.File

/**
 * Helper class for showing download notifications
 */
object DownloadNotificationHelper {

    private const val CHANNEL_ID = "download_channel"
    private const val CHANNEL_NAME = "Downloads"
    private const val CHANNEL_DESCRIPTION = "Notifications for downloaded files"
    private const val NOTIFICATION_ID = 1001

    /**
     * Check if notification permission is granted (for Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not needed for Android 12 and below
        }
    }

    /**
     * Request notification permission (for Android 13+)
     * Call this from an Activity
     */
    fun requestNotificationPermission(activity: android.app.Activity, requestCode: Int = 1001) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.app.ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                requestCode
            )
        }
    }

    /**
     * Initialize notification channel (required for Android O and above)
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification for downloaded file
     * @param context Application context
     * @param fileName Name of the downloaded file
     * @param filePath Full path to the downloaded file
     * @param fileDescription Description to show in notification (e.g., "Travel Admin Report")
     */
    fun showDownloadNotification(
        context: Context,
        fileName: String,
        filePath: String,
        fileDescription: String = "Report downloaded"
    ) {
        try {
            android.util.Log.d("DownloadNotification", "Attempting to show notification for: $fileName")

            // Check notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                android.util.Log.d("DownloadNotification", "Android 13+ - Has notification permission: $hasPermission")

                if (!hasPermission) {
                    android.util.Log.w("DownloadNotification", "POST_NOTIFICATIONS permission not granted")
                    return
                }
            }

            // Create notification channel
            createNotificationChannel(context)
            android.util.Log.d("DownloadNotification", "Notification channel created")

            // Build notification without intent (simple notification)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done) // Use system download icon
                .setContentTitle("Download Complete")
                .setContentText(fileDescription)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$fileDescription\nFile: $fileName\nSaved to Downloads folder")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Remove notification when tapped
                .build()

            // Show notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            android.util.Log.d("DownloadNotification", "Notification displayed successfully")
        } catch (e: Exception) {
            android.util.Log.e("DownloadNotification", "Error showing notification", e)
        }
    }

    /**
     * Show notification with custom notification ID (useful for multiple downloads)
     */
    fun showDownloadNotification(
        context: Context,
        fileName: String,
        filePath: String,
        fileDescription: String = "Report downloaded",
        notificationId: Int
    ) {
        try {
            // Create notification channel
            createNotificationChannel(context)

            // Build notification without intent (simple notification)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download Complete")
                .setContentText(fileDescription)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("$fileDescription\nFile: $fileName\nSaved to Downloads folder")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            // Show notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            android.util.Log.e("DownloadNotification", "Error showing notification", e)
        }
    }
}
