package com.archeGlobal.one.network

import com.google.gson.annotations.SerializedName

data class ExpensesListResponse(
    val total: Int,
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    val data: List<ExpenseItemResponse>,
)

data class ExpenseItemResponse(
    val id: Int,
    val name: String?,
    @SerializedName("vendor_name") val vendorName: String?,
    @SerializedName("project_code") val projectCode: String?,
    val format: String?,
    val category: String?,
    @SerializedName("sub_category") val subCategory: String?,
    @SerializedName("total_amount") val totalAmount: String?,
    @SerializedName("bill_date") val billDate: String?,
    val scope: String?,
    @SerializedName("user_amount") val userAmount: String?,
    val status: String?,
    @SerializedName("document_no") val documentNo: String?,
    @SerializedName("split_with") val splitWith: List<Any>?,
    @SerializedName("user_expense_id") val userExpenseId: Int?,
    val note: String?,
    @SerializedName("submitted_at") val submittedAt: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("is_manual") val isManual: Boolean?,
    @SerializedName("target_user_id") val targetUserId: Int?,
)

data class ExpenseUi(
    val id: String,
    val name: String,
    val vendorName: String,
    val projectCode: String,
    val category: String,
    val subCategory: String,
    val totalAmount: String,
    val billDate: String,
    val status: String,
    val documentNo: String?,
    val userExpenseId: Int?,
    val createdAt: String,
)

fun ExpenseItemResponse.toUi(): ExpenseUi = ExpenseUi(
    id = id.toString(),
    name = name?.takeIf { it.isNotBlank() } ?: "-",
    vendorName = vendorName?.takeIf { it.isNotBlank() } ?: "-",
    projectCode = projectCode?.takeIf { it.isNotBlank() } ?: "-",
    category = category?.takeIf { it.isNotBlank() } ?: "-",
    subCategory = subCategory?.takeIf { it.isNotBlank() } ?: "-",
    totalAmount = totalAmount?.takeIf { it.isNotBlank() }?.let { "Rs $it" } ?: "-",
    billDate = billDate?.takeIf { it.isNotBlank() } ?: "-",
    status = status?.takeIf { it.isNotBlank() } ?: "-",
    documentNo = documentNo,
    userExpenseId = userExpenseId,
    createdAt = createdAt?.takeIf { it.isNotBlank() } ?: "-",
)

data class UploadExpenseRequest(
    val file: String,
    val filename: String? = null,
)

data class UploadExpenseResponse(
    val id: Int,
    val name: String,
    val status: String,
    val format: String? = null,
)
