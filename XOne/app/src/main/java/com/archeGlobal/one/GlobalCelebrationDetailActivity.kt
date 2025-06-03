package com.archeGlobal.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.archeGlobal.one.ui.theme.XOneTheme
import com.google.gson.Gson
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GlobalCelebrationDetailActivity : ComponentActivity() {
    private var allGreetings by mutableStateOf<List<String>>(emptyList())
    private var editableMessage by mutableStateOf("")
    private var selectedGreetingUrl by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get data from intent (like GreetingDetailActivity)
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val message = intent.getStringExtra("message") ?: ""
        val category = intent.getStringExtra("category") ?: "Global Celebration"
        val greetingsList = intent.getStringArrayListExtra("allGreetings")
        allGreetings = greetingsList ?: listOf(imageUrl)
        selectedGreetingUrl = imageUrl
        editableMessage = message

        setContent {
            XOneTheme {
                com.archeGlobal.one.ui.screens.GlobalCelebrationDetailScreen(
                    subcategory = com.archeGlobal.one.model.GreetingSubcategory(
                        id = 0, // You may want to pass the real id
                        name = category,
                        files = allGreetings,
                        message = editableMessage
                    ),
                    onBackPressed = { finish() },
                    onSendGreeting = { imageUrl, message -> sendGreeting(imageUrl, message, category) },
                    onSendInOutlook = { imageUrl, message -> sendGreetingInOutlook(imageUrl, message, category) }
                )
            }
        }
    }

    private fun sendGreeting(imageUrl: String, message: String, category: String) {
        android.widget.Toast.makeText(this, "Preparing greeting to send...", android.widget.Toast.LENGTH_SHORT).show()
        downloadImageAndShare(imageUrl, message, category)
    }

    private fun sendGreetingInOutlook(imageUrl: String, message: String, category: String) {
        // This will open the Outlook app with the greeting and message
        downloadImageAndShareOutlook(imageUrl, message, category)
    }

    private fun downloadImageAndShare(imageUrl: String, message: String, category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageLoader = ImageLoader(this@GlobalCelebrationDetailActivity)
                val request = ImageRequest.Builder(this@GlobalCelebrationDetailActivity)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                val imageBitmap = result.drawable?.let { drawable ->
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
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, imageUri)
                            putExtra(Intent.EXTRA_SUBJECT, category)
                            if (message.isNotEmpty()) {
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(intent, "Send Greeting"))
                    } else {
                        shareLinkOnly(imageUrl, message, category)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GlobalCelebrationDetailActivity", "Error downloading image: ", e)
                withContext(Dispatchers.Main) {
                    shareLinkOnly(imageUrl, message, category)
                }
            }
        }
    }    private fun downloadImageAndShareOutlook(imageUrl: String, message: String, category: String) {
        android.widget.Toast.makeText(this, "Preparing email for Outlook...", android.widget.Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageLoader = ImageLoader(this@GlobalCelebrationDetailActivity)
                val request = ImageRequest.Builder(this@GlobalCelebrationDetailActivity)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()
                val result = imageLoader.execute(request)
                val originalBitmap = result.drawable?.let { drawable ->
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
                
                if (originalBitmap == null) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(this@GlobalCelebrationDetailActivity, "Could not load image.", android.widget.Toast.LENGTH_SHORT).show()
                        shareLinkOnly(imageUrl, message, category)
                    }
                    return@launch
                }
                
                // Get user data for footer
                val userDataManager = com.archeGlobal.one.utils.UserDataManager.getInstance(this@GlobalCelebrationDetailActivity)
                val userData = userDataManager.getUserData()
                val userName = userData?.name ?: "Your Name"
                val userDesignation = userData?.designation ?: "Your Designation"
                val userMobile = userData?.mobile ?: " "
                
                // Use the direct imageUrl for the HTML email content
                val htmlEmailContent = createRichHtmlEmail(message, imageUrl, userName, userDesignation, userMobile)
                
                // Prepare image for attachment (better quality, for fallback)
                val bitmapForAttachment = createOutlookOptimizedBitmap(originalBitmap)
                val imageUriForAttachment = saveBitmapToCache(bitmapForAttachment)
                
                withContext(Dispatchers.Main) {
                    // Primary Attempt: Pure HTML
                    val pureHtmlIntent = Intent(Intent.ACTION_SEND).apply {
                        setPackage("com.microsoft.office.outlook")
                        type = "text/html"
                        putExtra(Intent.EXTRA_SUBJECT, category)
                        // For text/html, EXTRA_HTML_TEXT is the primary content.
                        // EXTRA_TEXT can serve as a fallback if HTML isn't rendered.
                        putExtra(Intent.EXTRA_TEXT, message) // Plain text version of the message
                        putExtra(Intent.EXTRA_HTML_TEXT, htmlEmailContent)
                    }
                    
                    try {
                        startActivity(pureHtmlIntent)
                        android.util.Log.d("GlobalCelebrationDetailActivity", "Attempted Outlook with pure HTML intent.")
                    } catch (e: Exception) {
                        android.util.Log.e("GlobalCelebrationDetailActivity", "Outlook pure HTML intent failed: ${e.message}. Falling back to attachment method.", e)
                        
                        // Fallback Strategy: Send as image attachment with clear instructions.
                        if (imageUriForAttachment != null) {
                            val attachmentIntent = Intent(Intent.ACTION_SEND).apply {
                                setPackage("com.microsoft.office.outlook")
                                type = "image/jpeg" 
                                putExtra(Intent.EXTRA_SUBJECT, category)
                                val instructionMessage = """
                                    $message
                                    
                                    ---
                                    The image is attached. To add it to your email body:
                                    1. Tap and hold the image attachment.
                                    2. Select "Add to Body" or a similar option.
                                """.trimIndent()
                                putExtra(Intent.EXTRA_TEXT, instructionMessage)
                                putExtra(Intent.EXTRA_STREAM, imageUriForAttachment)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                startActivity(attachmentIntent)
                                android.widget.Toast.makeText(this@GlobalCelebrationDetailActivity, "Image attached. Tap & hold to add to body.", android.widget.Toast.LENGTH_LONG).show()
                            } catch (e2: Exception) {
                                android.util.Log.e("GlobalCelebrationDetailActivity", "Outlook attachment fallback intent failed: ${e2.message}", e2)
                                shareLinkOnly(imageUrl, message, category) // Ultimate fallback
                            }
                        } else {
                            android.util.Log.e("GlobalCelebrationDetailActivity", "Image URI for attachment was null, falling back to link only.")
                            shareLinkOnly(imageUrl, message, category)
                        }
                    }
                }            } catch (e: Exception) {
                android.util.Log.e("GlobalCelebrationDetailActivity", "Error downloading image for Outlook: ", e)
                withContext(Dispatchers.Main) {
                    shareLinkOnly(imageUrl, message, category)
                }
            }
        }
    }
    
    /**
     * Creates a bitmap specifically optimized for Outlook embedding.
     */
    private fun createOutlookOptimizedBitmap(bitmap: Bitmap): Bitmap {
        val maxWidth = 600
        val maxHeight = 800
        val targetConfig = bitmap.config ?: Bitmap.Config.ARGB_8888

        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        if (originalWidth <= maxWidth && originalHeight <= maxHeight && bitmap.config == targetConfig) {
            return if (bitmap.isMutable) bitmap.copy(targetConfig, false) else bitmap
        }

        var newWidth = originalWidth
        var newHeight = originalHeight

        val ratioBitmap = originalWidth.toFloat() / originalHeight.toFloat()
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            if (originalWidth.toFloat() / maxWidth.toFloat() > originalHeight.toFloat() / maxHeight.toFloat()) {
                newWidth = maxWidth
                newHeight = (newWidth / ratioBitmap).toInt()
            } else {
                newHeight = maxHeight
                newWidth = (newHeight * ratioBitmap).toInt()
            }
        }
        
        if (newWidth <= 0) newWidth = 1
        if (newHeight <= 0) newHeight = 1

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        if (scaledBitmap.config != targetConfig) {
            val finalBitmap = Bitmap.createBitmap(newWidth, newHeight, targetConfig)
            val canvas = android.graphics.Canvas(finalBitmap)
            val paint = android.graphics.Paint().apply {
                isFilterBitmap = true
                isAntiAlias = true
            }
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
            if (scaledBitmap != bitmap && scaledBitmap != finalBitmap && !scaledBitmap.isRecycled) {
                scaledBitmap.recycle()
            }
            return finalBitmap
        }
        
        return scaledBitmap
    }

    /**
     * Creates a rich HTML email with an embedded image via URL.
     */
    private fun createRichHtmlEmail(
    message: String,
    imageUrl: String,
    userName: String,
    userDesignation: String,
    userMobile: String
): String {
    val sanitizedMessage = message.replace("\n", "<br />")
    // Use the public URL for the signature icon
    val iconUrl = "https://pulse.netcon.in:7000/signature"
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

    private fun shareLinkOnly(imageUrl: String, message: String, category: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, category)
            val shareText = if (message.isNotEmpty()) {
                "$message\n\n$imageUrl"
            } else {
                imageUrl
            }
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Send Greeting"))
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
                "${packageName}.provider",
                file
            )
        } catch (e: Exception) {
            android.util.Log.e("GlobalCelebrationDetailActivity", "Error saving bitmap: ", e)
            return null
        }
    }
}
