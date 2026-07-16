package com.archeGlobal.one.network

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

data class TripsListResponse(
    val total: Int,
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("has_next_page")
    val hasNextPage: Boolean,
    val data: List<TripItemResponse>,
)

data class TripItemResponse(
    val destination: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    val description: String?,
    @SerializedName("hotel_accommodation_needed")
    val hotelAccommodationNeeded: Boolean,
    @SerializedName("mode_of_travel")
    val modeOfTravel: String,
    @SerializedName("vehicle_needed")
    val vehicleNeeded: Boolean,
    @SerializedName("advance_needed")
    val advanceNeeded: Boolean,
    @SerializedName("advance_amount")
    val advanceAmount: String?,
    @SerializedName("estimated_amount")
    val estimatedAmount: String?,
    val id: Int,
    @SerializedName("trip_id")
    val tripId: String?,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("approved_amount")
    val approvedAmount: String?,
    @SerializedName("trip_code")
    val tripCode: String?,
    @SerializedName("total_approved_amount")
    val totalApprovedAmount: String?,
    @SerializedName("total_issued_amount")
    val totalIssuedAmount: String?,
    @SerializedName("total_requested_amount")
    val totalRequestedAmount: String?,
    val advances: List<AdvanceResponse>,
    val employee: EmployeeResponse?,
    val project: TripProjectResponse?,
)

data class TripProjectResponse(
    val id: Int,
    val code: String,
)

data class AdvanceResponse(
    val id: Int,
    val note: String?,
    @SerializedName("requested_amount")
    val requestedAmount: String?,
    @SerializedName("end_date")
    val endDate: String?,
    @SerializedName("estimated_amount")
    val estimatedAmount: String?,
    val status: String?,
    @SerializedName("issued_amount")
    val issuedAmount: String?,
)

data class EmployeeResponse(
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    val id: Int?,
    val email: String?,
)

data class CreateTripRequest(
    val destination: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    val description: String?,
    @SerializedName("hotel_accommodation_needed")
    val hotelAccommodationNeeded: Boolean,
    @SerializedName("mode_of_travel")
    val modeOfTravel: String,
    @SerializedName("vehicle_needed")
    val vehicleNeeded: Boolean,
    @SerializedName("advance_needed")
    val advanceNeeded: Boolean,
    @SerializedName("advance_amount")
    val advanceAmount: Double,
    @SerializedName("estimated_amount")
    val estimatedAmount: Double,
    @SerializedName("requested_amount")
    val requestedAmount: Double,
    @SerializedName("project_id")
    val projectId: Int,
)

data class DeleteTripResponse(
    val message: String,
)

data class ProjectOption(
    val id: Int,
    val code: String,
)

data class AdvanceRequestUi(
    val id: Int,
    val note: String,
    val requestedAmount: String,
    val requestedAmountRaw: String,
    val endDate: String,
    val estimatedAmount: String,
    val status: String,
    val issuedAmount: String,
)

data class ProjectOptionsResponse(
    val total: Int,
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("has_next_page")
    val hasNextPage: Boolean,
    val data: List<ProjectOption>,
)

data class TravelRequestItemUi(
    val requestId: String,
    val tripId: String,
    val tripCode: String,
    val projectId: String,
    val destination: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val travelDates: String,
    val estimatedCost: String,
    val modeOfTravel: String,
    val hotelNeeded: String,
    val vehicleNeeded: String,
    val advanceNeeded: String,
    val advanceAmount: String,
    val approvedAmount: String,
    val totalRequestedAmount: String,
    val totalApprovedAmount: String,
    val totalIssuedAmount: String,
    val advances: List<AdvanceRequestUi>,
    val employeeName: String,
    val employeeEmail: String,
    val firstAdvanceRequestedAmount: String,
    val firstAdvanceRequestedAmountRaw: String,
    val firstAdvanceStatus: String,
    val firstAdvanceIssuedAmount: String,
    val status: String,
)

fun TripItemResponse.toTravelRequestItemUi(): TravelRequestItemUi =
    TravelRequestItemUi(
        requestId = id.toString(),
        tripId = tripId?.takeIf { it.isNotBlank() } ?: id.toString(),
        tripCode = tripCode?.takeIf { it.isNotBlank() } ?: "-",
        projectId = project?.code?.takeIf { it.isNotBlank() } ?: "-",
        destination = destination,
        description = description?.takeIf { it.isNotBlank() } ?: "-",
        startDate = startDate,
        endDate = endDate,
        travelDates = formatTravelDates(startDate, endDate),
        estimatedCost = formatCurrency(estimatedAmount),
        modeOfTravel = modeOfTravel.replaceFirstChar { it.uppercase() },
        hotelNeeded = if (hotelAccommodationNeeded) "Yes" else "No",
        vehicleNeeded = if (vehicleNeeded) "Yes" else "No",
        advanceNeeded = if (advanceNeeded) "Yes" else "No",
        advanceAmount = advanceAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        approvedAmount = approvedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        totalRequestedAmount = totalRequestedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        totalApprovedAmount = totalApprovedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        totalIssuedAmount = totalIssuedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        advances = advances.map { advance ->
            AdvanceRequestUi(
                id = advance.id,
                note = advance.note?.takeIf { it.isNotBlank() } ?: "-",
                requestedAmount = advance.requestedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
                requestedAmountRaw = advance.requestedAmount.orEmpty(),
                endDate = advance.endDate?.takeIf { it.isNotBlank() } ?: "-",
                estimatedAmount = advance.estimatedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
                status = advance.status?.takeIf { it.isNotBlank() } ?: "-",
                issuedAmount = advance.issuedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
            )
        },
        employeeName = listOfNotNull(this.employee?.firstName, this.employee?.lastName).joinToString(" ").takeIf { it.isNotBlank() } ?: "-",
        employeeEmail = this.employee?.email ?: "-",
        firstAdvanceRequestedAmount = advances.firstOrNull()?.requestedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        firstAdvanceRequestedAmountRaw = advances.firstOrNull()?.requestedAmount.orEmpty(),
        firstAdvanceStatus = advances.firstOrNull()?.status?.takeIf { it.isNotBlank() } ?: "-",
        firstAdvanceIssuedAmount = advances.firstOrNull()?.issuedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        status = status,
    )

private fun formatTravelDates(
    startDate: String,
    endDate: String,
): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    return try {
        val start = inputFormat.parse(startDate)
        val end = inputFormat.parse(endDate)
        if (start != null && end != null) {
            "${dayFormat.format(start)}-${dayFormat.format(end)} ${monthYearFormat.format(end)}"
        } else {
            "$startDate - $endDate"
        }
    } catch (_: Exception) {
        "$startDate - $endDate"
    }
}

private fun formatCurrency(amount: String?): String {
    if (amount.isNullOrBlank()) return "-"
    return "Rs $amount"
}
