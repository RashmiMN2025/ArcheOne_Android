package com.archeGlobal.one.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ExpenseAuthService {
    @POST("auth/v1/entra/login")
    suspend fun entraLogin(
        @Body request: EntraLoginRequest,
    ): Response<EntraLoginResponse>
}
