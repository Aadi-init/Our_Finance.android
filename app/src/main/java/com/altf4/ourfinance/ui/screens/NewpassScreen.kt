package com.altf4.ourfinance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.altf4.ourfinance.ui.CustomInputField
import com.altf4.ourfinance.ui.DisplayText
import com.altf4.ourfinance.ui.SubmitButton
import com.altf4.ourfinance.ui.theme.OurFinanceTheme

@Composable
fun NewpassScreen() {
    var password by remember { mutableStateOf("") }
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
        Spacer(modifier = Modifier.weight(1f))
        DisplayText("Create New\nPassword")
        // Bottom spacing
        Spacer(modifier = Modifier.weight(1f))

        // 2. Input Fields
        CustomInputField(
            value = password,
            onValueChange = { password = it },
            label = "New Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )
        Spacer(modifier = Modifier.height(12.dp))

        CustomInputField(
            value = password,
            onValueChange = { password = it },
            label = "Confirm Password",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )
        Spacer(modifier = Modifier.height(20.dp))

        //3. Save Button
        SubmitButton("Save", onClick = { })
        
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun NewpassPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        NewpassScreen()
    }
}

@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun NewpassPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        NewpassScreen()
    }
}