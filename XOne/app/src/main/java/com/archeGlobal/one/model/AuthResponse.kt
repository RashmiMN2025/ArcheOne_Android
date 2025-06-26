package com.archeGlobal.one.model

data class AuthResponse(
    val message: String,
    val token: String,
    val email: String,
    val employeeId: String,
    val mobilePhone: String
)
