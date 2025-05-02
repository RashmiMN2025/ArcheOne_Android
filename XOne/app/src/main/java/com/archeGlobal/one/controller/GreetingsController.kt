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
        val firstGreetingInCategory = model.categories[category]?.firstOrNull()
        model = model.copy(
            selectedCategory = category,
            selectedGreeting = firstGreetingInCategory
        )
        Log.d("GreetingsController", "Selected category: $category with ${model.categories[category]?.size ?: 0} greetings")
        Log.d("GreetingsController", "Auto-selected first greeting: $firstGreetingInCategory")
    }

    fun onGreetingSelected(url: String) {
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
}