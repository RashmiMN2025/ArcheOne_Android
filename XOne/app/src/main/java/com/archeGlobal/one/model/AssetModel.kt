package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

data class AssetV2Request(
    val employeeCode: String? = null,
    val location: String? = null
)

data class AssetV2Response(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<EmployeeAssetGroup>
)

data class EmployeeAssetGroup(
    val employeeCode: String,
    val username: String,
    val location: String,
    val designation: String?,
    val division: String?,
    val department: String?,
    val mobileNumber: String?,
    val mailId: String?,
    val assets: List<ApiAssetDetail>
)

data class ApiAssetDetail(
    val location: String?,
    val division: String?,
    val assetType: String,
    val oldAssetId: String?,
    val newAssetId: String?,
    val purchaseDate: String,
    val modelNumber: String,
    val configuration: String,
    val reportingTo: String?,
    val divisionalHead: String?,
    val warrantyStart: String?,
    val warrantyEnd: String?,
    val dateOfIssue: String,
    val serialNumber: String,
    val isTagged: Int
)

data class AssetModel(
    val name: String = "",
    val employeeId: String = "",
    val mobile: String = "",
    val email: String = "",
    val location: String = "",
    val department: String = "",
    val designation: String = "",
    val reportingTo: String = "",          // from userData
    val assetDetails: List<AssetDetails> = emptyList(),
    val issueDescription: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AssetDetails(
    val location: String = "",
    val division: String = "",
    val assetType: String = "",
    val oldAssetId: String = "",
    val newAssetId: String = "",
    val purchaseDate: String = "",
    val modelNumber: String = "",
    val configuration: String = "",
    val reportingTo: String = "",
    val divisionalHead: String = "",
    val warrantyStart: String = "",
    val warrantyEnd: String = "",
    val dateOfIssue: String = "",
    val serialNumber: String = "",
    val isTagged: Int = 0
)

data class AssetTypeCountResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<AssetTypeCount>
)

data class AssetTypeCount(
    val assetType: String,
    val assetCount: Int
)

data class CreateAssetCategoryRequest(
    val assetType: String,
    val assetCount: Int = 0
)

data class CreateAssetCategoryResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: CreateAssetCategoryData?
)

data class CreateAssetCategoryData(
    val assetType: String,
    val assetCount: Int,
    val message: String
)

data class InventoryRequest(
    val assetType: String? = null
)

data class InventoryResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<DeviceGroup>
)

data class DeviceGroup(
    val deviceType: String,
    val assets: List<InventoryAsset>
)

data class InventoryAsset(
    val id: Int,
    val makeModel: String,
    val configuration: String,
    val serialNumber: String,
    val location: String,
    val purchaseDate: String,
    val warrantyStart: String,
    val warrantyEnd: String,
    val isCommissioned: Boolean
)

data class UpdateCommissionRequest(
    val serialNumber: String,
    val isCommissioned: Boolean
)

data class UpdateCommissionResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: UpdateCommissionData?
)

data class UpdateCommissionData(
    val serialNumber: String,
    val isCommissioned: Boolean,
    val message: String
)

data class DeleteAssetResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: DeleteAssetData?
)

data class DeleteAssetData(
    val serialNumber: String,
    val assetType: String,
    val message: String
)

// Updated AssetModel.kt (add these at the end)
data class AddInventoryAssetItemRequest(
    val id: Int? = null,
    val assetType: String,
    val makeModel: String? = null,
    val configuration: String? = null,
    val serialNumber: String? = null,
    val location: String? = null,
    val updatedBy: String? = null,
    val purchaseDate: String? = null,
    val warrantyStart: String? = null,
    val warrantyEnd: String? = null,
    val isCommissioned: Boolean = true
)

data class AddInventoryAssetItemResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: List<AddInventoryItemData>
)

data class AddInventoryItemData(
    val success: Boolean,
    val serialNumber: String,
    val assetType: String,
    val message: String,
    val id: Int
)

data class LocationAssetCountResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: List<LocationAssetCount>
)

data class LocationAssetCount(
    val location: String,
    val assetCount: Int
)

data class DownloadAssetInventoryRequest(
    val recipientEmail: String,
    val filters: Map<String, Any>? = null
)

data class DownloadAssetInventoryResponse(
    val success: Boolean,
    val message: String? = null,
    val statusCode: Int? = null,
    val status: Int? = null,
    val data: Any? = null
)

data class BulkUploadRequest(
    val userEmail: String
)

data class BulkUploadResponse(
    val success: Boolean,
    val message: String,
    val statusCode: Int,
    val data: BulkUploadData
)

data class BulkUploadData(
    val requestId: String,
    val userEmail: String,
    val uploadFormUrl: String,
    val initiatedAt: String
)

data class SelfTagRequest(
    val isTagged: Int = 0
)

data class SelfTagResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: List<SelfTagItem>
)

data class SelfTagItem(
    val employeeCode: String,
    val serialNumber: String,
    val username: String,
    val dateOfIssue: String,
    val location: String,
    val designation: String,
    val division: String,
    val department: String,
    val mobileNumber: String,
    val mailId: String,
    val assetType: String,
    val oldAssetId: String?,
    val newAssetId: String?,
    val purchaseDate: String,
    val modelNumber: String,
    val configuration: String,
    val reportingTo: String,
    val divisionalHead: String,
    val warrantyStart: String,
    val warrantyEnd: String,
    val isTagged: Int
)

data class TagActionRequest(
    val employeeCode: String,
    val serialNumber: String
)

data class TagActionResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: TagActionData?
)

data class TagActionData(
    val employeeCode: String?,
    val serialNumber: String?,
    val assetType: String? = null,
    val approvedAt: String? = null,
    val isTagged: Boolean? = null,
    val inventoryUpdated: Boolean? = null,
    val emailSent: Boolean? = null,
    val rejectedAt: String? = null,
    val assetDeleted: Boolean? = null
)

data class SelfTagAssetRequest(
    val assetType: String,
    val modelNumber: String,
    val serialNumber: String,
    val employeeCode: String,
    val username: String,
    val mailId: String,
    val mobileNumber: String,
    val location: String,
    val designation: String,
    val division: String,
    val department: String,
    val dateOfIssue: String,
    val reportingTo: String,
    val divisionalHead: String
)

data class SelfTagAssetResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: SelfTagAssetResponseData?
)

data class SelfTagAssetResponseData(
    @SerializedName("employee_code") val employeeCode: String,
    @SerializedName("serial_number") val serialNumber: String,
    @SerializedName("notification_sent_to") val notificationSentTo: String,
    val location: String,
    @SerializedName("inventory_data_fetched") val inventoryDataFetched: Boolean,
    @SerializedName("asset_type") val assetType: String
)

data class SuggestAssetUsersResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: List<SuggestedAssetUser>
)

data class SuggestedAssetUser(
    val employeeCode: String,
    val username: String,
    val location: String? = "",
    val designation: String? = "",
    val division: String? = "",
    val department: String? = "",
    val mobileNumber: String? = "",
    val emailId: String? = "",
    val divisionalHead: String? = "",
    val reportingManager: String? = ""
)

data class TagAssetCreateRequest(
    val serialNumber: String,
    val employeeCode: String,
    val username: String,
    val dateOfIssue: String,
    val location: String?,
    val designation: String?,
    val assetType: String
)

data class TagAssetCreateResponse(
    val success: Boolean,
    val message: String,
    val status_code: Int,
    val data: TagAssetCreateData?
)

data class TagAssetCreateData(
    val created: Boolean,
    val serialNumber: String,
    val employeeCode: String
)

data class DeleteTagAssetRequest(
    val serialNumber: String
)

data class DeleteTagAssetResponse(
    val success: Boolean,
    val message: String,
    val status: Int,
    val data: DeleteTagAssetData?
)

data class DeleteTagAssetData(
    val deleted: Boolean,
    val employeeCode: String,
    val assetType: String,
    val serialNumber: String,
    val message: String
)