package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExpensesResponse(
    val totalExpense: Double,
    val userContribution: Double,
    val toBeAdjusted: Double,
    val entries: List<ExpenseEntry>
)
