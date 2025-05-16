package com.archeGlobal.one.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.model.GlobalCelebrationModel
import com.archeGlobal.one.model.GreetingSubcategory
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.utils.UserDataManager
import android.content.Intent
import com.archeGlobal.one.GlobalCelebrationDetailActivity

class GlobalCelebrationController(
    private val context: Context,
    private val navigator: Navigator,
    private val parentController: GreetingsController? = null
) {
    private val userDataManager = UserDataManager.getInstance(context)
    
    var model by mutableStateOf(GlobalCelebrationModel())
        private set
        
    init {
        loadSubcategories()
    }
    
    private fun loadSubcategories() {
        val greetingCategories = userDataManager.getGreetingCategoriesData()
        if (greetingCategories == null) {
            Log.e("GlobalCelebrationController", "No greeting categories data available (null)")
            model = model.copy(subcategories = emptyList())
            return
        }
        if (greetingCategories.isEmpty()) {
            Log.e("GlobalCelebrationController", "Greeting categories data is empty")
            model = model.copy(subcategories = emptyList())
            return
        }
        // Find the Global Celebration category and use its subfolder as subcategories
        val globalCelebration = greetingCategories.find { it.name == "Global Celebration" }
        val subcategories = globalCelebration?.subfolder ?: emptyList()
        Log.d("GlobalCelebrationController", "Loaded ${subcategories.size} subcategories for Global Celebration (from subfolder)")
        model = model.copy(subcategories = subcategories)
    }
    
    fun updateSearchQuery(query: String) {
        model = model.copy(searchQuery = query)
    }
    
    fun getFilteredSubcategories(): List<GreetingSubcategory> {
        val query = model.searchQuery.lowercase()
        if (query.isEmpty()) return model.subcategories
        
        return model.subcategories.filter { subcategory ->
            subcategory.name.lowercase().contains(query)
        }
    }
    
    fun onSubcategorySelected(subcategory: GreetingSubcategory) {
        // Pass data like greetings: first image, all images, message, category
        val firstImage = subcategory.files.firstOrNull() ?: ""
        val allImages = ArrayList<String>(subcategory.files)
        val message = subcategory.message
        val category = subcategory.name
        val intent = Intent(context, GlobalCelebrationDetailActivity::class.java).apply {
            putExtra("imageUrl", firstImage)
            putStringArrayListExtra("allGreetings", allImages)
            putExtra("message", message)
            putExtra("category", category)
        }
        context.startActivity(intent)
    }
    
    private fun navigateToGreetingDetail(greetingUrl: String, message: String, category: String) {
        navigator.navigateToGreetingDetail(greetingUrl, message, category)
    }
    
    fun onBackPressed() {
        if (model.selectedSubcategory != null) {
            // Clear selection if a subcategory is selected
            model = model.copy(selectedSubcategory = null)
        } else {
            // Otherwise, go back to the main greetings screen
            navigator.navigateToGreetings() // Use navigateToGreetings instead of navigateBack
        }
    }
    
    fun getCategoryThumbnail(subcategory: GreetingSubcategory): String {
        // Return the first greeting image or empty string
        return subcategory.files.firstOrNull() ?: ""
    }
}
