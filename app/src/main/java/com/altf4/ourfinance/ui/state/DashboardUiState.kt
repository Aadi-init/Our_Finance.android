package com.altf4.ourfinance.ui.state

import com.altf4.ourfinance.data.model.DashboardResponse

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val data: DashboardResponse? = null,
    val error: String? = null
)