package com.archeGlobal.one.model

import com.archeGlobal.one.network.Service
import com.archeGlobal.one.network.UserDetails

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
    val sosContact: String? = null,
    val userDetails: UserDetails? = null,
    val greetings: Map<String, List<String>> = emptyMap(),
    val hasReportees: Boolean = false,
    val loginPunchIn: String? = null,
    val loginPunchOut: String? = null,
)
