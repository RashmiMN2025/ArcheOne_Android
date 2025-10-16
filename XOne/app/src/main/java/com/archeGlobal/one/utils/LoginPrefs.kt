package com.archeGlobal.one.utils

import android.content.Context

fun setFirstTimeLogin(
    context: Context,
    isFirstTime: Boolean,
) {
    context
        .getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("is_first_time_login", isFirstTime)
        .apply()
}

fun isFirstTimeLogin(context: Context): Boolean =
    context
        .getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        .getBoolean("is_first_time_login", true)
