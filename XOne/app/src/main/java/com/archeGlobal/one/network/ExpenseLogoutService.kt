package com.archeGlobal.one.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface ExpenseLogoutService {
    @GET("v1/logout")
    fun logout(): Call<ExpenseLogoutResponse>
}

data class ExpenseLogoutResponse(
    val status: Int,
    val message: String?,
)
