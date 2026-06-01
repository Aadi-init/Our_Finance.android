package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TransactionEntry(
    val id: String,
    val timestamp: String,
    val from: String,       // Who gave the money?
    val to: String,         // Who received the money?
    val amount: Double,
    val description: String,
    val isEdited: Boolean,
    val editCredential: List<EditHistory>
)
