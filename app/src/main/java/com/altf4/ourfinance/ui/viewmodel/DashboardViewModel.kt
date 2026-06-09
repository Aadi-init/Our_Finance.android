package com.altf4.ourfinance.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.altf4.ourfinance.data.model.NotificationItem
import com.altf4.ourfinance.data.network.RetrofitClient
import com.altf4.ourfinance.ui.state.DashboardUiState
import com.altf4.ourfinance.utils.UserManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("OurFinancePrefs", Context.MODE_PRIVATE)
    private val KEY_LAST_CLEARED_ID = "last_cleared_notification_id"
    private val KEY_LAST_REGISTERED_TOKEN = "last_registered_fcm_token"

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _hasNewNotifications = MutableStateFlow(false)
    val hasNewNotifications: StateFlow<Boolean> = _hasNewNotifications.asStateFlow()

    private var lastSeenCount = 0
    private var allNotificationsFetched: List<NotificationItem> = emptyList()

    fun fetchDashboardData(username: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Fetch dashboard data
                val dashboardResponse = RetrofitClient.apiService.getDashboardData(username)

                // Sync any incoming user profiles to UserManager
                UserManager.syncUserProfiles(dashboardResponse.userProfiles)

                // Fetch expenses to calculate real-time totals for the current month
                val expensesResponse = RetrofitClient.apiService.getExpenses(username = username)

                // Also sync profiles from expenses response if available
                UserManager.syncUserProfiles(expensesResponse.userProfiles)

                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                val currentMonthExpenses = expensesResponse.entries.filter { entry ->
                    val entryDate = parseTimestamp(entry.timestamp)
                    val cal = Calendar.getInstance().apply { time = entryDate }
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }

                val totalExpense = currentMonthExpenses.sumOf { it.amount }
                val userContribution = currentMonthExpenses
                    .filter { it.person.equals(username, ignoreCase = true) }
                    .sumOf { it.amount }

                val individualShare = totalExpense / 3.0
                val balance = individualShare - userContribution

                // Update response with calculated values for current month
                val updatedResponse = dashboardResponse.copy(
                    yourExpense = userContribution,
                    contributions = totalExpense,
                    balance = balance
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    data = updatedResponse,
                    error = null
                )
                fetchNotifications(username)
            } catch (e: Exception) {
                Log.e("PerformanceAudit", "Fetch Dashboard Error", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isInitialized = true,
                    error = e.localizedMessage ?: "Failed to connect to Google Sheets backend"
                )
            }
        }
    }

    private fun parseTimestamp(timestamp: String): Date {
        return try {
            SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).parse(timestamp) ?: Date()
        } catch (_: Exception) {
            Date()
        }
    }

    fun fetchNotifications(username: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getNotifications(username = username)
                allNotificationsFetched = response.notifications

                applyNotificationFilter()

                if (_notifications.value.size > lastSeenCount) {
                    _hasNewNotifications.value = true
                }
            } catch (e: Exception) {
                // Silent fail for notifications
            }
        }
    }

    private fun applyNotificationFilter() {
        val lastClearedId = sharedPrefs.getString(KEY_LAST_CLEARED_ID, "")

        if (lastClearedId.isNullOrEmpty()) {
            _notifications.value = allNotificationsFetched
        } else {
            val index = allNotificationsFetched.indexOfFirst { it.id == lastClearedId }
            if (index == -1) {
                _notifications.value = allNotificationsFetched
            } else {
                _notifications.value = allNotificationsFetched.subList(0, index)
            }
        }
    }

    fun markNotificationsAsSeen() {
        lastSeenCount = _notifications.value.size
        _hasNewNotifications.value = false
    }

    fun registerFcmToken(username: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            val lastToken = sharedPrefs.getString(KEY_LAST_REGISTERED_TOKEN, "")
            if (token != lastToken) {
                viewModelScope.launch {
                    try {
                        val response = RetrofitClient.apiService.updateFcmToken(username = username, token = token)
                        if (response.status == "success") {
                            sharedPrefs.edit().putString(KEY_LAST_REGISTERED_TOKEN, token).apply()
                        }
                    } catch (e: Exception) {
                        Log.e("FCM", "Failed to update token on server", e)
                    }
                }
            }
        }
    }

    fun clearNotifications() {
        if (allNotificationsFetched.isNotEmpty()) {
            val newestId = allNotificationsFetched.first().id
            sharedPrefs.edit().putString(KEY_LAST_CLEARED_ID, newestId).apply()
        }
        _notifications.value = emptyList()
        lastSeenCount = 0
        _hasNewNotifications.value = false
    }
}