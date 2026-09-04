package com.altf4.ourfinance.utils

import androidx.compose.runtime.mutableStateMapOf
import com.altf4.ourfinance.data.model.GoogleUser

object UserManager {
    // Reactive mapping architecture triggers automated Compose layout updates globally
    private val userMap = mutableStateMapOf<String, GoogleUser>(
        "Arnab" to GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab"),
        "Sadman" to GoogleUser("Sadman Hossain", "sadmanhossainwork@gmail.com", null, "Sadman"),
        "Sabbir" to GoogleUser("Sabbir Islam Tonmoy", "sabbirtonmoy911@gmail.com", null, "Sabbir")
    )

    fun updateUser(user: GoogleUser) {
        userMap[user.apiParamName] = user
    }

    /**
     * Batch updates roommates' details using a profile lookup map synced from Google Sheets.
     */
    fun syncUserProfiles(profiles: Map<String, String>?) {
        if (profiles == null) return

        // Save directly to offline storage first
        CacheManager.saveCachedProfiles("cached_user_profiles", profiles)

        // Update reactive memory layer for UI rendering
        profiles.forEach { (name, picUrl) ->
            val existing = userMap[name]
            if (existing != null) {
                if (existing.profilePictureUrl != picUrl && picUrl.isNotEmpty()) {
                    userMap[name] = existing.copy(profilePictureUrl = picUrl)
                }
            } else {
                userMap[name] = GoogleUser(
                    displayName = name,
                    email = "${name.lowercase()}@gmail.com",
                    profilePictureUrl = picUrl,
                    apiParamName = name
                )
            }
        }
    }

    /**
     * Extracts the avatar source. Returns either a URL string
     * or fallback to UI Avatars.
     */
    fun getProfilePicture(personName: String): Any {
        val user = userMap.values.find {
            it.apiParamName.equals(personName, ignoreCase = true) ||
                    it.displayName?.split(" ")?.firstOrNull().equals(personName, ignoreCase = true)
        }
        val avatarUrl = user?.profilePictureUrl
        return if (!avatarUrl.isNullOrEmpty()) {
            avatarUrl
        } else {
            "https://ui-avatars.com/api/?name=$personName&background=22C55E&color=fff"
        }
    }
}
