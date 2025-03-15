package com.archeGlobal.one.model

import com.archeGlobal.one.network.Service

data class UserData(
    val name: String,
    val designation: String,
    val department: String,
    val employeeId: String,
    val email: String,
    val mobile: String,
    val location: String = "",
    val services: List<Service> = emptyList(),
    val profilePic: String? = null,
    val sosContact: String? = null
) 