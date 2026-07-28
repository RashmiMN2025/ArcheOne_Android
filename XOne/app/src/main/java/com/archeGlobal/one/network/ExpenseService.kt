package com.archeGlobal.one.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpenseService {
    @GET("v1/expenses")
    suspend fun getExpenses(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("status") statuses: List<String>? = null,
    ): Response<ExpensesListResponse>

    @GET("v1/expenses/list")
    suspend fun getSubmittedExpenses(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 10,
        @Query("view") view: String? = null,
    ): Response<SubmittedExpensesListResponse>

    @GET("v1/expenses/{expense_id}")
    suspend fun getExpense(
        @Path("expense_id") expenseId: String,
    ): Response<ExpenseDetailResponse>

    @GET("v1/users/me")
    suspend fun getCurrentUserProfile(): Response<ExpenseUserProfileResponse>

    @PATCH("v1/expenses/{expense_id}/submit")
    suspend fun submitExpense(
        @Path("expense_id") expenseId: String,
        @Body request: ExpenseSubmitRequest,
    ): Response<ExpenseSubmitResponse>

    @DELETE("v1/expenses/{expense_id}")
    suspend fun deleteExpense(
        @Path("expense_id") expenseId: String,
    ): Response<DeleteExpenseResponse>

    @GET("v1/users/options")
    suspend fun getUserOptions(): Response<List<ExpenseUserOption>>

    @PATCH("v1/expenses/{expense_id}/split")
    suspend fun splitExpense(
        @Path("expense_id") expenseId: String,
        @Body request: SplitExpenseRequest,
    ): Response<SplitExpenseResponse>

    @Multipart
    @POST("v1/expenses")
    suspend fun uploadExpense(
        @Part file: MultipartBody.Part,
        @Part("filename") filename: RequestBody? = null,
    ): Response<UploadExpenseResponse>
}
