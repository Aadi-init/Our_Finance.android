package com.altf4.ourfinance.ui.state

import com.altf4.ourfinance.data.model.DashboardResponse

sealed interface DashboardUiState {
    object Loading : DashboardUiState

    data class Success(val data: DashboardResponse) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}