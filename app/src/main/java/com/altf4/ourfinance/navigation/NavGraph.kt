package com.altf4.ourfinance.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.altf4.ourfinance.ui.screens.LoginScreen
import com.altf4.ourfinance.ui.screens.SignupScreen
import com.altf4.ourfinance.ui.screens.DashboardScreen
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.ui.viewmodel.DashboardViewModel
import com.altf4.ourfinance.ui.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    authViewModel: AuthViewModel,
    onGoogleSignInRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authenticatedUser by authViewModel.authenticatedUser.collectAsState()

    // Centralized navigation logic: whenever an authenticated user exists, go to Dashboard
    LaunchedEffect(authenticatedUser) {
        if (authenticatedUser != null) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != Screen.Dashboard.route) {
                Log.d("AuthDebug", "User authenticated: ${authenticatedUser?.email}. Navigating to Dashboard.")
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.SignUp.route,
        modifier = modifier
    ) {
        // --- SIGN UP SCREEN ---
        composable(Screen.SignUp.route) {
            SignupScreen(
                onSignupSuccess = { email ->
                    Log.d("AuthDebug", "Manual signup attempt: $email")
                    if (authViewModel.allowedEmails.contains(email.lowercase().trim())) {
                        val user = GoogleUser(
                            displayName = email.split("@").firstOrNull(),
                            email = email,
                            profilePictureUrl = null,
                            apiParamName = authViewModel.getApiParamName(email)
                        )
                        authViewModel.setAuthenticatedUser(user)
                    } else {
                        Log.w("AuthDebug", "Manual signup denied: $email")
                    }
                },
                onGoogleSignInClick = onGoogleSignInRequested,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // --- LOGIN SCREEN ---
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { email ->
                    Log.d("AuthDebug", "Manual login attempt: $email")
                    if (authViewModel.allowedEmails.contains(email.lowercase().trim())) {
                        val user = GoogleUser(
                            displayName = email.split("@").firstOrNull(),
                            email = email,
                            profilePictureUrl = null,
                            apiParamName = authViewModel.getApiParamName(email)
                        )
                        authViewModel.setAuthenticatedUser(user)
                    }
                },
                onGoogleSignInClick = onGoogleSignInRequested,
                onNavigateToSignup = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    // navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // --- DASHBOARD SCREEN ---
        composable(Screen.Dashboard.route) {
            authenticatedUser?.let { user ->
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    currentUser = user,
                    onInfoClick = { /* Handle info */ },
                    onAddExpenseClick = { /* Handle add expense */ },
                    onAddSettlementClick = { /* Handle settlement */ }
                )
            } ?: run {
                Log.w("AuthDebug", "Dashboard reached without user. Fallback to Signup.")
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}

/**
 * Helper function to handle the logic of verifying a Google User.
 * The navigation is handled by LaunchedEffect in AppNavGraph.
 */
fun handleGoogleSignInResult(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    email: String,
    displayName: String?,
    photoUrl: String?
) {
    Log.d("AuthDebug", "handleGoogleSignInResult for: $email")
    if (authViewModel.allowedEmails.contains(email.lowercase().trim())) {
        val user = GoogleUser(
            displayName = displayName ?: email.split("@").firstOrNull(),
            email = email,
            profilePictureUrl = photoUrl,
            apiParamName = authViewModel.getApiParamName(email)
        )
        authViewModel.setAuthenticatedUser(user)
        
        // Force navigation immediately
        Log.d("AuthDebug", "Navigating to Dashboard from result handler.")
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(0) { inclusive = true }
        }
    } else {
        Log.w("AuthDebug", "Google sign-in email not whitelisted: $email")
    }
}
