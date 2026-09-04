package com.altf4.ourfinance.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ForgotPassword : Screen("forgot_password")
    object VerifyEmail : Screen("verify_email")
    object NewPassword : Screen("new_password")
    object RentInvoice : Screen("rent_invoice")
    object Expenses : Screen("expenses") {
        const val routeWithArgs = "expenses?highlightId={highlightId}"
    }
    object AddExpense : Screen("add_expense")
    object AddExpenseForm : Screen("add_expense_form")
    object EditExpenseEntry : Screen("edit_expense_entry")
    object Settlements : Screen("settlements") {
        const val routeWithArgs = "settlements?highlightId={highlightId}"
    }
    object AddSettlement : Screen("add_settlement")
    object AddSettlementForm : Screen("add_settlement_form")
    object EditSettlementEntry : Screen("edit_settlement_entry")
    object Accessibility : Screen("accessibility")
    object About : Screen("about")
}
