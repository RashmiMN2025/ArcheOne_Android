package com.archeGlobal.one.model

data class GreetingModel(
    val categories: Map<String, List<String>> = emptyMap(),
    val categoryMessages: Map<String, String> = emptyMap(),
    val selectedCategory: String? = null,
    val selectedGreeting: String? = null,
    val message: String = "",
    val searchQuery: String = ""
)

data class GreetingCategory(
    val title: String,
    val imageUrl: String
)

data class GreetingImage(
    val url: String
)

data class ApiGreetingCategory(
    val id: Int,
    val name: String,
    val files: List<String>,
    val message: String
)