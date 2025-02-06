package com.example.xone.model

data class BusinessCardModel(
    val companyLogo: Int, // Resource ID for logo
    val name: String,
    val designation: String,
    val department: String,
    val email: String,
    val phone: String,
    val location: String,
    val qrCode: String // You might want to generate this dynamically
) 