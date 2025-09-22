package com.archeGlobal.one.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.archeGlobal.one.GreetingDetailActivity
import com.archeGlobal.one.model.GreetingSubcategory
import com.archeGlobal.one.model.RegionalFestivalsModel
import com.archeGlobal.one.navigation.Navigator
import com.archeGlobal.one.utils.UserDataManager
import androidx.activity.ComponentActivity
import com.archeGlobal.one.R

class RegionalFestivalsController(
    private val context: Context,
    private val navigator: Navigator
) {
    private val userDataManager = UserDataManager.getInstance(context)

    var model by mutableStateOf(RegionalFestivalsModel())
        private set

    init {
        loadSubcategories()
    }

    private fun loadSubcategories() {
        val greetingCategories = userDataManager.getGreetingCategoriesData()
        if (greetingCategories == null) {
            Log.e("RegionalFestivalsController", "No greeting categories data available (null)")
            model = model.copy(subcategories = emptyList())
            return
        }
        if (greetingCategories.isEmpty()) {
            Log.e("RegionalFestivalsController", "Greeting categories data is empty")
            model = model.copy(subcategories = emptyList())
            return
        }
        // Find the Regional Festivals category and use its subfolder as subcategories
        val regionalFestivals = greetingCategories.find { it.name == "Regional Festivals" }
        val subcategories = regionalFestivals?.subfolder ?: emptyList()
        Log.d("RegionalFestivalsController", "Loaded ${subcategories.size} subcategories for Regional Festivals (from subfolder)")
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
        val intent = Intent(context, GreetingDetailActivity::class.java).apply {
            putExtra("imageUrl", firstImage)
            putStringArrayListExtra("allGreetings", allImages)
            putExtra("message", message)
            putExtra("category", category)
        }
        context.startActivity(intent)
    }

    fun onBackPressed() {
        if (model.selectedSubcategory != null) {
            // Clear selection if a subcategory is selected
            model = model.copy(selectedSubcategory = null)
        } else {
            // Finish the activity with back animation
            (context as? ComponentActivity)?.let { activity ->
                activity.finish()
                activity.overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
        }
    }

    fun getCategoryThumbnail(subcategory: GreetingSubcategory): String {
        // Return the first greeting image or empty string
        return subcategory.files.firstOrNull() ?: ""
    }
}
