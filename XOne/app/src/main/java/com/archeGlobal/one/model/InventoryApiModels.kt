package com.archeGlobal.one.model

import com.google.gson.annotations.SerializedName

// Add Item API Models
data class AddInventoryItemRequest(
    @SerializedName("updatedby")
    val updatedBy: String,
    @SerializedName("itemcategory")
    val itemCategory: String,
    @SerializedName("view")
    val view: String,
    @SerializedName("itemname")
    val itemName: String,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("brand")
    val brand: String,
    @SerializedName("openingstock")
    val openingStock: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("suppliedDate")
    val suppliedDate: String
)

data class AddInventoryItemResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: AddedInventoryItem?
)

data class AddedInventoryItem(
    @SerializedName("Sl_No")
    val slNo: Int,
    @SerializedName("Item/Material_Category")
    val itemCategory: String,
    @SerializedName("Item/Material_ID")
    val itemId: String,
    @SerializedName("Item/Material_Name")
    val itemName: String,
    @SerializedName("Unit")
    val unit: String,
    @SerializedName("Brand")
    val brand: String,
    @SerializedName("Opening_Stock")
    val openingStock: String,
    @SerializedName("New_Stock")
    val newStock: String,
    @SerializedName("Total_Stock")
    val totalStock: String,
    @SerializedName("Consumption")
    val consumption: String,
    @SerializedName("Closing_Stock")
    val closingStock: String,
    @SerializedName("Location")
    val location: String,
    @SerializedName("Updated_By_")
    val updatedBy: String,
    @SerializedName("Last_Updated_Date_Time")
    val lastUpdatedDateTime: String,
    @SerializedName("stock_supplied_date")
    val stockSuppliedDate: String
)

// Update Item API Models
data class UpdateInventoryItemRequest(
    @SerializedName("updatedby")
    val updatedBy: String,
    @SerializedName("itemname")
    val itemName: String,
    @SerializedName("itemid")
    val itemId: String,
    @SerializedName("itemcount")
    val itemCount: Int,
    @SerializedName("brand")
    val brand: String,
    @SerializedName("unit")
    val unit: String,
    @SerializedName("suppliedDate")
    val suppliedDate: String? = null,
    @SerializedName("updateUtilization")
    val updateUtilization: Boolean = true,
    @SerializedName("location")
    val location: String? = null
)

data class UpdateInventoryItemResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: UpdatedInventoryItem?
)

data class UpdatedInventoryItem(
    @SerializedName("Sl_No")
    val slNo: Int,
    @SerializedName("Item/Material_Category")
    val itemCategory: String,
    @SerializedName("Item/Material_ID")
    val itemId: String,
    @SerializedName("Item/Material_Name")
    val itemName: String,
    @SerializedName("Unit")
    val unit: String,
    @SerializedName("Brand")
    val brand: String,
    @SerializedName("Opening_Stock")
    val openingStock: String,
    @SerializedName("New_Stock")
    val newStock: String,
    @SerializedName("Total_Stock")
    val totalStock: String,
    @SerializedName("Consumption")
    val consumption: String,
    @SerializedName("Closing_Stock")
    val closingStock: String,
    @SerializedName("Location")
    val location: String,
    @SerializedName("Updated_By_")
    val updatedBy: String,
    @SerializedName("Last_Updated_Date_Time")
    val lastUpdatedDateTime: String,
    @SerializedName("stock_supplied_date")
    val stockSuppliedDate: String,
    @SerializedName("Utilization")
    val utilization: String
)