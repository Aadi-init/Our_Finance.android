package com.altf4.ourfinance.data.network

import com.altf4.ourfinance.data.model.BaseResponse
import com.altf4.ourfinance.data.model.DashboardResponse
import com.altf4.ourfinance.data.model.ExpenseEntry
import com.altf4.ourfinance.data.model.ExpensesResponse
import com.altf4.ourfinance.data.model.SettlementsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OurFinanceApiService {

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
    suspend fun getDashboardData(
        @Query("user") username: String
    ): DashboardResponse

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
    suspend fun getExpenses(
        @Query("action") action: String = "getExpenses",
        @Query("user") username: String
    ): ExpensesResponse

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
    suspend fun getSettlements(
        @Query("action") action: String = "getSettlements",
        @Query("user") username: String
    ): SettlementsResponse

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
    suspend fun getNotifications(
        @Query("action") action: String = "getNotifications",
        @Query("user") username: String
    ): com.altf4.ourfinance.data.model.NotificationsResponse

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
    suspend fun updateFcmToken(
        @Query("action") action: String = "updateFcmToken",
        @Query("user") username: String,
        @Query("token") token: String
    ): BaseResponse

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
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

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
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

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
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

    @GET("macros/s/AKfycbytiC_6lYC-dxcbs-8meuRBtIUXCf2wZil_Fo-NT6EgkJE0nky1EHYiJ6NC92HhyrgP/exec")
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
}