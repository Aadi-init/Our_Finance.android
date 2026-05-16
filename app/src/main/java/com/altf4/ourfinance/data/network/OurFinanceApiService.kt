package com.altf4.ourfinance.data.network

import com.altf4.ourfinance.data.model.DashboardResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OurFinanceApiService {

    @GET("macros/s/AKfycbyCkjx-7J6GUsE3MDO4RF8a16QLiKs4xzlSuvpDEqQrREBxVY_Bf6bZb6F6XFj3z3YN/exec")
    suspend fun getDashboardData(
        @Query("user") username: String
    ): DashboardResponse
}