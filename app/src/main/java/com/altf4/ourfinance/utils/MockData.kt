package com.altf4.ourfinance.utils

import com.altf4.ourfinance.data.model.DashboardResponse
import com.altf4.ourfinance.data.model.RentInvoiceBreakdown
import com.altf4.ourfinance.data.model.GoogleUser

val MockRentInvoiceBreakdown = RentInvoiceBreakdown(
    rent = 25000.0,
    electricity = 1200.0,
    internet = 1000.0,
    waterFilter = 500.0,
    househelp = 3000.0,
    others = 200.0,
    adjustments = -500.0
)

val MockDashboardData = DashboardResponse(
    fullName = "John Doe",
    rentStatus = "Paid",
    totalRent = 30400.0,
    yourExpense = 10133.33,
    contributions = 30400.0,
    balance = (30400.0 / 3.0) - 10133.33,
    settlement = 0.0,
    invoiceBreakdown = MockRentInvoiceBreakdown
)

val MockGoogleUser = GoogleUser(
    displayName = "John Doe",
    email = "john.doe@example.com",
    profilePictureUrl = "https://example.com/photo.jpg",
    apiParamName = "john"
)
