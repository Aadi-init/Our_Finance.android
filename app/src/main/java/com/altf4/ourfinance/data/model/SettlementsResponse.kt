package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SettlementsResponse(
    val totalSent: Double,
    val totalReceived: Double,
    val toBeSettled: Double,
    val peerBalances: Map<String, Double>, // e.g., "Sadman" -> 691.0
    val entries: List<TransactionEntry>
)
