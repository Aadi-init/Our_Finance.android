package com.altf4.ourfinance

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
import com.altf4.ourfinance.ui.theme.OurFinanceTheme


@Composable
fun SignupScreen() {
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
        // 1. Greeting
        // Top spacing
        Spacer(modifier = Modifier.weight(0.3f))
        DisplayText("Let's\nGet Started")
        // Bottom spacing
        Spacer(modifier = Modifier.weight(0.3f))

        // 2. Input Fields
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
            passwordVisible = passwordVisible, // This state is managed at the top of SignupScreen
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Signup Button
        SubmitButton("Sign Up", onClick = { })

        Spacer(modifier = Modifier.height(24.dp))

        // 4. "OR" Separator
        OrSeparator()

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Google Button (Placeholder for the logic we will add later)
        GoogleButton {  }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Bottom Navigation
        TextButton(onClick = { /* Navigate to Login */ }) {
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
    OurFinanceTheme(darkTheme = false) { // Forces Light Mode
        SignupScreen()
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun SignupPreviewDark() {
    OurFinanceTheme(darkTheme = true) { // Forces Dark Mode
        SignupScreen()
    }
}