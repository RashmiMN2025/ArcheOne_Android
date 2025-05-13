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
    private var cardScreenshot: Bitmap? = null    // This will be populated from API data
    private val categoryMessages = mutableMapOf<String, String>()

    init {
        loadGreetings()
    }

    private fun loadGreetings() {
        val greetings = userDataManager.getGreetingsData()
        val greetingCategories = userDataManager.getGreetingCategoriesData()
          if (greetings != null) {
            model = model.copy(categories = greetings)
            Log.d("GreetingsController", "Loaded ${greetings.size} greeting categories")
            
            // Load category messages from the API data
            if (greetingCategories != null) {
                val messages = mutableMapOf<String, String>()
                greetingCategories.forEach { category ->
                    messages[category.name] = category.message
                }
                model = model.copy(categoryMessages = messages)
                Log.d("GreetingsController", "Loaded ${messages.size} greeting category messages")
            } else {
                Log.e("GreetingsController", "No greeting category messages available")
            }
        } else {
            Log.e("GreetingsController", "No greetings data available")
        }
    }
    
    // Get message for category from API data or fallback to empty
    private fun getMessageForCategory(category: String): String {
        return model.categoryMessages[category] ?: ""
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
    }    fun onCategorySelected(category: String) {
        // When a category is selected, also select the first greeting in that category
        val greetingsInCategory = model.categories[category]
        val firstGreetingInCategory = greetingsInCategory?.firstOrNull()
        
        // Log what's happening with more verbose details
        Log.d("GreetingsController", "onCategorySelected called with category: $category")
        Log.d("GreetingsController", "Selected category: $category, found ${greetingsInCategory?.size ?: 0} greetings")
        Log.d("GreetingsController", "First greeting URL: $firstGreetingInCategory")
        
        // Clear any existing card screenshot when changing category
        cardScreenshot = null
        
        // Get message for this category from API data
        val categoryMessage = getMessageForCategory(category)
        Log.d("GreetingsController", "Using message from API data: $categoryMessage")
        
        // Update the model with category-specific message from API
        model = model.copy(
            selectedCategory = category,
            selectedGreeting = firstGreetingInCategory,
            message = categoryMessage
        )
    }    fun onGreetingSelected(greetingUrl: String) {
        Log.d("GreetingsController", "onGreetingSelected method called with URL: $greetingUrl")
        model = model.copy(selectedGreeting = greetingUrl)
        Log.d("GreetingsController", "Model updated. New selectedGreeting: ${model.selectedGreeting}")
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
        val currentMessage = model.message // Capture current message
        val currentSelectedGreetingUrl = model.selectedGreeting // Capture current greeting URL

        val outlookPackages = arrayOf(
            "com.microsoft.office.outlook",
            "com.microsoft.outlook"
        )
        
        var resolvedOutlookPackage: String? = null
        for (pkgName in outlookPackages) { // Renamed loop variable
            try {
                // Check if the package is installed and has a launch intent
                if (context.packageManager.getLaunchIntentForPackage(pkgName) != null) {
                    resolvedOutlookPackage = pkgName
                    break 
                }
            } catch (e: Exception) {
                // Log error if checking a package fails, but continue to check others
                Log.e("GreetingsController", "Error checking package $pkgName: ${e.message}")
            }
        }
        
        // If no Outlook package was found after checking all candidates
        if (resolvedOutlookPackage == null) {
            android.widget.Toast.makeText(
                context, 
                "Microsoft Outlook is not installed. Please install it to use this feature.", 
                android.widget.Toast.LENGTH_LONG
            ).show()
            Log.e("GreetingsController", "No installed Outlook package found from candidates: ${outlookPackages.joinToString()}")
            return // Exit if Outlook is not available
        }
        
        // Attempt to use card screenshot if available
        val imageUri = saveBitmapForSharing(cardScreenshot)
        
        if (imageUri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
                putExtra(Intent.EXTRA_TEXT, currentMessage)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage(resolvedOutlookPackage) // Target the resolved Outlook package
            }
            try {
                context.startActivity(intent)
                return // Successfully launched Outlook with image, so exit
            } catch (e: Exception) {
                Log.e("GreetingsController", "Error opening Outlook with image: ${e.message}", e)
                // If sending with image fails, fall through to try other methods
            }
        } else {
            Log.d("GreetingsController", "No card screenshot available for Outlook, attempting alternative methods.")
        }
        
        // If screenshot is not available or sending it failed, try downloading the image URL
        if (!currentSelectedGreetingUrl.isNullOrEmpty()) {
            android.widget.Toast.makeText(context, "Preparing greeting for Outlook...", android.widget.Toast.LENGTH_SHORT).show()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val imageBitmap = coil.ImageLoader(context).execute(
                        coil.request.ImageRequest.Builder(context)
                            .data(currentSelectedGreetingUrl)
                            .allowHardware(false) // Important for accessing pixels from the drawable
                            .build()
                    ).drawable?.let { drawable ->
                        // Convert drawable to bitmap
                        when (drawable) {
                            is android.graphics.drawable.BitmapDrawable -> drawable.bitmap
                            else -> { // Handle other drawable types
                                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 512 // Default if 0
                                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 512 // Default if 0
                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                bitmap
                            }
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        if (imageBitmap != null) {
                            val downloadedImageUri = saveBitmapForSharing(imageBitmap)
                            if (downloadedImageUri != null) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, downloadedImageUri)
                                    putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
                                    putExtra(Intent.EXTRA_TEXT, currentMessage)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    setPackage(resolvedOutlookPackage) // Target Outlook
                                }
                                try {
                                    context.startActivity(intent)
                                    return@withContext // Successfully launched with downloaded image
                                } catch (e: Exception) {
                                    Log.e("GreetingsController", "Error opening Outlook with downloaded image: ${e.message}", e)
                                    // Fall through to text-only if this fails
                                }
                            }
                        }
                        // Fallback to text-only if image download or sending failed
                        sendTextOnlyToOutlook(resolvedOutlookPackage, greetingTitle, currentMessage)
                    }
                } catch (e: Exception) {
                    Log.e("GreetingsController", "Error downloading image for Outlook: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        // Fallback to text-only on download error
                        sendTextOnlyToOutlook(resolvedOutlookPackage, greetingTitle, currentMessage)
                    }
                }
            }
        } else {
            // No image URL available, send text-only
            Log.d("GreetingsController", "No image URL for Outlook, sending text-only.")
            sendTextOnlyToOutlook(resolvedOutlookPackage, greetingTitle, currentMessage)
        }
    }
    
    // Helper method to send a text-only email via Outlook
    private fun sendTextOnlyToOutlook(packageName: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822" // Standard MIME type for email
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            setPackage(packageName) // Target the specific Outlook package
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                context, 
                "Could not open Outlook. Please ensure it is installed and properly configured.", 
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