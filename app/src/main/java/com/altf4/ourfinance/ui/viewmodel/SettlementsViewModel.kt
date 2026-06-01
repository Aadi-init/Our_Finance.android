package com.altf4.ourfinance.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.altf4.ourfinance.data.model.TransactionEntry
import com.altf4.ourfinance.data.network.RetrofitClient
import com.altf4.ourfinance.ui.state.SettlementsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettlementsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettlementsUiState())
    val uiState: StateFlow<SettlementsUiState> = _uiState.asStateFlow()

    private var allRawEntries: List<TransactionEntry> = emptyList()

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance()[Calendar.MONTH])
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance()[Calendar.YEAR])
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _filterPerson = MutableStateFlow("All")
    val filterPerson: StateFlow<String> = _filterPerson.asStateFlow()

    private val _filterType = MutableStateFlow("All")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    fun fetchSettlements(currentUser: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val response = RetrofitClient.apiService.getSettlements(username = currentUser)
                allRawEntries = response.entries
                updateFilteredState(currentUser, response)
            } catch (e: Exception) {
                Log.e("PerformanceAudit", "Fetch Settlements Error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    error = e.localizedMessage ?: "Failed to fetch settlement data"
                )
            }
        }
    }

    fun setDateFilter(month: Int, year: Int, currentUser: String) {
        _selectedMonth.value = month
        _selectedYear.value = year
        updateFilteredState(currentUser)
    }

    fun setPersonFilter(person: String, currentUser: String) {
        _filterPerson.value = person
        updateFilteredState(currentUser)
    }

    fun setTypeFilter(type: String, currentUser: String) {
        _filterType.value = type
        updateFilteredState(currentUser)
    }

    private fun updateFilteredState(currentUser: String, response: com.altf4.ourfinance.data.model.SettlementsResponse? = null) {
        val filteredByDate = allRawEntries.filter { entry ->
            val entryDate = parseTimestamp(entry.timestamp)
            val cal = Calendar.getInstance().apply { time = entryDate }
            (cal[Calendar.MONTH] == _selectedMonth.value) && (cal[Calendar.YEAR] == _selectedYear.value)
        }

        val totalSent = allRawEntries.filter { it.from == currentUser }.sumOf { it.amount }
        val totalReceived = allRawEntries.filter { it.to == currentUser }.sumOf { it.amount }
        val toBeSettled = totalSent - totalReceived

        val peers = listOf("Arnab", "Sadman", "Sabbir").filter { it != currentUser }
        val peerBalances = peers.associateWith { peer ->
            val sentToPeer = allRawEntries.filter { it.from == currentUser && it.to == peer }.sumOf { it.amount }
            val receivedFromPeer = allRawEntries.filter { it.from == peer && it.to == currentUser }.sumOf { it.amount }
            sentToPeer - receivedFromPeer
        }

        val filteredEntries = filteredByDate.filter { entry ->
            val personMatch = if (_filterPerson.value == "All") true
            else entry.from == _filterPerson.value || entry.to == _filterPerson.value

            val typeMatch = when (_filterType.value) {
                "Sent" -> entry.from == currentUser
                "Received" -> entry.to == currentUser
                else -> true
            }
            personMatch && typeMatch
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isInitialized = true,
            totalSent = totalSent,
            totalReceived = totalReceived,
            toBeSettled = toBeSettled,
            peerBalances = peerBalances,
            filteredEntries = filteredEntries,
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