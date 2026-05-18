package com.altf4.ourfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altf4.ourfinance.data.network.RetrofitClient
import com.altf4.ourfinance.ui.state.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    // Internal mutable state that handles the active pipeline updates
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)

    // Public read-only stream that the Jetpack Compose components listen to
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Reaches across the web to pull fresh row values from the Apps Script engine
     */
    fun fetchDashboardData(username: String) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val response = RetrofitClient.apiService.getDashboardData(username)
                _uiState.value = DashboardUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(
                    e.localizedMessage ?: "Failed to connect to Google Sheets backend"
                )
            }
        }
    }
}