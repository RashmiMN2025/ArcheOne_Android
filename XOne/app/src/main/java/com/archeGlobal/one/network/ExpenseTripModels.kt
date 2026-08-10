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
    @SerializedName("user")
    val user: EmployeeResponse? = null,
    val employee: EmployeeResponse? = null,
    val project: TripProjectResponse? = null,
    @SerializedName("project_code")
    val projectCode: String? = null,
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
    val approval: ApprovalDetailResponse?,
    val issuance: IssuanceDetailResponse?,
)

data class ApprovalDetailResponse(
    val approver: EmployeeResponse?,
    val date: String?,
    val comment: String?,
)

data class IssuanceDetailResponse(
    val issuer: EmployeeResponse?,
    val date: String?,
    val comment: String?,
)

data class EmployeeResponse(
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    val id: Int?,
    val email: String?,
    @SerializedName("reporting_manager")
    val reportingManager: ReportingManagerResponse?,
)

data class ReportingManagerResponse(
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
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

data class TripStatusUpdateRequest(
    val approved_amount: Double?,
    val comment: String,
    val status: String,
)

data class TripStatusUpdateResponse(
    val message: String? = null,
    val data: TripItemResponse? = null,
)

data class ProjectOption(
    val id: Int,
    val code: String,
    @SerializedName("customer_id") val customerId: String? = null,
    @SerializedName("so_number") val soNumber: String? = null,
    @SerializedName("customer_name") val customerName: String? = null,
    val status: String? = null
)

data class MileageRateRequest(
    @SerializedName("vehicle_id") val vehicleId: Int,
    @SerializedName("vehicle_ownership_type") val vehicleOwnershipType: String,
    @SerializedName("vehicle_type") val vehicleType: String,
)

data class TeamMileageDashboardMetricsResponse(
    @SerializedName("total_distance") val totalDistance: String? = null,
    @SerializedName("total_claim_amount") val totalClaimAmount: String? = null,
    @SerializedName("total_approved_amount") val totalApprovedAmount: String? = null,
    @SerializedName("pending_count") val pendingCount: Int? = null,
    @SerializedName("total_carbon_emission") val totalCarbonEmission: String? = null,
)

data class MileageExpenseRoutePoint(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

data class CreateMileageExpenseRequest(
    @SerializedName("from_date") val fromDate: String,
    @SerializedName("to_date") val toDate: String,
    val route: List<MileageExpenseRoutePoint>,
    @SerializedName("vehicle_id") val vehicleId: Int?,
    @SerializedName("vehicle_ownership_type") val vehicleOwnershipType: String,
    @SerializedName("vehicle_type") val vehicleType: String,
    val distance: Double,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("project_id") val projectId: Int,
    @SerializedName("duration_seconds") val durationSeconds: Int,
)

data class MileageExpenseRecord(
    val id: Int,
    val route: List<MileageExpenseRoutePoint>,
    @SerializedName("from_date") val fromDate: String,
    @SerializedName("to_date") val toDate: String,
    val vehicle: String,
    @SerializedName("vehicle_type") val vehicleType: String,
    val status: String,
    val amount: String,
    val distance: String,
    @SerializedName("customer_name") val customerName: String,
    @SerializedName("project_id") val projectId: Int,
)

data class MileageExpensesResponse(
    val total: Int,
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    val data: List<MileageExpenseRecord>,
)

data class MileageExpenseItemUi(
    val id: String,
    val customerName: String,
    val date: String,
    val startPoint: String,
    val endPoint: String,
    val type: String,
    val vehicle: String,
    val amount: String,
    val distance: String,
    val status: String,
)

fun MileageExpenseRecord.toMileageExpenseItemUi(): MileageExpenseItemUi {
    val routePoints = route.filter { it.name.isNotBlank() }
    val startPoint = routePoints.firstOrNull()?.name ?: "-"
    val endPoint = routePoints.lastOrNull()?.name ?: "-"
    val displayDate = if (fromDate.isNotBlank() && toDate.isNotBlank()) {
        "$fromDate to $toDate"
    } else {
        "-"
    }

    return MileageExpenseItemUi(
        id = "MLG-$id",
        customerName = customerName.takeIf { it.isNotBlank() } ?: "-",
        date = displayDate,
        startPoint = startPoint,
        endPoint = endPoint,
        type = vehicleType.replaceFirstChar { it.uppercase() }.takeIf { it.isNotBlank() } ?: "Mileage",
        vehicle = vehicle.replaceFirstChar { it.uppercase() }.takeIf { it.isNotBlank() } ?: "-",
        amount = amount.takeIf { it.isNotBlank() } ?: "0.00",
        distance = distance.takeIf { it.isNotBlank() } ?: "0",
        status = status.replaceFirstChar { it.uppercase() }.takeIf { it.isNotBlank() } ?: "Pending",
    )
}

data class MileageRateResponse(
    @SerializedName("mileage_rate") val mileageRate: String?,
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
    val approvedBy: String,
    val approvalDate: String,
    val approvalComment: String,
    val issuedBy: String,
    val issuanceDate: String,
    val issuanceComment: String,
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

data class TripOptionResponse(
    val id: Int,
    @SerializedName("trip_id") val tripId: String,
    val destination: String,
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
    val reportingManager: String,
    val firstAdvanceRequestedAmount: String,
    val firstAdvanceRequestedAmountRaw: String,
    val firstAdvanceStatus: String,
    val firstAdvanceIssuedAmount: String,
    val status: String,
)

fun TripItemResponse.toTravelRequestItemUi(): TravelRequestItemUi {
    val traveler = user ?: employee
    val projectCodeValue = projectCode?.takeIf { it.isNotBlank() } ?: project?.code?.takeIf { it.isNotBlank() } ?: "-"
    val reportingManagerName = traveler?.reportingManager?.let { manager ->
        listOfNotNull(manager.firstName, manager.lastName).joinToString(" ").trim().takeIf { it.isNotBlank() } ?: manager.email
    } ?: "-"

    return TravelRequestItemUi(
        requestId = id.toString(),
        tripId = tripId?.takeIf { it.isNotBlank() } ?: id.toString(),
        tripCode = tripCode?.takeIf { it.isNotBlank() } ?: "-",
        projectId = projectCodeValue,
        destination = destination,
        description = description?.takeIf { it.isNotBlank() } ?: "-",
        startDate = dateFormate(startDate),
        endDate = dateFormate(endDate),
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
            val approverName = advance.approval?.approver?.let { approver ->
                listOfNotNull(approver.firstName, approver.lastName).joinToString(" ").trim().takeIf { it.isNotBlank() } ?: approver.email
            } ?: "-"
            val issuerName = advance.issuance?.issuer?.let { issuer ->
                listOfNotNull(issuer.firstName, issuer.lastName).joinToString(" ").trim().takeIf { it.isNotBlank() } ?: issuer.email
            } ?: "-"
            
            AdvanceRequestUi(
                id = advance.id,
                note = advance.note?.takeIf { it.isNotBlank() } ?: "-",
                requestedAmount = advance.requestedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
                requestedAmountRaw = advance.requestedAmount.orEmpty(),
                endDate = advance.endDate?.takeIf { it.isNotBlank() } ?: "-",
                estimatedAmount = advance.estimatedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
                status = advance.status?.takeIf { it.isNotBlank() } ?: "-",
                issuedAmount = advance.issuedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
                approvedBy = approverName,
                approvalDate = advance.approval?.date?.takeIf { it.isNotBlank() }?.let { dateFormate(it) } ?: "-",
                approvalComment = advance.approval?.comment?.takeIf { it.isNotBlank() } ?: "-",
                issuedBy = issuerName,
                issuanceDate = advance.issuance?.date?.takeIf { it.isNotBlank() }?.let { dateFormate(it) } ?: "-",
                issuanceComment = advance.issuance?.comment?.takeIf { it.isNotBlank() } ?: "-",
            )
        },
        employeeName = listOfNotNull(traveler?.firstName, traveler?.lastName).joinToString(" ").takeIf { it.isNotBlank() } ?: "-",
        employeeEmail = traveler?.email ?: "-",
        reportingManager = reportingManagerName,
        firstAdvanceRequestedAmount = advances.firstOrNull()?.requestedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        firstAdvanceRequestedAmountRaw = advances.firstOrNull()?.requestedAmount.orEmpty(),
        firstAdvanceStatus = advances.firstOrNull()?.status?.takeIf { it.isNotBlank() } ?: "-",
        firstAdvanceIssuedAmount = advances.firstOrNull()?.issuedAmount?.takeIf { it.isNotBlank() }?.let { formatCurrency(it) } ?: "-",
        status = status,
    )
}

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

private fun dateFormate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return ""
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}

private fun formatCurrency(amount: String?): String {
    if (amount.isNullOrBlank()) return "-"
    return "Rs $amount"
}

data class VehicleAssetsResponse(
    val total: Int,
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    val data: List<VehicleAssetItem>,
)

data class VehicleAssetItem(
    val id: Int,
    val category: String?,
    @SerializedName("fuel_type") val fuelType: String?,
    @SerializedName("asset_code") val assetCode: String?,
    @SerializedName("make_model") val makeModel: String?,
    @SerializedName("vehicle_cc") val vehicleCc: Int?,
    @SerializedName("vehicle_type") val vehicleType: String?,
    val operator: VehicleAssetOperator?,
)

data class VehicleAssetOperator(
    val id: Int,
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
)

data class VehicleListResponse(
    val total: Int,
    val page: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    val vehicles: List<VehicleItem>,
)

data class VehicleItem(
    val id: Int,
    val type: String?,
    @SerializedName("engine_cc") val engineCc: Int?,
    @SerializedName("registration_number") val registrationNumber: String?,
    @SerializedName("make_model") val makeModel: String?,
    @SerializedName("fuel_type") val fuelType: String?,
)
