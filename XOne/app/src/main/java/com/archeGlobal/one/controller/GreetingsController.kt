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
            model = model.copy(selectedCategory = null, selectedGreeting = null)
        } else {
            navigator.navigateToHome()
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
        
        // Update the model
        model = model.copy(
            selectedCategory = category,
            selectedGreeting = firstGreetingInCategory
        )
    }

    fun onGreetingSelected(url: String) {
        // Log selection
        Log.d("GreetingsController", "Selected greeting: $url")
        
        // Clear any existing card screenshot when changing greeting
        cardScreenshot = null
        
        // Update model with selected greeting
        model = model.copy(selectedGreeting = url)
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
        val imageUri = saveBitmapForSharing(cardScreenshot)
        val greetingTitle = model.selectedCategory ?: "Greeting"
        
        if (imageUri != null) {
            // Send both card screenshot and text to Outlook
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
                putExtra(Intent.EXTRA_TEXT, model.message)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Add Outlook package name if available
                val outlookPackages = arrayOf(
                    "com.microsoft.office.outlook",
                    "com.microsoft.outlook"
                )
                for (pkg in outlookPackages) {
                    if (context.packageManager.getLaunchIntentForPackage(pkg) != null) {
                        setPackage(pkg)
                        break
                    }
                }
            }
            context.startActivity(intent)
        } else {
            // Fallback to text-only sharing if screenshot fails
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_SUBJECT, greetingTitle)
                putExtra(Intent.EXTRA_TEXT, model.message)
                // Add Outlook package name if available
                val outlookPackages = arrayOf(
                    "com.microsoft.office.outlook",
                    "com.microsoft.outlook"
                )
                for (pkg in outlookPackages) {
                    if (context.packageManager.getLaunchIntentForPackage(pkg) != null) {
                        setPackage(pkg)
                        break
                    }
                }
            }
            context.startActivity(intent)
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