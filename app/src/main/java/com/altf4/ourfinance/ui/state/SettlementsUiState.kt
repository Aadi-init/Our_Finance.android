package com.altf4.ourfinance.ui.state

import com.altf4.ourfinance.data.model.TransactionEntry

data class SettlementsUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val totalSent: Double = 0.0,
    val totalReceived: Double = 0.0,
    val toBeSettled: Double = 0.0,
    val peerBalances: Map<String, Double> = emptyMap(),
    val filteredEntries: List<TransactionEntry> = emptyList(),
    val error: String? = null
)