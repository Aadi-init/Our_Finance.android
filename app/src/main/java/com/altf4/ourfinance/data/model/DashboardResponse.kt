package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DashboardResponse(
    val fullName: String,
    val rentStatus: String,
    val totalRent: Double,
    val yourExpense: Double,
    val contributions: Double,
    val balance: Double,
    val settlement: Double,
    val invoiceBreakdown: RentInvoiceBreakdown,
    val userProfiles: Map<String, String>? = null // Dynamic CDN Mapping Cache Injection
)