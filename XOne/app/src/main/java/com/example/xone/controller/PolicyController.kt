package com.example.xone.controller

import android.content.Context
import android.widget.Toast
import com.example.xone.model.PolicyModel
import com.example.xone.navigation.Navigator

class PolicyController(
    private val context: Context,
    private val navigator: Navigator
) {
    val model = PolicyModel()

    fun onPolicyClick(policyName: String) {
        // TODO: Implement policy viewing logic
        Toast.makeText(context, "Opening $policyName", Toast.LENGTH_SHORT).show()
    }

    fun onDownloadClick(policyName: String) {
        // TODO: Implement policy download logic
        Toast.makeText(context, "Downloading $policyName", Toast.LENGTH_SHORT).show()
    }

    fun onBackClick() {
        navigator.navigateToHome()
    }
} 