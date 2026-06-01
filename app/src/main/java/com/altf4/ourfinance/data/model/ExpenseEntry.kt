package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

/**
 * Represents a flat, single chronological edit event timeline node.
 */
@JsonClass(generateAdapter = true)
data class EditHistory(
    val name: String,
    val time: String
)

/**
 * Represents a single expense entry row from the 'Raw Data' Google Sheet.
 */
@JsonClass(generateAdapter = true)
data class ExpenseEntry(
    val id: String,
    val timestamp: String,
    val person: String,       // E.g., "Sadman", "Arnab", "Sabbir"
    val description: String,
    val amount: Double,
    val category: String,
    val isEdited: Boolean,    // Triggers the red dot in the UI
    val editCredential: List<EditHistory> // The chronological JSON Array
)