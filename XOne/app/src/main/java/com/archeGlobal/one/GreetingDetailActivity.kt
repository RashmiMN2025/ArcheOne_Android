package com.archeGlobal.one

import android.content.Intent
import android.graphics.Bitmap // Ensure Bitmap is imported
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.navigation.AndroidNavigator
import com.archeGlobal.one.ui.screens.GreetingDetailScreen
import com.archeGlobal.one.ui.theme.XOneTheme
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.ImageLoader
import coil.request.ImageRequest
import android.util.Base64
import android.util.Log
import androidx.core.text.HtmlCompat
import java.io.ByteArrayOutputStream

class GreetingDetailActivity : ComponentActivity() {
    private lateinit var navigator: AndroidNavigator
    private var allGreetings by mutableStateOf<List<String>>(emptyList())
    private var editableMessage by mutableStateOf("")    // Track the currently selected greeting URL
    private var selectedGreetingUrl by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get data from intent
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val message = intent.getStringExtra("message") ?: ""
        val category = intent.getStringExtra("category") ?: "Greeting"
        
        // Initialize selected greeting and message
        selectedGreetingUrl = imageUrl
        editableMessage = message
        
        // Initialize navigator
        navigator = AndroidNavigator(this)
          // Get all greetings in this category from UserDataManager
        val userDataManager = UserDataManager.getInstance(this)
        val greetingsData = userDataManager.getGreetingsData()
        val categoryMessages = userDataManager.getGreetingCategoriesData()
        
        // Get greetings for this category or use default
        allGreetings = greetingsData?.get(category) ?: listOf(imageUrl)
        
        // If message is empty, try to get default message for this category
        if (editableMessage.isEmpty()) {
            // Find category message from API data
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
                    category = category,                    onMessageChanged = { newMessage -> editableMessage = newMessage },
                    onGreetingSelected = { newGreetingUrl -> selectedGreetingUrl = newGreetingUrl },                    onBackPressed = { finish() },
                    onSendGreeting = { sendGreeting(selectedGreetingUrl, editableMessage, category) },
                    onSendInOutlook = { greetingUrl, msg -> sendGreetingInOutlook(greetingUrl, msg, category) }
                )
            }
        }
    }private fun sendGreeting(imageUrl: String, message: String, category: String) {
        // Show loading toast
        android.widget.Toast.makeText(this, "Preparing greeting to send...", android.widget.Toast.LENGTH_SHORT).show()
        
        // Download the image and share it
        downloadImageAndShare(imageUrl, message, category)
    }
      private fun downloadImageAndShare(imageUrl: String, message: String, category: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use Coil to download the image
                val imageLoader = ImageLoader(this@GreetingDetailActivity)
                val request = ImageRequest.Builder(this@GreetingDetailActivity)
                    .data(imageUrl)
                    .allowHardware(false) // Important for accessing pixels
                    .build()
                
                val result = imageLoader.execute(request)
                val imageBitmap = result.drawable?.let { drawable ->
                    // Convert drawable to bitmap
                    when (drawable) {
                        is android.graphics.drawable.BitmapDrawable -> drawable.bitmap
                        else -> {
                            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 512
                            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 512
                            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)
                            bitmap
                        }
                    }
                }
                
                // Save bitmap to a temporary file
                val imageUri = saveBitmapToCache(imageBitmap)
                
                // Share the image with actual image file
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
                        // Fallback to text-only if image failed
                        shareLinkOnly(imageUrl, message, category)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GreetingDetailActivity", "Error downloading image: ${e.message}")
                
                // Fallback to text sharing on error
                withContext(Dispatchers.Main) {
                    shareLinkOnly(imageUrl, message, category)
                }
            }
        }
    }
    
    private fun shareLinkOnly(imageUrl: String, message: String, category: String) {
        // Fallback implementation using just text
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, category)
            
            // If we have a message, include it
            val shareText = if (message.isNotEmpty()) {
                "$message\n\n$imageUrl"
            } else {
                imageUrl
            }
            
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(intent, "Send Greeting"))
    }
    
    private fun saveBitmapToCache(bitmap: android.graphics.Bitmap?, quality: Int = 90): android.net.Uri? { // Added quality parameter
        if (bitmap == null) {
            return null
        }
        
        try {
            // Create a temporary file in the cache directory
            val cacheDir = cacheDir
            val file = java.io.File(cacheDir, "greeting_card_${System.currentTimeMillis()}.jpg")
            
            // Save bitmap to file
            java.io.FileOutputStream(file).use { outputStream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outputStream) // Use provided quality
            }
            
            // Get content URI using FileProvider
            return androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )
        } catch (e: Exception) {
            android.util.Log.e("GreetingDetailActivity", "Error saving image: ${e.message}")
            return null
        }
    }    private fun sendGreetingInOutlook(imageUrl: String, message: String, category: String) {
        // This will open the Outlook app with the greeting and message
        downloadImageAndShareOutlook(imageUrl, message, category)
    }

    private fun downloadImageAndShareOutlook(imageUrl: String, message: String, category: String) {
        android.widget.Toast.makeText(this, "Preparing email for Outlook...", android.widget.Toast.LENGTH_SHORT).show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageLoader = ImageLoader(this@GreetingDetailActivity)
                val request = ImageRequest.Builder(this@GreetingDetailActivity)
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
                            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            val canvas = android.graphics.Canvas(bmp)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)
                            bmp
                        }
                    }
                }

                if (originalBitmap == null) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(this@GreetingDetailActivity, "Could not load image.", android.widget.Toast.LENGTH_SHORT).show()
                        shareLinkOnly(imageUrl, message, category)
                    }
                    return@launch
                }

                // 1. Prepare image for HTML embedding (very optimized)
                // val bitmapForHtmlBase64 = createOutlookOptimizedBitmap(originalBitmap, forBase64 = true) // No longer needed for direct URL embedding
                // val byteArrayOutputStreamHtml = ByteArrayOutputStream()
                // bitmapForHtmlBase64.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStreamHtml) 
                // val imageBytesHtml = byteArrayOutputStreamHtml.toByteArray()
                // val base64ImageHtml = Base64.encodeToString(imageBytesHtml, Base64.NO_WRAP)
                
                // Get user data for footer
                val userDataManager = UserDataManager.getInstance(this@GreetingDetailActivity)
                val userData = userDataManager.getUserData()
                val userName = userData?.name ?: "Your Name"
                val userDesignation = userData?.designation ?: "Your Designation"
                
                // Use the direct imageUrl for the HTML email content
                val htmlEmailContent = createRichHtmlEmail(message, imageUrl, category, userName, userDesignation)

                // 2. Prepare image for attachment (better quality, for fallback)
                val bitmapForAttachment = createOutlookOptimizedBitmap(originalBitmap, forBase64 = false)
                val imageUriForAttachment = saveBitmapToCache(bitmapForAttachment, 80)


                withContext(Dispatchers.Main) {
                    // Primary Attempt: Pure HTML with embedded base64
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
                        Log.d("GreetingDetailActivity", "Attempted Outlook with pure HTML/Base64 intent.")
                        // No immediate toast here, as we don't know if it rendered inline yet.
                    } catch (e: Exception) {
                        Log.e("GreetingDetailActivity", "Outlook pure HTML intent failed: ${e.message}. Falling back to attachment method.", e)
                        
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
                                // Optionally, still include HTML as secondary info
                                // putExtra(Intent.EXTRA_HTML_TEXT, htmlEmailContent) 
                            }
                            try {
                                startActivity(attachmentIntent)
                                android.widget.Toast.makeText(this@GreetingDetailActivity, "Image attached. Tap & hold to add to body.", android.widget.Toast.LENGTH_LONG).show()
                            } catch (e2: Exception) {
                                Log.e("GreetingDetailActivity", "Outlook attachment fallback intent failed: ${e2.message}", e2)
                                shareLinkOnly(imageUrl, message, category) // Ultimate fallback
                            }
                        } else {
                            Log.e("GreetingDetailActivity", "Image URI for attachment was null, falling back to link only.")
                            shareLinkOnly(imageUrl, message, category)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GreetingDetailActivity", "Error in downloadImageAndShareOutlook: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    shareLinkOnly(imageUrl, message, category)
                }
            }
        }
    }

    /**
     * Creates a bitmap specifically optimized.
     * @param forBase64 If true, optimizes for very small size (for base64 string), uses RGB_565.
     *                  If false, optimizes for attachment (better quality/size), uses ARGB_8888 or original.
     */
    private fun createOutlookOptimizedBitmap(bitmap: Bitmap, forBase64: Boolean): Bitmap {
        val maxWidth = if (forBase64) 500 else 600 // Drastically reduced for base64
        val maxHeight = if (forBase64) 600 else 800 // Drastically reduced for base64
        val targetConfig = if (forBase64) Bitmap.Config.RGB_565 else bitmap.config ?: Bitmap.Config.ARGB_8888

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
                if (targetConfig == Bitmap.Config.RGB_565) isDither = true 
            }
            canvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
            // Recycle the intermediate scaledBitmap if it's different from the original and the final one
            if (scaledBitmap != bitmap && scaledBitmap != finalBitmap && !scaledBitmap.isRecycled) {
                scaledBitmap.recycle()
            }
            return finalBitmap
        }
        
        // If scaledBitmap is the one we need (already correct config)
        // and it's different from the original, and the original is not needed anymore, consider recycling original.
        // However, be careful with recycling if 'bitmap' is passed from elsewhere and might be reused.
        // For safety, let's assume 'bitmap' might be used elsewhere or is the direct result from Coil.
        return scaledBitmap
    }

    /**
     * Creates a rich HTML email with an embedded image via URL.
     */
    private fun createRichHtmlEmail(message: String, imageUrl: String, category: String, userName: String, userDesignation: String): String {
        val sanitizedMessage = message.replace("\n", "<br />")
        // Using a table for layout can sometimes be more robust in older/quirky email clients
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
                <title>$category</title>
                <style>
                    body { font-family: Arial, sans-serif; font-size: 16px; margin: 0; padding: 0; background-color: #f8f8f8; }
                    .email-container { width: 100%; max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; }
                    .message-text { margin-bottom: 50px; line-height: 1.6; color: #333333; } /* Increased margin-bottom */
                    .image-container { text-align: center; margin-bottom: 30px; } /* Increased margin-bottom */
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
                        <img src="$imageUrl" alt="$category Greeting" style="max-width: 50%; width: 50%; height: auto; border: 0; display: block; margin: 0 auto;" /> 
                        <br /> <br />
                    </div>
                    
                    <div class="footer-text">
                        Best Regards,<br />
                       $userName<br />
                       $userDesignation
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}

