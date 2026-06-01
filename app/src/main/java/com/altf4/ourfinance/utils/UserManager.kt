package com.altf4.ourfinance.utils

import com.altf4.ourfinance.data.model.GoogleUser

object UserManager {
    private val userMap = mutableMapOf<String, GoogleUser>(
        "Arnab" to GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab"),
        "Sadman" to GoogleUser("Sadman Hossain", "sadmanhossainwork@gmail.com", null, "Sadman"),
        "Sabbir" to GoogleUser("Sabbir Islam Tonmoy", "sabbirtonmoy911@gmail.com", null, "Sabbir")
    )

    fun updateUser(user: GoogleUser) {
        userMap[user.apiParamName] = user
    }

    fun getProfilePicture(personName: String): String {
        val user = userMap.values.find { 
            it.apiParamName.equals(personName, ignoreCase = true) || 
            it.displayName?.equals(personName, ignoreCase = true) == true 
        }
        return user?.profilePictureUrl ?: "https://ui-avatars.com/api/?name=$personName&background=22C55E&color=fff"
    }
}
