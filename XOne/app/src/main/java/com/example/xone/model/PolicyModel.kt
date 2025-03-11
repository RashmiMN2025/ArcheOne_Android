package com.example.xone.model

data class PolicyModel(
    val title: String = "Company Policies",
    val policies: List<Policy> = emptyList()
) {
    data class Policy(
        val policyName: String,
        val filePath: String
    )
}
