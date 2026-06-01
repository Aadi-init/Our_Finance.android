package com.altf4.ourfinance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altf4.ourfinance.ui.CustomInputField
import com.altf4.ourfinance.ui.DisplayText
import com.altf4.ourfinance.ui.GoogleButton
import com.altf4.ourfinance.ui.OrSeparator
import com.altf4.ourfinance.ui.SubmitButton
import com.altf4.ourfinance.ui.theme.OurFinanceTheme

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        DisplayText("Hey,\nWelcome Back")
        Spacer(modifier = Modifier.weight(0.3f))

        CustomInputField(
            value = email,
            onValueChange = { email = it },
            label = "Email Id",
            leadingIcon = Icons.Default.Email
        )
        Spacer(modifier = Modifier.height(12.dp))

        CustomInputField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = "Forgot Password?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SubmitButton("Login", onClick = { onLoginSuccess(email) })

        Spacer(modifier = Modifier.height(24.dp))

        OrSeparator()

        Spacer(modifier = Modifier.height(24.dp))

        GoogleButton(onClick = onGoogleSignInClick)

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToSignup) {
            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("Don't Have an Account? ")
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)) {
                    append("Sign Up")
                }
            })
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LoginPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        LoginScreen(
            onLoginSuccess = {},
            onGoogleSignInClick = {},
            onNavigateToSignup = {},
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
        LoginScreen(
            onLoginSuccess = {},
            onGoogleSignInClick = {},
            onNavigateToSignup = {},
            onForgotPasswordClick = {}
        )
    }
}
