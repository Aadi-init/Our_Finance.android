package com.altf4.ourfinance.data.network

import com.altf4.ourfinance.data.model.BaseResponse
import com.altf4.ourfinance.data.model.DashboardResponse
import com.altf4.ourfinance.data.model.ExpenseEntry
import com.altf4.ourfinance.data.model.ExpensesResponse
import com.altf4.ourfinance.data.model.SettlementsResponse
import com.altf4.ourfinance.data.model.DownloadInvoiceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OurFinanceApiService {

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun getDashboardData(
        @Query("user") username: String
    ): DashboardResponse

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun getExpenses(
        @Query("action") action: String = "getExpenses",
        @Query("user") username: String
    ): ExpensesResponse

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun getSettlements(
        @Query("action") action: String = "getSettlements",
        @Query("user") username: String
    ): SettlementsResponse

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun getNotifications(
        @Query("action") action: String = "getNotifications",
        @Query("user") username: String
    ): com.altf4.ourfinance.data.model.NotificationsResponse

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun updateFcmToken(
        @Query("action") action: String = "updateFcmToken",
        @Query("user") username: String,
        @Query("token") token: String
    ): BaseResponse

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun addExpense(
        @Query("action") action: String = "addExpense",
        @Query("id") id: String,
        @Query("user") username: String,
        @Query("amount") amount: Double,
        @Query("category") category: String,
        @Query("timestamp") timestamp: Long,
        @Query("description") description: String,
        @Query("isEdited") isEdited: Boolean,
        @Query("editCredential") editCredential: String
    ): BaseResponse

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun updateExpense(
        @Query("action") action: String = "updateExpense",
        @Query("id") id: String,
        @Query("user") username: String,
        @Query("amount") amount: Double,
        @Query("category") category: String,
        @Query("timestamp") timestamp: Long,
        @Query("description") description: String,
        @Query("isEdited") isEdited: Boolean,
        @Query("editCredential") editCredential: String,
        @Query("editorName") editorName: String,
        @Query("editTime") editTime: String
    ): BaseResponse

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun addSettlement(
        @Query("action") action: String = "addSettlement",
        @Query("id") id: String,
        @Query("user") username: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double,
        @Query("description") description: String,
        @Query("timestamp") timestamp: Long,
        @Query("isEdited") isEdited: Boolean,
        @Query("editCredential") editCredential: String
    ): BaseResponse

    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun updateSettlement(
        @Query("action") action: String = "updateSettlement",
        @Query("id") id: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double,
        @Query("description") description: String,
        @Query("timestamp") timestamp: Long,
        @Query("isEdited") isEdited: Boolean,
        @Query("editCredential") editCredential: String,
        @Query("editorName") editorName: String,
        @Query("editTime") editTime: String
    ): BaseResponse

    // --- NEW DOWNLOAD INVOICE ENDPOINT ---
    @GET("macros/s/AKfycbxFqubE8w1S03NiKiWO2BG7o0r7pqtx54xpbNMY4m6wGfYtf-o9crKg5RMSPBSKDoMw/exec")
    suspend fun downloadInvoice(
        @Query("action") action: String = "downloadInvoice",
        @Query("user") username: String
    ): DownloadInvoiceResponse
}