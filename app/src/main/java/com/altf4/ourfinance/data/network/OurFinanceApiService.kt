package com.altf4.ourfinance.data.network

import com.altf4.ourfinance.data.model.DashboardResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OurFinanceApiService {

    @GET("macros/s/AKfycbwV50yokyFOsKFQddxWMk6GdB11lfPGIDWOtVFQiSWkwj6bbCDArlfd-a2N-HlW-04U/exec")
    suspend fun getDashboardData(
        @Query("user") username: String
    ): DashboardResponse
}