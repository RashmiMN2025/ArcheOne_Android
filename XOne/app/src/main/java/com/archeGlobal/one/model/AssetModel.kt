package com.archeGlobal.one.model

data class AssetModel(
    val name: String = "",
    val employeeId: String = "",
    val mobile: String = "",
    val email: String = "",
    val location: String = "",
    val department: String = "",
    val designation: String = "",
    val assetDetails: List<AssetDetails> = emptyList(), // Changed to a list of AssetDetails
    val reportingTo: String = "",
    val divisionHead: String = "",
    val division: String = "",
    val issueDescription: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AssetDetails(
    val serialNo: String = "",
    val deviceModel: String = "",
    val dateOfIssue: String = "",
    val configuration: String = "",
    val assetType: String = "",
    val assetId: String = "",
    val purchaseDate: String = "",
    val oldAssetId: String = "",
    val hostName: String = "" // <-- Added
) 