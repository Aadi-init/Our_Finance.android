package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val id: String,
    val timestamp: String,
    val header: String,
    val body: String
)

@JsonClass(generateAdapter = true)
data class NotificationsResponse(
    val notifications: List<NotificationItem>
)
