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

data class SubmittedExpensesListResponse(
    val total: Int,
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    val data: List<SubmittedExpenseItemResponse>,
)

data class SubmittedExpenseItemResponse(
    @SerializedName("row_type") val rowType: String? = null,
    val id: Int,
    @SerializedName("user_expense_id") val userExpenseId: Int? = null,
    val employee: SubmittedExpenseEmployee? = null,
    val category: String? = null,
    val name: String? = null,
    @SerializedName("vendor_name") val vendorName: String? = null,
    val amount: String? = null,
    @SerializedName("approved_amount") val approvedAmount: String? = null,
    @SerializedName("bill_date") val billDate: String? = null,
    val status: String? = null,
    @SerializedName("approval_status") val approvalStatus: String? = null,
    @SerializedName("approval_level") val approvalLevel: String? = null,
    val format: String? = null,
    @SerializedName("submitted_at") val submittedAt: String? = null,
    @SerializedName("target_user_id") val targetUserId: Int? = null,
)

data class SubmittedExpenseEmployee(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val code: String? = null,
) {
    val displayName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").trim().ifBlank { "-" }
}

data class SubmittedExpenseUi(
    val id: String,
    val userExpenseId: Int?,
    val employeeName: String,
    val employeeCode: String,
    val category: String,
    val name: String,
    val vendorName: String,
    val amount: String,
    val approvedAmount: String,
    val billDate: String,
    val status: String,
    val format: String?,
    val submittedAt: String,
)

fun SubmittedExpenseItemResponse.toSubmittedUi(): SubmittedExpenseUi = SubmittedExpenseUi(
    id = id.toString(),
    userExpenseId = userExpenseId,
    employeeName = employee?.displayName ?: "-",
    employeeCode = employee?.code?.takeIf { it.isNotBlank() } ?: "-",
    category = category?.takeIf { it.isNotBlank() } ?: "-",
    name = name?.takeIf { it.isNotBlank() } ?: "-",
    vendorName = vendorName?.takeIf { it.isNotBlank() } ?: "-",
    amount = amount?.takeIf { it.isNotBlank() }?.let { "Rs $it" } ?: "-",
    approvedAmount = approvedAmount?.takeIf { it.isNotBlank() }?.let { "Rs $it" } ?: "-",
    billDate = billDate?.takeIf { it.isNotBlank() } ?: "-",
    status = status?.takeIf { it.isNotBlank() } ?: "-",
    format = format,
    submittedAt = submittedAt?.takeIf { it.isNotBlank() } ?: "-",
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

data class ConfidenceValue(
    val value: String? = null,
    @SerializedName("confidence_score") val confidenceScore: Int? = null,
)

data class ExpenseDetailResponse(
    val id: Int,
    val name: String?,
    @SerializedName("vendor_name") val vendorName: String?,
    val project: ExpenseDetailProject? = null,
    val url: String?,
    val format: String?,
    val category: String?,
    @SerializedName("sub_category") val subCategory: String?,
    @SerializedName("total_amount") val totalAmount: String?,
    @SerializedName("bill_date") val billDate: String?,
    val currency: String?,
    val status: String?,
    @SerializedName("user_expense_id") val userExpenseId: Int?,
    val users: List<ExpenseDetailUser>? = null,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    /** Raw extracted payload — must be echoed back on split/update APIs. */
    val data: com.google.gson.JsonObject? = null,
    val note: String? = null,
    @SerializedName("overall_document_confidence") val overallDocumentConfidence: Double? = null,
    @SerializedName("verifiability_message") val verifiabilityMessage: String?,
    val approvers: List<Any>? = null,
    @SerializedName("accommodation_type") val accommodationType: String? = null,
    @SerializedName("is_manual") val isManual: Boolean? = null,
)

data class ExpenseDetailProject(
    val id: Int? = null,
    val code: String? = null,
    @SerializedName("customer_id") val customerId: String? = null,
    @SerializedName("so_number") val soNumber: String? = null,
    @SerializedName("customer_name") val customerName: String? = null,
)

data class ExpenseDetailUser(
    val id: Int?,
    val email: String?,
    val amount: String? = null,
    val status: String? = null,
    @SerializedName("project_id") val projectId: Int? = null,
)

data class ExpenseExtractedData(
    val date: ConfidenceValue? = null,
    val currency: ConfidenceValue? = null,
    @SerializedName("project_id") val projectId: ConfidenceValue? = null,
    @SerializedName("document_id") val documentId: ConfidenceValue? = null,
    val identifiers: ExpenseIdentifiers? = null,
    @SerializedName("other_items") val otherItems: Map<String, ConfidenceValue>? = null,
    @SerializedName("vendor_name") val vendorName: ConfidenceValue? = null,
    @SerializedName("guest_details") val guestDetails: List<Any>? = null,
    @SerializedName("hotel_details") val hotelDetails: ExpenseHotelDetails? = null,
    @SerializedName("booking_details") val bookingDetails: ExpenseBookingDetails? = null,
    @SerializedName("transaction_details") val transactionDetails: ExpenseTransactionDetails? = null,
    @SerializedName("grand_total_After_GST") val grandTotalAfterGst: ConfidenceValue? = null,
)

data class ExpenseIdentifiers(
    val pnr: ConfidenceValue? = null,
    @SerializedName("bill no") val billNo: ConfidenceValue? = null,
    @SerializedName("serial no") val serialNo: ConfidenceValue? = null,
    @SerializedName("booking id") val bookingId: ConfidenceValue? = null,
    @SerializedName("invoice no") val invoiceNo: ConfidenceValue? = null,
    @SerializedName("confirmation number") val confirmationNumber: ConfidenceValue? = null,
    @SerializedName("booking reference no") val bookingReferenceNo: ConfidenceValue? = null,
)

data class ExpenseHotelDetails(
    val fax: ConfidenceValue? = null,
    val email: ConfidenceValue? = null,
    val phone: ConfidenceValue? = null,
    val address: ConfidenceValue? = null,
    val website: ConfidenceValue? = null,
    @SerializedName("gst_number") val gstNumber: ConfidenceValue? = null,
)

data class ExpenseBookingDetails(
    @SerializedName("meal_plan") val mealPlan: ConfidenceValue? = null,
    @SerializedName("room_type") val roomType: ConfidenceValue? = null,
    @SerializedName("arrival_date") val arrivalDate: ConfidenceValue? = null,
    @SerializedName("booking_date") val bookingDate: ConfidenceValue? = null,
    @SerializedName("booking_status") val bookingStatus: ConfidenceValue? = null,
    @SerializedName("departure_date") val departureDate: ConfidenceValue? = null,
    @SerializedName("booking_channel") val bookingChannel: ConfidenceValue? = null,
    @SerializedName("number_of_rooms") val numberOfRooms: ConfidenceValue? = null,
    @SerializedName("number_of_nights") val numberOfNights: ConfidenceValue? = null,
    @SerializedName("reservation_number") val reservationNumber: ConfidenceValue? = null,
)

data class ExpenseTransactionDetails(
    val subtotal: ConfidenceValue? = null,
    @SerializedName("total_due") val totalDue: ConfidenceValue? = null,
    @SerializedName("total_paid") val totalPaid: ConfidenceValue? = null,
    @SerializedName("balance_due") val balanceDue: ConfidenceValue? = null,
    @SerializedName("tax_details") val taxDetails: ExpenseTaxDetails? = null,
    @SerializedName("payment_method") val paymentMethod: ConfidenceValue? = null,
    @SerializedName("service_charges") val serviceCharges: ConfidenceValue? = null,
    @SerializedName("administrative_fees") val administrativeFees: ConfidenceValue? = null,
    @SerializedName("card_last_four_digits") val cardLastFourDigits: ConfidenceValue? = null,
)

data class ExpenseTaxDetails(
    @SerializedName("tax_amount") val taxAmount: ConfidenceValue? = null,
    @SerializedName("vat_amount") val vatAmount: ConfidenceValue? = null,
    @SerializedName("cgst_amount") val cgstAmount: ConfidenceValue? = null,
    @SerializedName("igst_amount") val igstAmount: ConfidenceValue? = null,
    @SerializedName("sgst_amount") val sgstAmount: ConfidenceValue? = null,
    @SerializedName("vat_percent") val vatPercent: ConfidenceValue? = null,
    @SerializedName("cgst_percent") val cgstPercent: ConfidenceValue? = null,
    @SerializedName("igst_percent") val igstPercent: ConfidenceValue? = null,
    @SerializedName("sgst_percent") val sgstPercent: ConfidenceValue? = null,
    @SerializedName("other_tax_amount") val otherTaxAmount: ConfidenceValue? = null,
    @SerializedName("other_tax_percent") val otherTaxPercent: ConfidenceValue? = null,
)

data class ExpenseDetailUi(
    val id: String,
    val name: String,
    val fileUrl: String?,
    val format: String?,
    val status: String,
    val createdAt: String,
    val billDate: String,
    val projectId: String,
    val category: String,
    val mode: String,
    val accommodationType: String,
    val hotelName: String,
    val gstinOfHotel: String,
    val currency: String,
    val checkIn: String,
    val checkOut: String,
    val noOfNights: String,
    val roomType: String,
    val serviceCharges: String,
    val sgstAmount: String,
    val cgstAmount: String,
    val igstAmount: String,
    val amount: String,
    val note: String,
    val soNumber: String,
    val documentId: String,
    val customerId: String = "",
    val customerName: String = "",
    val ownerUserId: Int? = null,
    val ownerUserEmail: String? = null,
    /** Original extracted `data` object from GET expense — required by split API. */
    val extractedData: com.google.gson.JsonObject? = null,
)

private fun String?.formatExpenseCategory(): String =
    this?.takeIf { it.isNotBlank() }
        ?.replace('_', ' ')
        ?.split(' ')
        ?.joinToString(" ") { word ->
            word.replaceFirstChar { ch ->
                if (ch.isLowerCase()) ch.titlecase(java.util.Locale.getDefault()) else ch.toString()
            }
        }
        .orEmpty()

private fun String?.formatAccommodationType(): String =
    when (this?.lowercase()?.trim()) {
        "domestic" -> "Domestic"
        "international" -> "International"
        else -> this?.takeIf { it.isNotBlank() }.orEmpty()
    }

private fun com.google.gson.JsonObject?.confidenceText(vararg path: String): String {
    if (this == null || path.isEmpty()) return ""
    var current: com.google.gson.JsonElement? = this
    for (key in path) {
        current = current?.takeIf { it.isJsonObject }?.asJsonObject?.get(key) ?: return ""
    }
    return when {
        current == null || current.isJsonNull -> ""
        current.isJsonObject -> current.asJsonObject.get("value")?.takeUnless { it.isJsonNull }?.asString.orEmpty()
        current.isJsonPrimitive -> current.asString
        else -> ""
    }.trim()
}

fun ExpenseDetailResponse.toDetailUi(): ExpenseDetailUi {
    val extracted = data
    val amountValue = totalAmount?.takeIf { it.isNotBlank() }
        ?: extracted.confidenceText("grand_total_After_GST").takeIf { it.isNotBlank() }
        ?: extracted.confidenceText("total_fare").takeIf { it.isNotBlank() }
        ?: extracted.confidenceText("transaction_details", "total_paid").takeIf { it.isNotBlank() }
        ?: ""

    val soNumber = project?.soNumber?.takeIf { it.isNotBlank() }
        ?: extracted.confidenceText("identifiers", "invoice no")
            .ifBlank { extracted.confidenceText("identifiers", "serial no") }
            .ifBlank { extracted.confidenceText("document_id") }

    val noteValue = note?.takeIf { it.isNotBlank() }
        ?: verifiabilityMessage?.takeIf { it.isNotBlank() }
        .orEmpty()

    return ExpenseDetailUi(
        id = id.toString(),
        name = name?.takeIf { it.isNotBlank() } ?: "Uploaded document",
        fileUrl = url,
        format = format,
        status = status?.takeIf { it.isNotBlank() } ?: "-",
        createdAt = createdAt?.takeIf { it.isNotBlank() } ?: "-",
        billDate = billDate?.takeIf { it.isNotBlank() }
            ?: extracted.confidenceText("date").ifBlank {
                extracted.confidenceText("booking_details", "arrival_date")
            },
        projectId = project?.code?.takeIf { it.isNotBlank() }
            ?: extracted.confidenceText("project_id"),
        category = category.formatExpenseCategory(),
        mode = extracted.confidenceText("transaction_details", "payment_method")
            .ifBlank { extracted.confidenceText("booking_details", "booking_channel") },
        accommodationType = accommodationType.formatAccommodationType()
            .ifBlank { subCategory.formatExpenseCategory() },
        hotelName = vendorName?.takeIf { it.isNotBlank() }
            ?: extracted.confidenceText("vendor_name"),
        gstinOfHotel = extracted.confidenceText("hotel_details", "gst_number"),
        currency = currency?.takeIf { it.isNotBlank() }
            ?: extracted.confidenceText("currency").ifBlank { "INR" },
        checkIn = extracted.confidenceText("booking_details", "arrival_date"),
        checkOut = extracted.confidenceText("booking_details", "departure_date"),
        noOfNights = extracted.confidenceText("booking_details", "number_of_nights"),
        roomType = extracted.confidenceText("booking_details", "room_type"),
        serviceCharges = extracted.confidenceText("transaction_details", "service_charges")
            .ifBlank { extracted.confidenceText("service_charge") },
        sgstAmount = extracted.confidenceText("transaction_details", "tax_details", "sgst_amount")
            .ifBlank { extracted.confidenceText("taxes", "sgst_amount") },
        cgstAmount = extracted.confidenceText("transaction_details", "tax_details", "cgst_amount")
            .ifBlank { extracted.confidenceText("taxes", "cgst_amount") },
        igstAmount = extracted.confidenceText("transaction_details", "tax_details", "igst_amount")
            .ifBlank { extracted.confidenceText("taxes", "igst_amount") },
        amount = amountValue,
        note = noteValue,
        soNumber = soNumber,
        documentId = extracted.confidenceText("document_id"),
        customerId = project?.customerId?.takeIf { it.isNotBlank() }.orEmpty(),
        customerName = project?.customerName?.takeIf { it.isNotBlank() }.orEmpty(),
        ownerUserId = users?.firstOrNull()?.id,
        ownerUserEmail = users?.firstOrNull()?.email,
        extractedData = extracted?.deepCopy(),
    )
}

data class DeleteExpenseResponse(
    val message: String? = null,
)

data class ExpenseUserOption(
    val id: Int,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
) {
    val displayName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .trim()
            .ifBlank { "User $id" }
}

data class SplitExpenseRequest(
    @SerializedName("updated_data") val updatedData: SplitUpdatedData,
    val users: List<SplitUserAmount>,
)

data class SplitUpdatedData(
    val data: com.google.gson.JsonObject? = null,
    val note: String? = null,
    @SerializedName("project_id") val projectId: Int? = null,
    @SerializedName("trip_id") val tripId: Int? = null,
    @SerializedName("flight_class") val flightClass: String? = null,
    @SerializedName("train_class") val trainClass: String? = null,
    @SerializedName("accommodation_type") val accommodationType: String? = null,
    @SerializedName("submit_behavior") val submitBehavior: String? = null,
)

/** Ensures `data.date` exists in the shape the split API expects. */
fun ensureSplitExtractedData(
    source: com.google.gson.JsonObject?,
    preferredDate: String?,
): com.google.gson.JsonObject {
    val data = source?.deepCopy() ?: com.google.gson.JsonObject()
    val existingDate = data.getAsJsonObject("date")
    val existingValue = existingDate?.get("value")?.takeUnless { it.isJsonNull }?.asString
    val dateValue = preferredDate?.takeIf { it.isNotBlank() } ?: existingValue

    if (!dateValue.isNullOrBlank()) {
        val dateObj = existingDate?.deepCopy() ?: com.google.gson.JsonObject()
        dateObj.addProperty("value", dateValue)
        if (!dateObj.has("confidence_score") || dateObj.get("confidence_score").isJsonNull) {
            dateObj.addProperty("confidence_score", existingDate?.get("confidence_score")?.asInt ?: 88)
        }
        data.add("date", dateObj)
    }
    return data
}

data class SplitUserAmount(
    @SerializedName("user_id") val userId: Int,
    val amount: Double,
)

data class SplitExpenseResponse(
    @SerializedName("expense_id") val expenseId: Int? = null,
    @SerializedName("total_split") val totalSplit: String? = null,
    val users: List<SplitUserAmountResponse>? = null,
    @SerializedName("updated_data") val updatedData: Map<String, Any>? = null,
    val note: String? = null,
    val status: String? = null,
    @SerializedName("project_id") val projectId: Int? = null,
    @SerializedName("trip_id") val tripId: Int? = null,
)

data class SplitUserAmountResponse(
    @SerializedName("user_id") val userId: Int? = null,
    val amount: String? = null,
)

data class ExpenseSubmitRequest(
    @SerializedName("project_id") val projectId: Int? = null,
    @SerializedName("trip_id") val tripId: Int? = null,
    @SerializedName("flight_class") val flightClass: String? = null,
    @SerializedName("train_class") val trainClass: String? = null,
    @SerializedName("accommodation_type") val accommodationType: String? = null,
    val data: com.google.gson.JsonObject? = null,
    val note: String? = null,
    @SerializedName("submit_behavior") val submitBehavior: String? = null,
)

data class ExpenseSubmitResponse(
    val id: Int? = null,
    val data: com.google.gson.JsonObject? = null,
    val note: String? = null,
    @SerializedName("overall_document_confidence") val overallDocumentConfidence: Double? = null,
    val status: String? = null,
    @SerializedName("project_id") val projectId: Int? = null,
    @SerializedName("trip_id") val tripId: Int? = null,
    @SerializedName("user_amount") val userAmount: String? = null,
)

data class ExpenseSubmitErrorResponse(
    val detail: ExpenseSubmitErrorDetail? = null,
)

data class ExpenseSubmitErrorDetail(
    val message: String? = null,
    @SerializedName("error_code") val errorCode: String? = null,
    @SerializedName("total_amount") val totalAmount: Double? = null,
    @SerializedName("approval_limit") val approvalLimit: Double? = null,
    @SerializedName("remaining_daily") val remainingDaily: Double? = null,
    @SerializedName("remaining_monthly") val remainingMonthly: Double? = null,
)

data class ApiValidationErrorResponse(
    val detail: List<ApiValidationErrorDetail>? = null,
)

data class ApiValidationErrorDetail(
    val field: String? = null,
    val message: String? = null,
    val loc: List<Any>? = null,
    val msg: String? = null,
    val type: String? = null,
    val input: Any? = null,
    val ctx: Map<String, Any>? = null,
)
