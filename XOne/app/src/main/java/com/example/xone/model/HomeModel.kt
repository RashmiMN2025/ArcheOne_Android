package com.example.xone.model

data class HomeModel(
    val userName: String = "",
    val designation: String = "",
    val department: String = "",
    val employeeId: String = "",
    val showAllApps: Boolean = true,
    val viewFavorites: Boolean = false,
    val showSearchAndFavorites: Boolean = true,
    val categories: Map<String, List<HomeItem>> = emptyMap(),
    val favorites: Map<String, List<HomeItem>> = emptyMap(),
    val footerNavigation: FooterNavigationModel = FooterNavigationModel()
)

data class HomeItem(
    val title: String,
    val icon: String,
    val isFavorite: Boolean = false,
    val category: String = ""
) 