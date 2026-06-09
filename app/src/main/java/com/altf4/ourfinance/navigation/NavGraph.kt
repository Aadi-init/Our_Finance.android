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
import com.altf4.ourfinance.ui.screens.DashboardScreen
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.ui.viewmodel.DashboardViewModel
import com.altf4.ourfinance.ui.viewmodel.AuthViewModel
import com.altf4.ourfinance.ui.screens.RentinvoiceScreen
import com.altf4.ourfinance.ui.state.DashboardUiState
import com.altf4.ourfinance.ui.state.ExpensesUiState
import com.altf4.ourfinance.ui.viewmodel.ExpensesViewModel
import com.altf4.ourfinance.ui.viewmodel.AddExpenseViewModel
import com.altf4.ourfinance.ui.screens.ExpensesScreen
import com.altf4.ourfinance.ui.screens.AddexpensesScreen
import com.altf4.ourfinance.ui.screens.AddexpensesformScreen
import com.altf4.ourfinance.ui.screens.AddsettlementsScreen
import com.altf4.ourfinance.ui.screens.AddsettlementsformScreen
import com.altf4.ourfinance.ui.screens.EditexpenseentryScreen
import com.altf4.ourfinance.ui.screens.EditsettlemententryScreen
import com.altf4.ourfinance.ui.screens.SettlementsScreen
import com.altf4.ourfinance.ui.state.SettlementsUiState
import com.altf4.ourfinance.ui.viewmodel.SettlementsViewModel
import com.altf4.ourfinance.ui.viewmodel.ThemeViewModel
import com.altf4.ourfinance.ui.screens.AccessibilityScreen
import com.altf4.ourfinance.ui.screens.AboutScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AppNavGraph(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    authViewModel: AuthViewModel,
    expensesViewModel: ExpensesViewModel,
    settlementsViewModel: SettlementsViewModel,
    addExpenseViewModel: AddExpenseViewModel,
    themeViewModel: ThemeViewModel,
    onGoogleSignInRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authenticatedUser by authViewModel.authenticatedUser.collectAsState()

    // Centralized navigation logic: handles navigation AFTER a successful sign-in
    LaunchedEffect(authenticatedUser) {
        if (authenticatedUser != null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            // Only navigate if we are currently on an auth screen or at start
            if (currentRoute == Screen.Login.route || currentRoute == null) {
                Log.d("AuthDebug", "User signed in. Navigating to Dashboard.")
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // --- LOGIN SCREEN ---
        composable(Screen.Login.route) {
            val context = LocalContext.current
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { email ->
                    Log.d("AuthDebug", "Manual login success for: $email")
                },
                onGoogleSignInClick = onGoogleSignInRequested,
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
                    settlementsViewModel = settlementsViewModel,
                    currentUser = user,
                    navController = navController,
                    onInfoClick = {
                        navController.navigate(Screen.RentInvoice.route)
                    },
                    onAddExpenseClick = {
                        navController.navigate(Screen.AddExpense.route)
                    },
                    onAddSettlementClick = {
                        navController.navigate(Screen.AddSettlement.route)
                    }
                )
            }
        }

        // --- RENT INVOICE SCREEN ---
        composable(Screen.RentInvoice.route) {
            val uiState by dashboardViewModel.uiState.collectAsState()

            uiState.data?.let { invoiceData ->
                RentinvoiceScreen(
                    data = invoiceData,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onDownloadClick = { /* Handle download */ }
                )
            }
        }

        // --- EXPENSES SCREEN ---
        composable(Screen.Expenses.route) {
            authenticatedUser?.let { user ->
                ExpensesScreen(
                    viewModel = expensesViewModel,
                    currentUser = user,
                    navController = navController,
                    onAddExpenseClick = {
                        navController.navigate(Screen.AddExpense.route)
                    },
                    onEntryClick = { entry ->
                        navController.navigate("${Screen.EditExpenseEntry.route}/${entry.id}")
                    }
                )
            }
        }

        // --- SETTLEMENTS SCREEN ---
        composable(Screen.Settlements.route) {
            authenticatedUser?.let { user ->
                SettlementsScreen(
                    viewModel = settlementsViewModel,
                    currentUser = user,
                    navController = navController,
                    onAddSettlementClick = {
                        navController.navigate(Screen.AddSettlement.route)
                    },
                    onEntryClick = { entry ->
                        navController.navigate("${Screen.EditSettlementEntry.route}/${entry.id}")
                    }
                )
            }
        }

        // --- ADD SETTLEMENT SCREEN ---
        composable(Screen.AddSettlement.route) {
            authenticatedUser?.let { user ->
                AddsettlementsScreen(
                    currentUser = user.apiParamName,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = { amount, type, person, timestamp ->
                        val encodedType = URLEncoder.encode(type, StandardCharsets.UTF_8.toString())
                        val encodedPerson = URLEncoder.encode(person, StandardCharsets.UTF_8.toString())
                        navController.navigate("${Screen.AddSettlementForm.route}/$amount/$encodedType/$encodedPerson/$timestamp")
                    }
                )
            }
        }

        // --- ADD SETTLEMENT FORM SCREEN ---
        composable(
            route = "${Screen.AddSettlementForm.route}/{amount}/{type}/{person}/{timestamp}",
            arguments = listOf(
                navArgument("amount") { type = NavType.FloatType },
                navArgument("type") { type = NavType.StringType },
                navArgument("person") { type = NavType.StringType },
                navArgument("timestamp") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
            val type = backStackEntry.arguments?.getString("type") ?: "Sent"
            val person = backStackEntry.arguments?.getString("person") ?: ""
            val timestamp = backStackEntry.arguments?.getLong("timestamp") ?: System.currentTimeMillis()

            val isSaving by addExpenseViewModel.isSaving.collectAsState()

            LaunchedEffect(Unit) {
                addExpenseViewModel.saveResult.collect { result ->
                    result.onSuccess {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        navController.popBackStack(Screen.AddSettlement.route, inclusive = true)

                        dashboardViewModel.fetchDashboardData(authenticatedUser?.apiParamName ?: "")
                        settlementsViewModel.fetchSettlements(authenticatedUser?.apiParamName ?: "")
                    }.onFailure {
                        Toast.makeText(context, it.localizedMessage ?: "Error saving settlement", Toast.LENGTH_LONG).show()
                    }
                }
            }

            authenticatedUser?.let { user ->
                AddsettlementsformScreen(
                    initialAmount = amount,
                    initialType = type,
                    initialPerson = person,
                    initialTimestamp = timestamp,
                    currentUser = user,
                    isSaving = isSaving,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = { finalAmount, finalType, finalPerson, finalTimestamp, description ->
                        val fromUser = if (finalType == "Sent") user.apiParamName else finalPerson
                        val toUser = if (finalType == "Sent") finalPerson else user.apiParamName

                        addExpenseViewModel.addSettlement(
                            username = user.apiParamName,
                            from = fromUser,
                            to = toUser,
                            amount = finalAmount,
                            description = description,
                            timestamp = finalTimestamp
                        )
                    }
                )
            }
        }

        // --- EDIT SETTLEMENT ENTRY SCREEN ---
        composable(
            route = "${Screen.EditSettlementEntry.route}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val id = backStackEntry.arguments?.getString("id")

            val entriesState by settlementsViewModel.uiState.collectAsState()
            val entry = entriesState.filteredEntries.find { it.id == id }

            val isSaving by addExpenseViewModel.isSaving.collectAsState()

            LaunchedEffect(Unit) {
                addExpenseViewModel.saveResult.collect { result ->
                    result.onSuccess {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                        dashboardViewModel.fetchDashboardData(authenticatedUser?.apiParamName ?: "")
                        settlementsViewModel.fetchSettlements(authenticatedUser?.apiParamName ?: "")
                    }.onFailure {
                        Toast.makeText(context, it.localizedMessage ?: "Error updating settlement", Toast.LENGTH_LONG).show()
                    }
                }
            }

            if (entry != null) {
                authenticatedUser?.let { user ->
                    EditsettlemententryScreen(
                        entry = entry,
                        currentUser = user,
                        isSaving = isSaving,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSaveClick = { finalAmount, finalType, finalPerson, finalTimestamp, description ->
                            val fromUser = if (finalType == "Sent") user.apiParamName else finalPerson
                            val toUser = if (finalType == "Sent") finalPerson else user.apiParamName

                            val now = SimpleDateFormat("d MMMM yyyy, hh:mm a", Locale.US).format(Date())
                            addExpenseViewModel.updateSettlement(
                                id = entry.id,
                                from = fromUser,
                                to = toUser,
                                amount = finalAmount,
                                description = description,
                                timestamp = finalTimestamp,
                                editorName = user.displayName ?: user.apiParamName,
                                editTime = now
                            )
                        }
                    )
                }
            }
        }

        // --- ADD EXPENSE SCREEN ---
        composable(Screen.AddExpense.route) {
            AddexpensesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveClick = { amount, category, timestamp ->
                    val encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8.toString())
                    navController.navigate("${Screen.AddExpenseForm.route}/$amount/$encodedCategory/$timestamp")
                }
            )
        }

        // --- ADD EXPENSE FORM SCREEN ---
        composable(
            route = "${Screen.AddExpenseForm.route}/{amount}/{category}/{timestamp}",
            arguments = listOf(
                navArgument("amount") { type = NavType.FloatType },
                navArgument("category") { type = NavType.StringType },
                navArgument("timestamp") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
            val category = backStackEntry.arguments?.getString("category") ?: "Groceries"
            val timestamp = backStackEntry.arguments?.getLong("timestamp") ?: System.currentTimeMillis()

            val isSaving by addExpenseViewModel.isSaving.collectAsState()

            LaunchedEffect(Unit) {
                addExpenseViewModel.saveResult.collect { result ->
                    result.onSuccess {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        navController.popBackStack(Screen.AddExpense.route, inclusive = true)

                        dashboardViewModel.fetchDashboardData(authenticatedUser?.apiParamName ?: "")
                        expensesViewModel.fetchExpenses(authenticatedUser?.apiParamName ?: "")
                    }.onFailure {
                        Toast.makeText(context, it.localizedMessage ?: "Error saving expense", Toast.LENGTH_LONG).show()
                    }
                }
            }

            authenticatedUser?.let { user ->
                AddexpensesformScreen(
                    initialAmount = amount,
                    initialCategory = category,
                    initialTimestamp = timestamp,
                    currentUser = user,
                    isSaving = isSaving,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = { finalAmount, finalCategory, finalTimestamp, description ->
                        addExpenseViewModel.addExpense(
                            username = user.apiParamName,
                            amount = finalAmount,
                            category = finalCategory,
                            timestamp = finalTimestamp,
                            description = description
                        )
                    }
                )
            }
        }

        // --- EDIT EXPENSE ENTRY SCREEN ---
        composable(
            route = "${Screen.EditExpenseEntry.route}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val id = backStackEntry.arguments?.getString("id")

            val entriesState by expensesViewModel.uiState.collectAsState()
            val entry = entriesState.allEntries.find { it.id == id }

            val isSaving by addExpenseViewModel.isSaving.collectAsState()

            LaunchedEffect(Unit) {
                addExpenseViewModel.saveResult.collect { result ->
                    result.onSuccess {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                        dashboardViewModel.fetchDashboardData(authenticatedUser?.apiParamName ?: "")
                        expensesViewModel.fetchExpenses(authenticatedUser?.apiParamName ?: "")
                    }.onFailure {
                        Toast.makeText(context, it.localizedMessage ?: "Error updating expense", Toast.LENGTH_LONG).show()
                    }
                }
            }

            if (entry != null) {
                authenticatedUser?.let { user ->
                    EditexpenseentryScreen(
                        entry = entry,
                        currentUser = user,
                        isSaving = isSaving,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSaveClick = { finalAmount, finalCategory, finalTimestamp, description, person ->
                            val now = SimpleDateFormat("d MMMM yyyy, hh:mm a", Locale.US).format(Date())
                            addExpenseViewModel.updateExpense(
                                id = entry.id,
                                username = person,
                                amount = finalAmount,
                                category = finalCategory,
                                timestamp = finalTimestamp,
                                description = description,
                                editorName = user.displayName ?: user.apiParamName,
                                editTime = now
                            )
                        }
                    )
                }
            }
        }

        // --- ACCESSIBILITY SCREEN ---
        composable(Screen.Accessibility.route) {
            authenticatedUser?.let { user ->
                AccessibilityScreen(
                    currentUser = user,
                    themeViewModel = themeViewModel,
                    authViewModel = authViewModel,
                    navController = navController,
                    onLogoutClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // --- ABOUT SCREEN ---
        composable(Screen.About.route) {
            AboutScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

fun handleGoogleSignInResult(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    email: String,
    displayName: String?,
    photoUrl: String?,
    context: android.content.Context
) {
    Log.d("AuthDebug", "handleGoogleSignInResult for: $email")
    authViewModel.handleGoogleLogin(email, displayName, photoUrl, context)
}