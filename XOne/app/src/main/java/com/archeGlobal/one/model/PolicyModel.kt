package com.archeGlobal.one.model

data class PolicyModel(
    val title: String = "Policies",
    val policies: List<Policy> = emptyList()
) {
    data class Policy(
        val policyName: String,
        val filePath: String,
        val showSosButton: Boolean = false,
        val previewUrl: String? = null
    )
}
