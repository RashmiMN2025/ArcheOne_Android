package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class GreetingModel(
    val categories: Map<String, List<String>> = emptyMap(),
    val categoryMessages: Map<String, String> = emptyMap(),
    val selectedCategory: String? = null,
    val selectedGreeting: String? = null,
    val message: String = "",
    val searchQuery: String = "",
    val subcategories: List<GreetingSubcategory> = emptyList(),
    val selectedSubcategory: GreetingSubcategory? = null,
)

data class GreetingCategory(
    val title: String,
    val imageUrl: String,
)

data class GreetingImage(
    val url: String,
)

data class ApiGreetingCategory(
    val id: Int,
    val name: String,
    val files: List<String>,
    val message: String,
    @SerializedName("subfolder")
    val subfolder: List<GreetingSubcategory>? = null,
)

data class GreetingSubcategory(
    val id: Int,
    val name: String,
    val files: List<String>,
    val message: String,
)
