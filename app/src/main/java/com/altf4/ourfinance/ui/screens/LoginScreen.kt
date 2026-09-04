package com.altf4.ourfinance.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altf4.ourfinance.R
import com.altf4.ourfinance.ui.CustomInputField
import com.altf4.ourfinance.ui.GoogleButton
import com.altf4.ourfinance.ui.OrSeparator
import com.altf4.ourfinance.ui.SubmitButton
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LoginContent(
        allowedEmails = authViewModel.allowedEmails,
        onLoginClick = { email, password ->
            coroutineScope.launch {
                val error = authViewModel.loginWithPassword(email, password)
                if (error == null) {
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(email)
                } else {
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            }
        },
        onGoogleSignInClick = onGoogleSignInClick,
        onForgotPasswordClick = onForgotPasswordClick
    )
}

@Composable
fun LoginContent(
    allowedEmails: Set<String>,
    onLoginClick: suspend (String, String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val screenHeight = maxHeight
        val isSmallScreen = screenHeight < 700.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Redesigned Top Portion ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isSmallScreen) 1.2f else 1f)
                    .clip(RoundedCornerShape(bottomStart = 35.dp, bottomEnd = 35.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(top = if (isSmallScreen) 40.dp else 80.dp, bottom = if (isSmallScreen) 30.dp else 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(if (isSmallScreen) 100.dp else 140.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontSize = if (isSmallScreen) 14.sp else 16.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))) {
                                append("Hey there!\n")
                            }
                            withStyle(style = SpanStyle(fontSize = if (isSmallScreen) 14.sp else 16.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))) {
                                append("Lets get  ")
                            }
                            withStyle(style = SpanStyle(fontSize = if (isSmallScreen) 14.sp else 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)) {
                                append("Our Finance")
                            }
                            withStyle(style = SpanStyle(fontSize = if (isSmallScreen) 14.sp else 16.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))) {
                                append("  sorted.")
                            }
                        },
                        textAlign = TextAlign.Center,
                        lineHeight = if (isSmallScreen) 18.sp else 22.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isSmallScreen) 16.dp else 32.dp))

            // Lower portion continues with consistent padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isSmallScreen) 2f else 1.5f)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CustomInputField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Id",
                    leadingIcon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 12.dp))

                CustomInputField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onVisibilityToggle = { passwordVisible = !passwordVisible }
                )
                Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 10.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = onForgotPasswordClick) {
                        Text(
                            text = "Forgot Password?",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 10.dp))

                if (isLoggingIn) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    SubmitButton("Login", onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "Please enter your email and password.", Toast.LENGTH_SHORT).show()
                        } else if (!allowedEmails.contains(email.trim().lowercase())) {
                            Toast.makeText(context, "Access Denied: Email not whitelisted.", Toast.LENGTH_LONG).show()
                        } else {
                            isLoggingIn = true
                            coroutineScope.launch {
                                onLoginClick(email, password)
                                isLoggingIn = false
                            }
                        }
                    })
                }

                Spacer(modifier = Modifier.height(if (isSmallScreen) 10.dp else 20.dp))

                OrSeparator()

                Spacer(modifier = Modifier.height(if (isSmallScreen) 10.dp else 20.dp))

                GoogleButton(onClick = onGoogleSignInClick)

                Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LoginPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        LoginContent(
            allowedEmails = setOf("test@example.com"),
            onLoginClick = { _, _ -> },
            onGoogleSignInClick = {},
            onForgotPasswordClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoginPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        LoginContent(
            allowedEmails = setOf("test@example.com"),
            onLoginClick = { _, _ -> },
            onGoogleSignInClick = {},
            onForgotPasswordClick = {}
        )
    }
}
