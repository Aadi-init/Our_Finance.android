package com.altf4.ourfinance.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Converts a raw numeric Double into a standardized Bangladeshi Taka currency visual string.
 */
fun Double.toTkFormat(): String {
    return try {
        // Formats numbers with standard commas (e.g., 12,000.00)
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        "Tk. ${formatter.format(this)}"
    } catch (e: Exception) {
        "Tk. ${this}"
    }
}