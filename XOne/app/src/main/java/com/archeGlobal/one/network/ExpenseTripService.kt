package com.archeGlobal.one.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpenseTripService {
    @GET("v1/trips/")
    suspend fun getTrips(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
    ): Response<TripsListResponse>

    @GET("v1/projects/options/my")
    suspend fun getProjectOptions(): Response<ProjectOptionsResponse>

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

    @GET("v1/assets")
    suspend fun getVehicleAssets(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("category") category: String = "vehicle",
        @Query("vehicle_type") vehicleType: String,
    ): Response<VehicleAssetsResponse>

    @POST("v1/travel-expenses/mileage-rate")
    suspend fun getMileageRate(
        @Body request: MileageRateRequest,
    ): Response<MileageRateResponse>

    @GET("v1/travel-expenses")
    suspend fun getMileageExpenses(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
    ): Response<MileageExpensesResponse>

    @POST("v1/travel-expenses")
    suspend fun createMileageExpense(
        @Body request: CreateMileageExpenseRequest,
    ): Response<Any>
}
