package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val id: String,
    val timestamp: String,
    val header: String,
    val body: String,
    val type: String? = null,     // e.g., "EXPENSE", "SETTLEMENT", "RENT"
    val targetId: String? = null // ID of the entry to highlight
)

@JsonClass(generateAdapter = true)
data class NotificationsResponse(
    val notifications: List<NotificationItem>
)
