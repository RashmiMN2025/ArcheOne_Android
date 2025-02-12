package com.example.xone.model

data class AssetModel(
    val name: String = "",
    val employeeId: String = "",
    val mobile: String = "",
    val email: String = "",
    val location: String = "",
    val assetDetails: AssetDetails = AssetDetails(),
    val issueDescription: String = ""
)

data class AssetDetails(
    val serialNo: String = "N/A",
    val deviceModel: String = "N/A",
    val dateOfIssue: String = "N/A",
    val configuration: String = "N/A"
) 