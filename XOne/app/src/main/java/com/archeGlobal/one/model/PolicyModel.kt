package com.archeGlobal.one.model

data class PolicyModel(
    val title: String = "Policies",
    val policies: List<Policy> = emptyList()
) {
    data class Policy(
        val policyName: String = "",
        val filePath: String = "",
        val previewUrl: String = "",
        val showSosButton: Boolean = false // Add this if you use it in your UI
    )
}