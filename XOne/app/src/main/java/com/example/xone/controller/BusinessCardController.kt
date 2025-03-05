package com.example.xone.controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import com.example.xone.R
import com.example.xone.model.BusinessCardModel
import com.example.xone.navigation.AndroidNavigator
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

interface BusinessCardController {
    fun onBackPressed()
    fun onDownloadCard(bitmap: Bitmap)
    fun onShareCard(bitmap: Bitmap)
}

class BusinessCardControllerImpl(
    private val context: Context,
    private val navigator: AndroidNavigator
) : BusinessCardController {
    // Safely access notification manager - will be null in preview
    private val notificationManager by lazy {
        try {
            ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            )
        } catch (e: Exception) {
            Log.d("BusinessCardController", "NotificationManager not available - likely in preview mode")
            null
        }
    }

    init {
        // Only create channel if notification manager is available
        if (notificationManager != null) {
            createNotificationChannel()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Business Card Downloads",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for downloaded business cards"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Get user data from LoginController
    val businessCard = LoginController.getUserData()?.let { userData ->
        BusinessCardModel(
            companyLogo = R.drawable.arche,
            name = userData.name,
            designation = userData.designation,
            department = userData.department,
            email = userData.email,
            phone = userData.mobile,
            location = "Bangalore", // Note: Location is not currently part of UserData
            qrCode = "" // Generate QR code string here
        )
    } ?: BusinessCardModel(
        // Fallback default values if userData is null
        companyLogo = R.drawable.arche,
        name = "",
        designation = "",
        department = "",
        email = "",
        phone = "",
        location = "",
        qrCode = ""
    )
    
    override fun onDownloadCard(bitmap: Bitmap) {
        try {
            // Create a file in the Downloads directory
            val fileName = "business_card_${System.currentTimeMillis()}.png"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val businessCardsDir = File(downloadsDir, "BusinessCards")
            if (!businessCardsDir.exists()) {
                businessCardsDir.mkdirs()
            }
            
            val imageFile = File(businessCardsDir, fileName)
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            if (checkNotificationPermission()) {
                // Create intent to open the file
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    imageFile
                )
                
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(contentUri, "image/png")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    viewIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                // Build notification
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_download)
                    .setContentTitle("Business Card Downloaded")
                    .setContentText("Tap to view your business card")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                // Use a unique notification ID and safely access notificationManager
                val notificationId = System.currentTimeMillis().toInt()
                notificationManager?.notify(notificationId, notification)
            }

            // Show toast
            android.widget.Toast.makeText(
                context,
                "Business card saved to Downloads/BusinessCards",
                android.widget.Toast.LENGTH_SHORT
            ).show()

        } catch (e: IOException) {
            e.printStackTrace()
            android.widget.Toast.makeText(
                context,
                "Failed to save business card",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onShareCard(bitmap: Bitmap) {
        try {
            // Save bitmap to cache directory
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val fileName = "shared_business_card.png"
            val stream = FileOutputStream("$cachePath/$fileName")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imagePath = File(cachePath, fileName)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imagePath
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Business Card"))

        } catch (e: IOException) {
            e.printStackTrace()
            android.widget.Toast.makeText(
                context,
                "Failed to share business card",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    override fun onBackPressed() {
        navigator.navigateToHome()  // Navigate back to home screen
    }

    companion object {
        const val CHANNEL_ID = "business_card_channel"
    }
} 