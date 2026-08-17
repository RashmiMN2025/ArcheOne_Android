package com.archeGlobal.one.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpenseTripService {
    @GET("v1/trips/")
    suspend fun getTrips(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
    ): Response<TripsListResponse>

    @GET("v1/trips/team")
    suspend fun getTeamTrips(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
    ): Response<TripsListResponse>

    @GET("v1/projects/options/my")
    suspend fun getProjectOptions(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
    ): Response<ProjectOptionsResponse>

    @GET("v1/trips/options")
    suspend fun getTripOptions(): Response<List<TripOptionResponse>>

    @POST("v1/trips/")
    suspend fun createTrip(@Body request: CreateTripRequest): Response<TripItemResponse>

    @GET("v1/trips/{trip_id}")
    suspend fun getTrip(
        @Path("trip_id") tripId: String,
    ): Response<TripItemResponse>

    @DELETE("v1/trips/{trip_id}")
    suspend fun deleteTrip(
        @Path("trip_id") tripId: String,
    ): Response<DeleteTripResponse>

    @PUT("v1/trips/{trip_id}")
    suspend fun updateTrip(
        @Path("trip_id") tripId: String,
        @Body request: CreateTripRequest,
    ): Response<TripItemResponse>

    @PATCH("v1/trips/{trip_id}/status")
    suspend fun updateTripStatus(
        @Path("trip_id") tripId: String,
        @Body request: TripStatusUpdateRequest,
    ): Response<TripStatusUpdateResponse>

    @GET("v1/assets")
    suspend fun getVehicleAssets(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("category") category: String = "vehicle",
        @Query("vehicle_type") vehicleType: String,
    ): Response<VehicleAssetsResponse>

    @GET("v1/vehicles")
    suspend fun getVehicles(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("type") type: String,
    ): Response<VehicleListResponse>

    @POST("v1/travel-expenses/mileage-rate")
    suspend fun getMileageRate(
        @Body request: MileageRateRequest,
    ): Response<MileageRateResponse>

    @GET("v1/travel-expenses")
    suspend fun getMileageExpenses(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
    ): Response<MileageExpensesResponse>

    @GET("v1/travel-expenses/dashboard-metrics")
    suspend fun getMileageDashboardMetrics(): Response<TeamMileageDashboardMetricsResponse>

    @GET("v1/travel-expenses/team/dashboard-metrics")
    suspend fun getTeamMileageDashboardMetrics(): Response<TeamMileageDashboardMetricsResponse>

    @GET("v1/travel-expenses/team")
    suspend fun getTeamMileageExpenses(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
    ): Response<MileageExpensesResponse>

    @POST("v1/travel-expenses")
    suspend fun createMileageExpense(
        @Body request: CreateMileageExpenseRequest,
    ): Response<Any>

    @PUT("v1/travel-expenses/{expense_id}")
    suspend fun updateMileageExpense(
        @Path("expense_id") expenseId: Int,
        @Body request: CreateMileageExpenseRequest,
    ): Response<Any>

    @PATCH("v1/travel-expenses/{expense_id}/withdraw")
    suspend fun withdrawMileageExpense(
        @Path("expense_id") expenseId: Int,
    ): Response<Any>

    @DELETE("v1/travel-expenses/{expense_id}")
    suspend fun deleteMileageExpense(
        @Path("expense_id") expenseId: Int,
    ): Response<Any>

    @GET("v1/travel-expenses/{expense_id}")
    suspend fun getMileageExpenseDetail(
        @Path("expense_id") expenseId: Int,
    ): Response<MileageExpenseDetailResponse>

    @GET("v1/travel-expenses/notes/{expense_id}")
    suspend fun getMileageExpenseNotes(
        @Path("expense_id") expenseId: Int,
    ): Response<List<MileageExpenseNoteResponse>>

    // Server expects multipart/form-data (expense_id required, notes and file optional).
    @Multipart
    @POST("v1/travel-expenses/notes")
    suspend fun addMileageExpenseNote(
        @Part("expense_id") expenseId: okhttp3.RequestBody,
        @Part("notes") notes: okhttp3.RequestBody? = null,
        @Part file: okhttp3.MultipartBody.Part? = null,
    ): Response<AddMileageExpenseNoteResponse>

    @POST("v1/travel-expenses/{expense_id}/submit")
    suspend fun submitMileageExpense(
        @Path("expense_id") expenseId: Int,
    ): Response<SubmitMileageExpenseResponse>

    @PATCH("v1/travel-expenses/approve/{expense_id}")
    suspend fun approveMileageExpense(
        @Path("expense_id") expenseId: Int,
        @Body request: TravelExpenseApproveRequest,
    ): Response<TravelExpenseActionResponse>

    @PATCH("v1/travel-expenses/reject/{expense_id}")
    suspend fun rejectMileageExpense(
        @Path("expense_id") expenseId: Int,
        @Body request: TravelExpenseRejectRequest,
    ): Response<TravelExpenseActionResponse>
}
