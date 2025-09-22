package com.archeGlobal.one

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.GreetingDetailScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class GreetingDetailActivity : ComponentActivity() {
    private lateinit var navigator: AndroidNavigator
    private var allGreetings by mutableStateOf<List<String>>(emptyList())
    private var editableMessage by mutableStateOf("")
    private var selectedGreetingUrl by mutableStateOf("")

    private fun getBase64FromDrawable(resId: Int): String {
        val drawable = ContextCompat.getDrawable(applicationContext, resId) as? BitmapDrawable
        val bitmap = drawable?.bitmap ?: return ""
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val message = intent.getStringExtra("message") ?: ""
        val category = intent.getStringExtra("category") ?: "Greeting"
        val recipientEmail = intent.getStringExtra("recipientEmail") ?: ""
        val recipientName = intent.getStringExtra("recipientName") ?: ""
        val greetingsList = intent.getStringArrayListExtra("allGreetings")
        allGreetings = greetingsList ?: listOf(imageUrl)
        selectedGreetingUrl = imageUrl
        editableMessage = message

        navigator = AndroidNavigator(this)
        val userDataManager = UserDataManager.getInstance(this)
        val greetingsData = userDataManager.getGreetingsData()
        val categoryMessages = userDataManager.getGreetingCategoriesData()

        if (editableMessage.isEmpty()) {
            categoryMessages?.find { it.name == category }?.let { categoryData ->
                if (categoryData.message.isNotEmpty()) {
                    editableMessage = categoryData.message
                }
            }
        }

        setContent {
            XOneTheme {
                GreetingDetailScreen(
                    imageUrl = imageUrl,
                    categoryGreetings = allGreetings,
                    selectedGreetingUrl = selectedGreetingUrl,
                    message = editableMessage,
                    category = category,
                    onMessageChanged = { newMessage -> editableMessage = newMessage },
                    onGreetingSelected = { newGreetingUrl -> selectedGreetingUrl = newGreetingUrl },
                    onBackPressed = { finish() },
                    onSendGreeting = { sendGreeting(selectedGreetingUrl, editableMessage, category, recipientEmail) },
                    onSendInOutlook = { greetingUrl, msg -> sendGreetingInOutlook(greetingUrl, msg, category, recipientEmail) },
                )
            }
        }
    }

    private fun sendGreeting(
        imageUrl: String,
        message: String,
        category: String,
        recipientEmail: String = "",
    ) {
        android.widget.Toast
            .makeText(this, "Preparing greeting to send...", android.widget.Toast.LENGTH_SHORT)
            .show()
        downloadImageAndShare(imageUrl, message, category, recipientEmail)
    }

    private fun shareLinkOnly(
        imageUrl: String,
        message: String,
        category: String,
        recipientEmail: String = "",
    ) {
        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, category)
                val shareText =
                    if (message.isNotEmpty()) {
                        "$message\n\n$imageUrl"
                    } else {
                        imageUrl
                    }
                putExtra(Intent.EXTRA_TEXT, shareText)
                // Pre-fill recipient email if available
                if (recipientEmail.isNotEmpty()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                }
            }
        startActivity(Intent.createChooser(intent, "Send Greeting"))
    }

    private fun downloadImageAndShare(
        imageUrl: String,
        message: String,
        category: String,
        recipientEmail: String = "",
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageLoader = ImageLoader(this@GreetingDetailActivity)
                val request =
                    ImageRequest
                        .Builder(this@GreetingDetailActivity)
                        .data(imageUrl)
                        .allowHardware(false)
                        .build()
                val result = imageLoader.execute(request)
                val imageBitmap =
                    result.drawable?.let { drawable ->
                        when (drawable) {
                            is android.graphics.drawable.BitmapDrawable -> drawable.bitmap
                            else -> {
                                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 512
                                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 512
                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                bitmap
                            }
                        }
                    }
                val imageUri = saveBitmapToCache(imageBitmap)
                withContext(Dispatchers.Main) {
                    if (imageUri != null) {
                        val intent =
                            Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, imageUri)
                                putExtra(Intent.EXTRA_SUBJECT, category)
                                if (message.isNotEmpty()) {
                                    putExtra(Intent.EXTRA_TEXT, message)
                                }
                                // Pre-fill recipient email if available
                                if (recipientEmail.isNotEmpty()) {
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                                }
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        startActivity(Intent.createChooser(intent, "Send Greeting"))
                    } else {
                        shareLinkOnly(imageUrl, message, category, recipientEmail)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GreetingDetailActivity", "Error downloading image: ", e)
                withContext(Dispatchers.Main) {
                    shareLinkOnly(imageUrl, message, category, recipientEmail)
                }
            }
        }
    }

    // --- Send via Outlook with image URL and small display in body ---
    private fun sendGreetingInOutlook(
        imageUrl: String,
        message: String,
        category: String,
        recipientEmail: String = "",
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get user data for signature
                val userDataManager = UserDataManager.getInstance(this@GreetingDetailActivity)
                val userData = userDataManager.getUserData()
                val userName = userData?.name ?: "Your Name"
                val userDesignation = userData?.designation ?: "Your Designation"
                val userMobile = userData?.mobile ?: ""

                // Build HTML email with image URL (width 300px) and signature
                val htmlEmailContent =
                    createHtmlEmailWithImageUrl(
                        message,
                        imageUrl,
                        userName,
                        userDesignation,
                        userMobile,
                    )

                withContext(Dispatchers.Main) {
                    val emailIntent =
                        Intent(Intent.ACTION_SEND).apply {
                            setPackage("com.microsoft.office.outlook")
                            type = "text/html"
                            putExtra(Intent.EXTRA_SUBJECT, category)
                            putExtra(Intent.EXTRA_HTML_TEXT, htmlEmailContent)
                            putExtra(Intent.EXTRA_TEXT, message)
                            // Pre-fill recipient email if available
                            if (recipientEmail.isNotEmpty()) {
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                            }
                        }
                    try {
                        startActivity(emailIntent)
                    } catch (e: Exception) {
                        Log.e("GreetingDetailActivity", "Outlook HTML intent failed: ${e.message}")
                        shareLinkOnly(imageUrl, message, category, recipientEmail)
                    }
                }
            } catch (e: Exception) {
                Log.e("GreetingDetailActivity", "Error in sendGreetingInOutlook: ${e.message}")
                withContext(Dispatchers.Main) {
                    shareLinkOnly(imageUrl, message, category, recipientEmail)
                }
            }
        }
    }

    // Create HTML email with image URL (width 300px) and signature
    private fun createHtmlEmailWithImageUrl(
        message: String,
        imageUrl: String,
        userName: String,
        userDesignation: String,
        userMobile: String,
    ): String {
        val sanitizedMessage = message.replace("\n", "<br>")
        // Use the public URL for the signature icon
        val iconUrl = "https://dev.arche.global:7000/signature"
        val signatureImgTag = """<img src="$iconUrl" width="90" height="80" alt="User Icon" style="vertical-align: middle;"/>"""

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
                <style>
                    body { font-family: Arial, sans-serif; font-size: 16px; margin: 0; padding: 0; background-color: #f8f8f8; }
                    .email-container { width: 100%; max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; }
                    .message-text { margin-bottom: 50px; line-height: 1.6; color: #333333; }
                    .image-container { text-align: center; margin-bottom: 30px; }
                    .footer-text { font-size:12px; color:#777777; text-align:center; margin-top:20px; }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="message-text">
                        $sanitizedMessage
                        <br /><br />
                    </div>
                    
                    <div class="image-container">
                        <img src="$imageUrl" width="300" style="display:block; margin-top:10px;" /> 
                        <br /> <br />
                    </div>
                    
                    <p style="margin-top: 20px;">Best Regards,</p>
                    
                    <table style="margin-top: 10px;">
                        <tr>
                            <td style="vertical-align: middle;">
                                $signatureImgTag
                            </td>
                            <td style="padding-left: 18px; vertical-align: middle;">
                                <strong>$userName</strong><br/>
                                $userDesignation<br/>
                                $userMobile
                            </td>
                        </tr>
                    </table>
                </div>
            </body>
            </html>
            """.trimIndent()
    }

    private fun saveBitmapToCache(bitmap: Bitmap?): android.net.Uri? {
        if (bitmap == null) return null
        try {
            val cacheDir = cacheDir
            val file = java.io.File(cacheDir, "greeting_card_${System.currentTimeMillis()}.jpg")
            java.io.FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            return FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                file,
            )
        } catch (e: Exception) {
            android.util.Log.e("GreetingDetailActivity", "Error saving bitmap: ", e)
            return null
        }
    }
}
