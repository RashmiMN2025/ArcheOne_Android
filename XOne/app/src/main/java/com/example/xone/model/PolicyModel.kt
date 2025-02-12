package com.example.xone.model

data class PolicyModel(
    val title: String = "Company Policies",
    val policies: List<Policy> = listOf(
        Policy("Travel Reimbursement Policy"),
        Policy("POSH Policy"),
        Policy("Revised Shift Allowance Policy"),
        Policy("Netcon IT Asset Policy"),
        Policy("Netcon Anti-Bribery Anti-Corruption Policy"),
        Policy("Leave and Attendance Policy"),
        Policy("Employee Referral Policy"),
        Policy("Employee Loan Policy"),
        Policy("Employee Gift Policy")
    )
) {
    data class Policy(
        val name: String,
        val isDownloaded: Boolean = false
    )
} 