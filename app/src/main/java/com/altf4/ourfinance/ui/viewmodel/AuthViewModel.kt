package com.altf4.ourfinance.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.altf4.ourfinance.data.model.GoogleUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {
    private val _authenticatedUser = MutableStateFlow<GoogleUser?>(null)
    val authenticatedUser: StateFlow<GoogleUser?> = _authenticatedUser.asStateFlow()

    fun setAuthenticatedUser(user: GoogleUser?) {
        _authenticatedUser.value = user
    }

    // Whitelisted users
    val allowedEmails = setOf(
        "arnab.banik299@gmail.com",
        "sadmanhossainwork@gmail.com",
        "sabbirtonmoy911@gmail.com"
    )

    fun getApiParamName(email: String): String {
        return when (email.lowercase().trim()) {
            "sadmanhossainwork@gmail.com" -> "Sadman"
            "arnab.banik299@gmail.com" -> "Arnab"
            "sabbirtonmoy911@gmail.com" -> "Sabbir"
            else -> "Arnab"
        }
    }
}
