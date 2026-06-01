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
import com.altf4.ourfinance.ui.CustomInputField
import com.altf4.ourfinance.ui.DisplayText
import com.altf4.ourfinance.ui.GoogleButton
import com.altf4.ourfinance.ui.OrSeparator
import com.altf4.ourfinance.ui.SubmitButton
import com.altf4.ourfinance.ui.theme.OurFinanceTheme

@Composable
fun SignupScreen(
    onSignupSuccess: (String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        DisplayText("Let's\nGet Started")
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

        CustomInputField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(32.dp))

        SubmitButton("Sign Up", onClick = { onSignupSuccess(email) })

        Spacer(modifier = Modifier.height(24.dp))

        OrSeparator()

        Spacer(modifier = Modifier.height(24.dp))

        GoogleButton(onClick = onGoogleSignInClick)

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text(text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append("Already have an account? ")
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)) {
                    append("Login")
                }
            })
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun SignupPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        SignupScreen(
            onSignupSuccess = {},
            onGoogleSignInClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SignupPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        SignupScreen(
            onSignupSuccess = {},
            onGoogleSignInClick = {},
            onNavigateToLogin = {}
        )
    }
}
