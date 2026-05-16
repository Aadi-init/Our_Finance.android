package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RentInvoiceBreakdown(
    val rent: Double = 0.0,
    val electricity: Double = 0.0,
    val internet: Double = 0.0,
    val waterFilter: Double = 0.0,
    val househelp: Double = 0.0,
    val others: Double = 0.0,
    val adjustments: Double = 0.0
)