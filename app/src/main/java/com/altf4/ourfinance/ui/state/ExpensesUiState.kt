package com.altf4.ourfinance.ui.state

import com.altf4.ourfinance.data.model.ExpenseEntry

data class ExpensesUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val allEntries: List<ExpenseEntry> = emptyList(),
    val filteredEntries: List<ExpenseEntry> = emptyList(),
    val totalExpense: Double = 0.0,
    val userContribution: Double = 0.0,
    val toBeAdjusted: Double = 0.0,
    val error: String? = null
)