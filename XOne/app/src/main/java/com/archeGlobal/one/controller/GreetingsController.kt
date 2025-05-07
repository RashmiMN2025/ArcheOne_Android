package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.GreetingModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.utils.UserDataManager
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GreetingsController(
    private val context: Context,
    private val navigator: Navigator
) {
    private val userDataManager = UserDataManager.getInstance(context)
    
    var model by mutableStateOf(GreetingModel())
        private set
    
    // Captured card screenshot bitmap
    private var cardScreenshot: Bitmap? = null

    init {
        loadGreetings()
    }

    private fun loadGreetings() {
        val greetings = userDataManager.getGreetingsData()
        if (greetings != null) {
            model = model.copy(categories = greetings)
            Log.d("GreetingsController", "Loaded ${greetings.size} greeting categories")
        } else {
            Log.e("GreetingsController", "No greetings data available")
        }
    }

    fun onBackPressed() {
        if (model.selectedCategory != null) {
            // If we're in a category view, go back to category list
            Log.d("GreetingsController", "Back pressed while in a category, going to category list")
            model = model.copy(selectedCategory = null, selectedGreeting = null)
        } else {
            // If we're already at the main greetings page, navigate to home
            Log.d("GreetingsController", "Back pressed on main greetings page, navigating to home")
            try {
                navigator.navigateToHome()
            } catch (e: Exception) {
                Log.e("GreetingsController", "Failed to navigate to home: ${e.message}")
                // Fallback approach in case the navigation fails
                try {
                    val intent = navigator.getHomeIntent()
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    Log.e("GreetingsController", "Both navigation approaches failed: ${e2.message}")
                }
            }
        }
    }

    fun onCategorySelected(category: String) {
        // When a category is selected, also select the first greeting in that category
        val greetingsInCategory = model.categories[category]
        val firstGreetingInCategory = greetingsInCategory?.firstOrNull()
        
        // Log what's happening 
        Log.d("GreetingsController", "Selected category: $category, found ${greetingsInCategory?.size ?: 0} greetings")
        Log.d("GreetingsController", "First greeting URL: $firstGreetingInCategory")
        
        // Clear any existing card screenshot when changing category
        cardScreenshot = null
        
        // Update the model - clear the message when changing category
        model = model.copy(
            selectedCategory = category,
            selectedGreeting = firstGreetingInCategory,
            message = "" // Reset the message when changing category
        )
    }

    fun onGreetingSelected(url: String) {
        // Log selection
        Log.d("GreetingsController", "Selected greeting: $url")
        
        // Clear any existing card screenshot when changing greeting
        cardScreenshot = null
        
        // Update model with selected greeting and reset message
        model = model.copy(
            selectedGreeting = url,
            message = "" // Reset the message when changing greeting
        )
    }

    fun updateMessage(message: String) {
        model = model.copy(message = message)
    }

    fun updateSearchQuery(query: String) {
        model = model.copy(searchQuery = query)
    }

    fun getFilteredCategories(): List<String> {
        val query = model.searchQuery.lowercase()
        return model.categories.keys.filter { category ->
            category.lowercase().contains(query)
        }
    }

    // Method to set the screenshot of the card
    fun setCardScreenshot(bitmap: Bitmap) {
        Log.d("GreetingsController", "Card screenshot captured: ${bitmap.width}x${bitmap.height}")
        cardScreenshot = bitmap
    }

    // Helper method to save bitmap to a file and get sharing URI
    private fun saveBitmapForSharing(bitmap: Bitmap?): Uri? {
        if (bitmap == null) {
            Log.e("GreetingsController", "No card screenshot available")
            return null
        }
        
        try {
            // Create a temporary file in the cache directory
            val cacheDir = context.cacheDir
            val file = File(cacheDir, "greeting_card_${System.currentTimeMillis()}.jpg")
            
            // Save bitmap to file
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            
            // Get content URI using FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("GreetingsController", "Error saving card screenshot: ${e.message}", e)
            return null
        }
    }

    fun sendGreeting() {
        val imageUri = saveBitmapForSharing(cardScreenshot)
        val greetingTitle = model.selectedCategory ?: "Greeting"
        
        if (imageUri != null) {
            // Send both card screenshot and text
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
                putExtra(Intent.EXTRA_TEXT, model.message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Send Greeting"))
        } else {
            // Fallback to text-only sharing if screenshot fails
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
                putExtra(Intent.EXTRA_TEXT, model.message)
            }
            context.startActivity(Intent.createChooser(intent, "Send Greeting"))
        }
    }

    fun sendInOutlook() {
        val greetingTitle = model.selectedCategory ?: "Greeting"
        val imageUrl = model.selectedGreeting
        
        // Define Outlook package names - Microsoft Outlook has different package names on different devices
        val outlookPackages = arrayOf(
            "com.microsoft.office.outlook",
            "com.microsoft.outlook"
        )
        
        // Find the installed Outlook package if available
        var outlookPackage: String? = null
        for (pkg in outlookPackages) {
            try {
                if (context.packageManager.getLaunchIntentForPackage(pkg) != null) {
                    outlookPackage = pkg
                    break
                }
            } catch (e: Exception) {
                // Log the error but continue checking other packages
                Log.e("GreetingsController", "Error checking package $pkg: ${e.message}")
            }
        }
        
        // If Outlook is not installed, show a message to the user
        if (outlookPackage == null) {
            android.widget.Toast.makeText(
                context, 
                "Microsoft Outlook is not installed. Please install it from the Play Store.", 
                android.widget.Toast.LENGTH_LONG
            ).show()
            Log.e("GreetingsController", "No Outlook package found among: ${outlookPackages.joinToString()}")
            return
        }
        
        // First try with the screenshot if available
        val imageUri = saveBitmapForSharing(cardScreenshot)
        
        if (imageUri != null) {
            // Send both card screenshot and text directly to Outlook (no chooser)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
                putExtra(Intent.EXTRA_TEXT, model.message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Set Outlook as the only target
                setPackage(outlookPackage)
            }
            
            try {
                context.startActivity(intent)
                return // Exit early as we've handled the sharing successfully
            } catch (e: Exception) {
                Log.e("GreetingsController", "Error opening Outlook with image: ${e.message}", e)
                // Continue to fallback options below
            }
        } else {
            // No screenshot available, log the issue
            Log.d("GreetingsController", "No screenshot available, falling back to alternative methods")
        }
        
        // If we got here, either the screenshot was null or sending with the screenshot failed
        // If we have a URL for the greeting, try to download it first
        if (!imageUrl.isNullOrEmpty()) {
            android.widget.Toast.makeText(
                context, 
                "Preparing greeting for Outlook...", 
                android.widget.Toast.LENGTH_SHORT
            ).show()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Try to download the image
                    val imageBitmap = coil.ImageLoader(context).execute(
                        coil.request.ImageRequest.Builder(context)
                            .data(imageUrl)
                            .allowHardware(false) // Needed to access pixels
                            .build()
                    ).drawable?.let { drawable ->
                        // Convert drawable to bitmap
                        when (drawable) {
                            is android.graphics.drawable.BitmapDrawable -> drawable.bitmap
                            else -> {
                                // Create a bitmap from any drawable
                                val bitmap = Bitmap.createBitmap(
                                    drawable.intrinsicWidth,
                                    drawable.intrinsicHeight,
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = android.graphics.Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                bitmap
                            }
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (imageBitmap != null) {
                            // Save the downloaded bitmap and get its URI
                            val downloadedImageUri = saveBitmapForSharing(imageBitmap)
                            
                            if (downloadedImageUri != null) {
                                // Now try sending via Outlook with the downloaded image
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, downloadedImageUri)
                                    putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
                                    putExtra(Intent.EXTRA_TEXT, model.message)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    setPackage(outlookPackage)
                                }
                                
                                try {
                                    context.startActivity(intent)
                                    return@withContext
                                } catch (e: Exception) {
                                    Log.e("GreetingsController", "Error opening Outlook with downloaded image: ${e.message}", e)
                                    // Fall back to text-only below
                                }
                            }
                        }
                        
                        // If we get here, use text-only fallback
                        sendTextOnlyToOutlook(outlookPackage, greetingTitle)
                    }
                } catch (e: Exception) {
                    Log.e("GreetingsController", "Error downloading image: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        // Fall back to text-only
                        sendTextOnlyToOutlook(outlookPackage, greetingTitle)
                    }
                }
            }
        } else {
            // No image URL, just send text-only
            sendTextOnlyToOutlook(outlookPackage, greetingTitle)
        }
    }
    
    // Helper method to send text-only email to Outlook
    private fun sendTextOnlyToOutlook(outlookPackage: String, greetingTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
            putExtra(Intent.EXTRA_TEXT, model.message)
            setPackage(outlookPackage)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context, 
                "Could not open Outlook. Please make sure it's properly installed.", 
                android.widget.Toast.LENGTH_LONG
            ).show()
            Log.e("GreetingsController", "Error opening Outlook for text-only email: ${e.message}", e)
        }
    }

    // Method to download an image from URL and share it directly
    fun downloadAndShareImage(imageUrl: String) {
        if (imageUrl.isEmpty()) {
            Log.e("GreetingsController", "Empty image URL")
            return
        }
        
        Log.d("GreetingsController", "Downloading image directly from: $imageUrl")
        
        // Use Coil to download the image in the background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create a target bitmap to receive the downloaded image
                val imageBitmap = coil.ImageLoader(context).execute(
                    coil.request.ImageRequest.Builder(context)
                        .data(imageUrl)
                        .allowHardware(false) // Needed to access pixels
                        .build()
                ).drawable?.let { drawable ->
                    // Convert drawable to bitmap
                    when (drawable) {
                        is android.graphics.drawable.BitmapDrawable -> drawable.bitmap
                        else -> {
                            // Create a bitmap from any drawable
                            val bitmap = Bitmap.createBitmap(
                                drawable.intrinsicWidth,
                                drawable.intrinsicHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = android.graphics.Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)
                            bitmap
                        }
                    }
                }
                
                // If image was successfully downloaded and converted to bitmap
                if (imageBitmap != null) {
                    // Set as card screenshot
                    cardScreenshot = imageBitmap
                    
                    // Switch to main thread to start share intent
                    withContext(Dispatchers.Main) {
                        sendGreeting()
                    }
                } else {
                    // Log error and use fallback
                    Log.e("GreetingsController", "Failed to download image, using fallback")
                    withContext(Dispatchers.Main) {
                        sendGreeting() // This will use text-only fallback
                    }
                }
            } catch (e: Exception) {
                Log.e("GreetingsController", "Error downloading image: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    sendGreeting() // This will use text-only fallback
                }
            }
        }
    }
}