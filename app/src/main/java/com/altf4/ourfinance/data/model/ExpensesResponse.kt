package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExpensesResponse(
    val totalExpense: Double,
    val userContribution: Double,
    val toBeAdjusted: Double,
    val entries: List<ExpenseEntry>,
    val userProfiles: Map<String, String>? = null // Dynamic CDN Mapping Cache Injection
)
