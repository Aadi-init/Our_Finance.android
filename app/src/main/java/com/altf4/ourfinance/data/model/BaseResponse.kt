package com.altf4.ourfinance.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseResponse(
    val status: String,
    val message: String? = null
)
