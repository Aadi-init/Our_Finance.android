package com.altf4.ourfinance

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.altf4.ourfinance.navigation.AppNavGraph
import com.altf4.ourfinance.navigation.handleGoogleSignInResult
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.ui.viewmodel.DashboardViewModel
import com.altf4.ourfinance.ui.viewmodel.AuthViewModel
import com.altf4.ourfinance.ui.viewmodel.ExpensesViewModel
import com.altf4.ourfinance.ui.viewmodel.SettlementsViewModel
import com.altf4.ourfinance.ui.viewmodel.AddExpenseViewModel
import com.altf4.ourfinance.ui.viewmodel.ThemeViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val expensesViewModel: ExpensesViewModel by viewModels()
    private val settlementsViewModel: SettlementsViewModel by viewModels()
    private val addExpenseViewModel: AddExpenseViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val credentialManager = CredentialManager.create(this)

        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()

            OurFinanceTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                
                AppNavGraph(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel,
                    authViewModel = authViewModel,
                    expensesViewModel = expensesViewModel,
                    settlementsViewModel = settlementsViewModel,
                    addExpenseViewModel = addExpenseViewModel,
                    themeViewModel = themeViewModel,
                    onGoogleSignInRequested = {
                        triggerGoogleSignIn(credentialManager) { email, displayName, photoUrl ->
                            Log.d("AuthDebug", "Sign-in success for: $email")
                            handleGoogleSignInResult(navController, authViewModel, email, displayName, photoUrl)
                        }
                    }
                )
            }
        }
    }

    private fun triggerGoogleSignIn(
        credentialManager: CredentialManager,
        onSuccess: (String, String?, String?) -> Unit
    ) {
        // This is the Client ID for the Web Application in Google Cloud Console
        val webClientId = "460922006999-l14t1l1bvsh9luee806a9f6kvu24cjls.apps.googleusercontent.com"

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                Log.d("AuthDebug", "Starting Credential Manager request...")
                val result = credentialManager.getCredential(
                    context = this@MainActivity,
                    request = request
                )

                val credential = result.credential
                Log.d("AuthDebug", "Credential received. Type: ${credential.type}")

                val googleIdTokenCredential = when {
                    credential is GoogleIdTokenCredential -> credential
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        GoogleIdTokenCredential.createFrom(credential.data)
                    }
                    else -> null
                }

                if (googleIdTokenCredential != null) {
                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName
                    val profilePic = googleIdTokenCredential.profilePictureUri?.toString()

                    Log.d("AuthDebug", "Google ID Token Email: $email")

                    if (authViewModel.allowedEmails.contains(email.lowercase().trim())) {
                        onSuccess(email, displayName, profilePic)
                    } else {
                        Log.w("AuthDebug", "Whitelist check failed for: $email")
                        Toast.makeText(this@MainActivity, "Access Denied: $email is not whitelisted.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.w("AuthDebug", "Received unexpected credential type: ${credential.type}")
                    Toast.makeText(this@MainActivity, "Sign-in failed: Unexpected credential type", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("AuthError", "Credential Manager Error: ${e.message}", e)
                val errorMessage = when (e.message) {
                    "No credentials available" -> "No Google accounts found or configuration mismatch."
                    else -> e.localizedMessage ?: "Unknown Error"
                }
                Toast.makeText(this@MainActivity, "Sign-in failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }
}
