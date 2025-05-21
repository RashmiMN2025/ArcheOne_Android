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
                        name = intent.getStringExtra("category") ?: "Global Celebration",
                        files = allGreetings,
                        message = editableMessage
                    ),
                    onBackPressed = { finish() },
                    onSendGreeting = { imageUrl, message -> sendGreeting(imageUrl, message, "Global Celebration") },
                    onSendInOutlook = { imageUrl, message -> sendGreetingInOutlook(imageUrl, message, "Global Celebration") }
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
                
                // Save bitmap to a temporary file
                val imageUri = saveBitmapToCache(imageBitmap)
                
                withContext(Dispatchers.Main) {
                    if (imageUri != null) {
                        // Create intent for Outlook with image in body
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822"  // Use email MIME type
                            putExtra(Intent.EXTRA_SUBJECT, category)
                            
                            // Add message as text
                            if (message.isNotEmpty()) {
                                putExtra(Intent.EXTRA_TEXT, message)
                            }
                            
                            // Add image to be embedded in body, not as attachment
                            putExtra(Intent.EXTRA_STREAM, imageUri)
                            
                            // Set flags to grant URI permissions
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            
                            // Target Outlook app specifically
                            setPackage("com.microsoft.office.outlook")
                        }
                        
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            android.util.Log.e("GlobalCelebrationDetailActivity", "Error opening Outlook: ${e.message}", e)
                            // Fallback to normal share if Outlook is not installed or has issues
                            sendGreeting(imageUrl, message, category)
                        }
                    } else {
                        // Fallback to text-only if image processing failed
                        shareLinkOnly(imageUrl, message, category)
                    }
                }            } catch (e: Exception) {
                android.util.Log.e("GlobalCelebrationDetailActivity", "Error downloading image for Outlook: ", e)
                withContext(Dispatchers.Main) {
                    shareLinkOnly(imageUrl, message, category)
                }
            }
        }
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
