package com.archeGlobal.one

import android.content.Intent
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
                    category = category,
                    onMessageChanged = { newMessage -> editableMessage = newMessage },
                    onGreetingSelected = { newGreetingUrl -> selectedGreetingUrl = newGreetingUrl },
                    onBackPressed = { finish() },
                    onSendGreeting = { sendGreeting(selectedGreetingUrl, editableMessage, category) },
                    onSendInOutlook = { sendGreetingInOutlook(selectedGreetingUrl, editableMessage, category) }
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
    
    private fun saveBitmapToCache(bitmap: android.graphics.Bitmap?): android.net.Uri? {
        if (bitmap == null) {
            return null
        }
        
        try {
            // Create a temporary file in the cache directory
            val cacheDir = cacheDir
            val file = java.io.File(cacheDir, "greeting_card_${System.currentTimeMillis()}.jpg")
            
            // Save bitmap to file
            java.io.FileOutputStream(file).use { outputStream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
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
        // Show loading toast
        android.widget.Toast.makeText(this, "Preparing greeting for Outlook...", android.widget.Toast.LENGTH_SHORT).show()
        
        // Implementation for sending via Outlook
        val outlookPackages = arrayOf(
            "com.microsoft.office.outlook",
            "com.microsoft.outlook"
        )
        
        var resolvedOutlookPackage: String? = null
        for (pkgName in outlookPackages) {
            try {
                if (packageManager.getLaunchIntentForPackage(pkgName) != null) {
                    resolvedOutlookPackage = pkgName
                    break
                }
            } catch (e: Exception) {
                // Log error but continue checking other packages
                android.util.Log.e("GreetingDetailActivity", "Error checking package $pkgName: ${e.message}")
            }
        }
        
        if (resolvedOutlookPackage == null) {
            // Outlook not found - show error message
            android.widget.Toast.makeText(
                this,
                "Microsoft Outlook is not installed. Please install it to use this feature.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        
        // Download and share the image via Outlook
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
                
                // Share via Outlook with actual image file
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
                            setPackage(resolvedOutlookPackage)
                        }
                        
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(
                                this@GreetingDetailActivity,
                                "Could not open Outlook. Please ensure it is installed and properly configured.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            android.util.Log.e("GreetingDetailActivity", "Error opening Outlook with image: ${e.message}")
                            // Try fallback with text-only
                            sendTextOnlyToOutlook(resolvedOutlookPackage, category, message)
                        }
                    } else {
                        // Fallback to text-only if image failed
                        sendTextOnlyToOutlook(resolvedOutlookPackage, category, message)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("GreetingDetailActivity", "Error downloading image for Outlook: ${e.message}")
                
                // Fallback to text sharing on error
                withContext(Dispatchers.Main) {
                    sendTextOnlyToOutlook(resolvedOutlookPackage, category, message)
                }
            }
        }
    }
    
    private fun sendTextOnlyToOutlook(packageName: String, subject: String, body: String) {
        // Create intent specifically for Outlook text-only
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822" // email MIME type
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            setPackage(packageName)
        }
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                this,
                "Could not open Outlook. Please ensure it is installed and properly configured.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            android.util.Log.e("GreetingDetailActivity", "Error opening Outlook: ${e.message}")
        }
    }
}
