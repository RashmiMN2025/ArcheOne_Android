package com.archeGlobal.one.controller

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.archeGlobal.one.BusinessCardActivity
import com.archeGlobal.one.R
import com.archeGlobal.one.model.BusinessCardModel
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.utils.QRCodeGenerator
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

interface BusinessCardController {
    val showEditCardDialog: MutableState<Boolean>
    val businessCard: BusinessCardModel
    fun onBackPressed()
    fun onDownloadCard(bitmap: Bitmap)
    fun onShareCard(bitmap: Bitmap)
    fun onEditCard()
    fun onCardUpdated(newLocation: String, newPhone: String)
    fun refreshQRCode(isPortraitMode: Boolean)
}

class BusinessCardControllerImpl(
    private val context: Context,
    private val navigator: AndroidNavigator
) : BusinessCardController {

    override val showEditCardDialog: MutableState<Boolean> = mutableStateOf(false)

    override val businessCard: BusinessCardModel
        get() = _businessCard.value

    override fun onEditCard() {
        showEditCardDialog.value = true
    }

    override fun onCardUpdated(newLocation: String, newPhone: String) {
        var isValid = true
        var message = ""

        if (newLocation.isEmpty()) {
            isValid = false
            message = "Location cannot be empty!"
        }

        if (newPhone.isEmpty()) {
            isValid = false
            message = if (message.isEmpty()) "Phone number cannot be empty!" else "Location and phone number cannot be empty!"
        }

        if (isValid) {
            val locationValue = if (newLocation.trim().equals("N/A", ignoreCase = true)) {
                "Bangalore"
            } else {
                newLocation
            }

            val updatedCard = _businessCard.value.copy(
                location = locationValue,
                phone = formatPhoneNumber(newPhone)
            )
            _businessCard.value = generateQRCodeForCard(updatedCard)

            showEditCardDialog.value = false
            Toast.makeText(context, "Card updated!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.trim().replace(" ", "").replace("-", "")

        if (cleanPhone.isEmpty()) return ""

        // Remove any non-digit characters except + at the beginning
        val digitsOnly = if (cleanPhone.startsWith("+")) {
            "+" + cleanPhone.substring(1).filter { it.isDigit() }
        } else {
            cleanPhone.filter { it.isDigit() }
        }

        return when {
            // Already has +91 prefix
            digitsOnly.startsWith("+91") -> {
                val numberPart = digitsOnly.substring(3)
                when {
                    numberPart.length == 10 -> digitsOnly // Perfect: +91 + 10 digits = 12 total
                    numberPart.length < 10 -> "+91" + numberPart.padEnd(10, '0') // Pad with zeros if needed
                    else -> "+91" + numberPart.take(10) // Truncate if too long
                }
            }
            // Starts with 91 and has exactly 12 digits total (treat 91 as country code)
            digitsOnly.startsWith("91") && digitsOnly.length == 12 -> {
                "+$digitsOnly"
            }
            // Has exactly 10 digits (complete Indian mobile number)
            digitsOnly.length == 10 -> {
                "+91$digitsOnly"
            }
            // Starts with 91 but not 12 digits total (91 might be part of the number)
            digitsOnly.startsWith("91") -> {
                when {
                    digitsOnly.length < 10 -> "+91" + digitsOnly.padEnd(10, '0') // Pad with zeros if needed
                    digitsOnly.length > 10 && digitsOnly.length != 12 -> "+91" + digitsOnly.take(10) // Truncate if too long but not 12 digits
                    else -> "+91$digitsOnly" // Fallback
                }
            }
            // Any other case
            else -> {
                when {
                    digitsOnly.length < 10 -> "+91" + digitsOnly.padEnd(10, '0') // Pad with zeros if needed
                    else -> "+91" + digitsOnly.take(10) // Truncate if too long
                }
            }
        }
    }

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
    } // Change _businessCard to MutableState
    private val _businessCard = mutableStateOf(
        OtpVerificationController.getUserData()?.let { userData ->
            // Process the location - if it's N/A, replace with Bangalore
            val locationValue = if (userData.location == "N/A" || userData.location.isEmpty()) {
                "Bangalore"
            } else {
                userData.location
            }

            val card = BusinessCardModel(
                companyLogo = R.drawable.arche,
                name = userData.name,
                designation = userData.designation,
                department = userData.department,
                email = userData.email,
                phone = formatPhoneNumber(userData.mobile),
                location = locationValue, // Updated location with Bangalore fallback
                website = "www.arche.global"
            )

            // Generate QR code for the card
            generateQRCodeForCard(card)
        } ?: BusinessCardModel(
            // Fallback default values if userData is null
            companyLogo = R.drawable.arche,
            name = "",
            designation = "",
            department = "",
            email = "",
            phone = "",
            location = "",
            website = ""
        )
    )

    // Generate QR code for a business card and return a new card with QR code
    private fun generateQRCodeForCard(card: BusinessCardModel, isPortrait: Boolean = true): BusinessCardModel {
        val qrCode = QRCodeGenerator.generateQRCode(
            name = card.name,
            title = card.designation,
            email = card.email,
            phone = card.phone,
            location = card.location,
            layoutType = if (isPortrait) {
                QRCodeGenerator.QRLayoutType.VERTICAL
            } else {
                QRCodeGenerator.QRLayoutType.HORIZONTAL
            },
            size = if (isPortrait) 240 else 140 // Changed from 100 to 140 to match 70dp on screen size
        )

        return card.copy(qrCode = qrCode)
    }

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
        (context as? BusinessCardActivity)?.finishWithAnimation()
    }

    override fun refreshQRCode(isPortraitMode: Boolean) {
        _businessCard.value = generateQRCodeForCard(_businessCard.value, isPortraitMode)
    }

    companion object {
        const val CHANNEL_ID = "business_card_channel"
    }
}
