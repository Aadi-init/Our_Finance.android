package com.altf4.ourfinance.data.model

data class GoogleUser(
    val displayName: String?,
    val email: String,
    val profilePictureUrl: String?,
    val apiParamName: String // "Sadman", "Arnab", or "Sabbir"
)