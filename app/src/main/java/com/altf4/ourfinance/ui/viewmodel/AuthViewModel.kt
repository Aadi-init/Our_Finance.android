package com.altf4.ourfinance.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.altf4.ourfinance.data.model.GoogleUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPrefs = application.getSharedPreferences("OurFinancePrefs", Context.MODE_PRIVATE)
    private val KEY_LOGGED_IN_USER_EMAIL = "logged_in_user_email"
    private val KEY_LOGGED_IN_USER_NAME = "logged_in_user_name"
    private val KEY_LOGGED_IN_USER_PIC = "logged_in_user_pic"

    private val _authenticatedUser = MutableStateFlow<GoogleUser?>(null)
    val authenticatedUser: StateFlow<GoogleUser?> = _authenticatedUser.asStateFlow()

    init {
        // Restore session on startup
        val savedEmail = sharedPrefs.getString(KEY_LOGGED_IN_USER_EMAIL, null)
        if (savedEmail != null) {
            _authenticatedUser.value = GoogleUser(
                displayName = sharedPrefs.getString(KEY_LOGGED_IN_USER_NAME, ""),
                email = savedEmail,
                profilePictureUrl = sharedPrefs.getString(KEY_LOGGED_IN_USER_PIC, null),
                apiParamName = getApiParamName(savedEmail)
            )
        }
    }

    fun setAuthenticatedUser(user: GoogleUser?) {
        _authenticatedUser.value = user
        if (user != null) {
            sharedPrefs.edit().apply {
                putString(KEY_LOGGED_IN_USER_EMAIL, user.email)
                putString(KEY_LOGGED_IN_USER_NAME, user.displayName)
                putString(KEY_LOGGED_IN_USER_PIC, user.profilePictureUrl)
                apply()
            }
        }
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

    fun logout() {
        _authenticatedUser.value = null
        sharedPrefs.edit().clear().apply()
    }
}
