package com.altf4.ourfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel : ViewModel() {
    private val _isDarkMode = MutableStateFlow(true) // Default: Dark Mode enabled
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isDynamicIconEnabled = MutableStateFlow(false)
    val isDynamicIconEnabled: StateFlow<Boolean> = _isDynamicIconEnabled.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setDynamicIconEnabled(enabled: Boolean) {
        _isDynamicIconEnabled.value = enabled
    }

    /**
     * Synchronizes the view model state with saved preferences.
     * This should be called during app initialization (e.g., in MainActivity).
     */
    fun syncWithPreferences(isDarkMode: Boolean, isDynamicIconEnabled: Boolean) {
        _isDarkMode.value = isDarkMode
        _isDynamicIconEnabled.value = isDynamicIconEnabled
    }
}
