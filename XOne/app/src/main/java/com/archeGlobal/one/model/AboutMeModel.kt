package com.archeGlobal.one.model

data class AboutMeModel(
    // Basic Information
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val employeeId: String = "",

    // Additional Details (even if currently empty)
    val aadharNumber: String = "",
    val panNumber: String = "",
    val uanNumber: String = "",
    val bloodGroup: String = "",

    // Reporting Structure
    val reportingManager: String = "",
    val divisionalHead: String = "",

    // Work Information
    val department: String = "",
    val designation: String = "",
    val location: String = ""
)
