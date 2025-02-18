package com.example.xone.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.xone.model.HomeItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "XOne_preferences",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun saveFavorites(favorites: Map<String, List<HomeItem>>) {
        val json = gson.toJson(favorites)
        sharedPreferences.edit().putString(KEY_FAVORITES, json).apply()
    }

    fun getFavorites(): Map<String, List<HomeItem>> {
        val json = sharedPreferences.getString(KEY_FAVORITES, null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, List<HomeItem>>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyMap()
        }
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
    }
} 