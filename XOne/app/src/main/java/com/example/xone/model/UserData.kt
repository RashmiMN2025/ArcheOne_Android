package com.example.xone.model

data class UserData(
    val name: String,
    val designation: String,
    val department: String,
    val employeeId: String,
    val email: String,
    val mobile: String,
    val location: String = ""
) 