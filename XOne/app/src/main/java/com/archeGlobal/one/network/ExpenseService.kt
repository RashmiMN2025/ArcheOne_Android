package com.archeGlobal.one.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ExpenseService {
    @GET("v1/expenses")
    suspend fun getExpenses(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("status") statuses: List<String>? = null,
    ): Response<ExpensesListResponse>

    @Multipart
    @POST("v1/expenses")
    suspend fun uploadExpense(
        @Part file: MultipartBody.Part,
        @Part("filename") filename: RequestBody? = null,
    ): Response<UploadExpenseResponse>
}
