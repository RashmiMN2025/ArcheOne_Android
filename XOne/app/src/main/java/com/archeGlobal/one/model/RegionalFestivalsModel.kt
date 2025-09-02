package com.archeGlobal.one.model

// Model class for Regional Festivals screen
data class RegionalFestivalsModel(
    val subcategories: List<GreetingSubcategory> = emptyList(),
    val searchQuery: String = "",
    val selectedSubcategory: GreetingSubcategory? = null
)