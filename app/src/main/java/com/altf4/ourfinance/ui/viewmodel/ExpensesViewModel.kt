package com.altf4.ourfinance.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altf4.ourfinance.data.model.ExpenseEntry
import com.altf4.ourfinance.data.network.RetrofitClient
import com.altf4.ourfinance.ui.state.ExpensesUiState
import com.altf4.ourfinance.utils.UserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.altf4.ourfinance.data.model.ExpensesResponse
import com.altf4.ourfinance.utils.CacheManager

class ExpensesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    private var allRawEntries: List<ExpenseEntry> = emptyList()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance()[Calendar.MONTH])
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance()[Calendar.YEAR])
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _filterType = MutableStateFlow("All Entries")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

//    fun fetchExpenses(currentUser: String, forceRefresh: Boolean = false) {
//        viewModelScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true)
//
//            try {
//                val response = RetrofitClient.apiService.getExpenses(username = currentUser)
//
//                // Sync roommate profiles to UserManager
//                UserManager.syncUserProfiles(response.userProfiles)
//
//                allRawEntries = response.entries
//                updateFilteredState(currentUser)
//            } catch (e: Exception) {
//                Log.e("PerformanceAudit", "Fetch Expenses Error", e)
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    isInitialized = true,
//                    error = e.localizedMessage ?: "Failed to fetch expense data"
//                )
//            }
//        }
//    }

    fun fetchExpenses(currentUser: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // 1. INSTANT CACHE LOAD
            val cachedResponse = CacheManager.getCachedData<ExpensesResponse>("expenses_$currentUser")
            if (cachedResponse != null) {
                allRawEntries = cachedResponse.entries
                updateFilteredState(currentUser, isLoading = true)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            // 2. BACKGROUND NETWORK FETCH
            try {
                val response = RetrofitClient.apiService.getExpenses(username = currentUser)

                // Save fresh data
                CacheManager.saveCacheData("expenses_$currentUser", response)
                UserManager.syncUserProfiles(response.userProfiles)

                allRawEntries = response.entries
                updateFilteredState(currentUser, isLoading = false)
            } catch (e: Exception) {
                Log.e("PerformanceAudit", "Fetch Expenses Error", e)
                if (allRawEntries.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isInitialized = true,
                        error = e.localizedMessage ?: "Failed to fetch expense data"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun setDateFilter(month: Int, year: Int, currentUser: String) {
        _selectedMonth.value = month
        _selectedYear.value = year
        updateFilteredState(currentUser)
    }

    fun setFilterType(type: String, currentUser: String) {
        _filterType.value = type
        updateFilteredState(currentUser)
    }

    private fun updateFilteredState(currentUser: String, isLoading: Boolean = false) {
        val filteredByDate = allRawEntries.filter { entry ->
            val entryDate = parseTimestamp(entry.timestamp)
            val cal = Calendar.getInstance().apply { time = entryDate }
            (cal[Calendar.MONTH] == _selectedMonth.value) && (cal[Calendar.YEAR] == _selectedYear.value)
        }

        val totalExpense = filteredByDate.sumOf { it.amount }
        val userContribution = filteredByDate
            .filter { it.person == currentUser }
            .sumOf { it.amount }

        val individualShare = totalExpense / 3.0
        val toBeAdjusted = individualShare - userContribution

        val filteredEntries = if (_filterType.value == "Your Entries") {
            filteredByDate.filter { it.person == currentUser }
        } else {
            filteredByDate
        }

        _uiState.value = _uiState.value.copy(
            isLoading = isLoading,
            isInitialized = true,
            allEntries = filteredByDate,
            filteredEntries = filteredEntries,
            totalExpense = totalExpense,
            userContribution = userContribution,
            toBeAdjusted = toBeAdjusted,
            error = null
        )
    }

    private fun parseTimestamp(timestamp: String): Date {
        return try {
            SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).parse(timestamp) ?: Date()
        } catch (_: Exception) {
            Date()
        }
    }
}