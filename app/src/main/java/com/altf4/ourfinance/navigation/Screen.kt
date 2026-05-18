package com.altf4.ourfinance.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Dashboard : Screen("dashboard")
    object ForgotPassword : Screen("forgot_password")
    object VerifyEmail : Screen("verify_email")
    object NewPassword : Screen("new_password")
}
