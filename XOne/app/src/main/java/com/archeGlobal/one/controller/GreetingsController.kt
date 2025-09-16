package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.archeGlobal.one.model.GreetingModel
import com.archeGlobal.one.model.GreetingSubcategory
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.utils.UserDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var cardScreenshot: Bitmap? = null // This will be populated from API data
    private val categoryMessages = mutableMapOf<String, String>()

    init {
        loadGreetings()
    } private fun loadGreetings() {
        val greetings = userDataManager.getGreetingsData()
        val greetingCategories = userDataManager.getGreetingCategoriesData()
        var subcategories: List<GreetingSubcategory> = emptyList()

        // Find subcategories for Global Celebration from API data
        if (greetingCategories != null) {
            // Two approaches to identify subcategories:
            // 1. Check if files contain "/Global Celebration/" path
            // 2. Check if message field is "Global Celebration" (indicates it's a subcategory)
            subcategories = greetingCategories.filter { cat ->
                cat.files.any { file -> file.contains("/Global Celebration/") } ||
                    cat.message == "Global Celebration" ||
                    cat.name != "Global Celebration" && (cat.files.any { file -> file.contains("global") || file.contains("celebration") })
            }.map { cat ->
                GreetingSubcategory(
                    id = cat.id,
                    name = cat.name,
                    files = cat.files,
                    message = cat.message
                )
            }
            Log.d("GreetingsController", "Found ${subcategories.size} subcategories for Global Celebration")
        }

        // Always add Global Celebration to categories regardless if we have subcategories or not
        // This ensures it appears in the UI
        val categoriesWithGlobal = if (greetings != null) {
            val mutable = greetings.toMutableMap()
            // Always add Global Celebration category (check both singular and plural forms)
            if (!mutable.containsKey("Global Celebrations") && !mutable.containsKey("Global Celebration")) {
                // Use first image from subcategories if available, otherwise empty list
                val firstImage = subcategories.firstOrNull()?.files?.firstOrNull()?.let { listOf(it) } ?: emptyList()
                mutable["Global Celebrations"] = firstImage
                Log.d("GreetingsController", "Added Global Celebrations category with ${firstImage.size} images")
            }
            mutable.toMap()
        } else {
            // If no categories at all, at least add Global Celebrations
            mapOf("Global Celebrations" to emptyList<String>())
        }

        model = model.copy(categories = categoriesWithGlobal, subcategories = subcategories)
        Log.d("GreetingsController", "Loaded ${categoriesWithGlobal.size} greeting categories with Global Celebration")

        if (greetingCategories != null) {
            val messages = mutableMapOf<String, String>()
            greetingCategories.forEach { category ->
                messages[category.name] = category.message
            }
            // Add special message for Global Celebrations if not present
            if (!messages.containsKey("Global Celebrations") && !messages.containsKey("Global Celebration")) {
                messages["Global Celebrations"] = "Global celebration greetings for special occasions around the world."
            }
            model = model.copy(categoryMessages = messages)
            Log.d("GreetingsController", "Loaded ${messages.size} greeting category messages")
        } else {
            Log.e("GreetingsController", "No greeting category messages available")
        }
    }

    // Get message for category from API data or fallback to empty
    private fun getMessageForCategory(category: String): String {
        return model.categoryMessages[category] ?: ""
    }

    fun onCategorySelected(category: String, navigateToDetail: Boolean = false) {
        try {
            // For Global Celebration, just select the category and show subcategories
            if ((category == "Global Celebrations" || category == "Global Celebration") && model.subcategories.isNotEmpty()) {
                Log.d("GreetingsController", "Selected parent category: $category with ${model.subcategories.size} subcategories")
                model = model.copy(
                    selectedCategory = category,
                    selectedSubcategory = null,
                    selectedGreeting = null,
                    message = getMessageForCategory(category)
                )
                return
            }
            // For other categories, select the first greeting
            val greetingsInCategory = model.categories[category]
            Log.d("GreetingsController", "Complete categories map: ${model.categories}")
            if (greetingsInCategory.isNullOrEmpty()) {
                Log.e("GreetingsController", "Error: No greetings found for category: $category")
                return
            }
            val firstGreetingInCategory = greetingsInCategory.firstOrNull()
            Log.d("GreetingsController", "onCategorySelected called with category: $category")
            Log.d("GreetingsController", "Selected category: $category, found ${greetingsInCategory.size} greetings")
            Log.d("GreetingsController", "First greeting URL: $firstGreetingInCategory")
            cardScreenshot = null
            val categoryMessage = getMessageForCategory(category)
            Log.d("GreetingsController", "Using message from API data: $categoryMessage")

            // FIX: Navigate before updating the model to avoid intermediate page
            if (navigateToDetail && firstGreetingInCategory != null) {
                navigator.navigateToGreetingDetail(
                    firstGreetingInCategory,
                    greetingsInCategory,
                    categoryMessage,
                    category
                )
                return // Prevent model update and recomposition
            }

            model = model.copy(
                selectedCategory = category,
                selectedSubcategory = null,
                selectedGreeting = firstGreetingInCategory,
                message = categoryMessage
            )
        } catch (e: Exception) {
            Log.e("GreetingsController", "Error in onCategorySelected: ${e.message}", e)
        }
    }

    fun onGreetingSelected(greetingUrl: String) {
        Log.d("GreetingsController", "onGreetingSelected method called with URL: $greetingUrl")

        // Clear any existing screenshot when changing greeting
        cardScreenshot = null

        // Create and assign a new model to ensure recomposition
        val updatedModel = model.copy(selectedGreeting = greetingUrl)
        model = updatedModel

        // Verify the update took effect
        Log.d("GreetingsController", "Model updated. New selectedGreeting: ${model.selectedGreeting}")

        // Navigate to greeting detail screen
        navigateToGreetingDetail(greetingUrl)
    }

    private fun navigateToGreetingDetail(greetingUrl: String) {
        try {
            val currentCategory = model.selectedCategory ?: "Greeting"
            val currentMessage = model.message
            val allGreetings = model.categories[currentCategory] ?: listOf(greetingUrl)

            Log.d("GreetingsController", "Navigating to greeting detail: $currentCategory, URL: $greetingUrl")
            navigator.navigateToGreetingDetail(greetingUrl, allGreetings, currentMessage, currentCategory)
        } catch (e: Exception) {
            Log.e("GreetingsController", "Error navigating to greeting detail: ${e.message}", e)
        }
    }

    fun updateMessage(message: String) {
        model = model.copy(message = message)
    }

    fun updateSearchQuery(query: String) {
        model = model.copy(searchQuery = query)
    }

    fun getFilteredCategories(): List<String> {
        val query = model.searchQuery.lowercase()
        val categories = model.categories.keys.filter { category ->
            category.lowercase().contains(query)
        }
        Log.d("GreetingsController", "Filtered categories: $categories from ${model.categories.keys}")
        return categories
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
    } // Removed controller-level sendInOutlook implementation
    // This functionality should only be handled by GreetingDetailActivity
    // to avoid conflicting implementations

    // Note: sendInOutlook and sendTextOnlyToOutlook have been removed
    // to ensure that all Outlook-related actions are handled
    // consistently by the GreetingDetailActivity instead

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

    // Get subcategories for a category (for now, only for Global Celebration)
    fun getSubcategoriesForCategory(category: String): List<GreetingSubcategory> {
        return if (category == "Global Celebrations" || category == "Global Celebration") model.subcategories else emptyList()
    }

    // Select a subcategory (now expects a GreetingSubcategory)
    fun onSubcategorySelected(subcategory: GreetingSubcategory, navigateToDetail: Boolean = false) {
        val greetingsInSubcategory = subcategory.files
        if (greetingsInSubcategory.isEmpty()) {
            Log.e("GreetingsController", "No greetings found for subcategory: ${subcategory.name}")
            return
        }
        val firstGreeting = greetingsInSubcategory.firstOrNull()
        val updatedModel = model.copy(
            selectedSubcategory = subcategory,
            selectedGreeting = firstGreeting,
            message = subcategory.message
        )
        model = updatedModel
        if (navigateToDetail && firstGreeting != null) {
            navigateToGreetingDetail(firstGreeting)
        }
    }

    // Get greetings for a subcategory
    fun getGreetingsForSubcategory(subcategory: GreetingSubcategory?): List<String> {
        return subcategory?.files ?: emptyList()
    }

    // Clear selected subcategory
    fun clearSelectedSubcategory() {
        model = model.copy(selectedSubcategory = null)
    }

    fun onBackPressed(): Boolean {
        return when {
            model.selectedSubcategory != null -> {
                clearSelectedSubcategory()
                true
            }
            model.selectedCategory != null -> {
                model = model.copy(selectedCategory = null)
                true
            }
            else -> false
        }
    }

    fun onCategoryClick(category: String) {
        if (category == "Global Celebrations" || category == "Global Celebration") {
            // Navigate to the Global Celebration screen instead of showing subcategories inline
            navigator.navigateToGlobalCelebration()
            Log.d("GreetingsController", "Global Celebration clicked, navigating to GlobalCelebrationScreen")
        } else if (category == "Regional Festivals") {
            // Navigate to the Regional Festivals screen
            navigator.navigateToRegionalFestivals()
            Log.d("GreetingsController", "Regional Festivals clicked, navigating to RegionalFestivalsScreen")
        } else {
            // For other categories, call onCategorySelected with navigateToDetail=true
            // to navigate directly to the greeting detail screen
            Log.d("GreetingsController", "Regular category clicked, navigating to detail: $category")
            onCategorySelected(category, navigateToDetail = true)
        }
    }

    fun getGreetingsForCategory(category: String): List<String> {
        return model.categories[category] ?: emptyList()
    }

    fun getCategoryThumbnail(category: String): String {
        val greetingCategories = userDataManager.getGreetingCategoriesData()

        // Special handling for categories with dedicated thumbnails - use image from API
        if (category == "Global Celebrations" || category == "Global Celebration") {
            val globalCelebration = greetingCategories?.find { it.name == "Global Celebrations" || it.name == "Global Celebration" }
            // Use the files from the main Global Celebration category as provided by API
            return globalCelebration?.files?.firstOrNull() ?: ""
        } else if (category == "Regional Festivals") {
            val regionalFestivals = greetingCategories?.find { it.name == "Regional Festivals" }
            // Use the files from the main Regional Festivals category as provided by API
            return regionalFestivals?.files?.firstOrNull() ?: ""
        }

        // For other categories, check if they have a dedicated thumbnail in API data first
        val apiCategory = greetingCategories?.find { it.name == category }
        if (apiCategory != null && apiCategory.files.isNotEmpty()) {
            return apiCategory.files.firstOrNull() ?: ""
        }

        // Fallback to first greeting image for the category
        return model.categories[category]?.firstOrNull() ?: ""
    }
}
