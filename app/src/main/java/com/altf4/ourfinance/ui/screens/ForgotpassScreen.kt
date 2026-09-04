package com.altf4.ourfinance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altf4.ourfinance.ui.CustomInputField
import com.altf4.ourfinance.ui.DisplayText
import com.altf4.ourfinance.ui.SubmitButton
import com.altf4.ourfinance.ui.theme.OurFinanceTheme

@Composable
fun ForgotpassScreen() {
    var email by remember { mutableStateOf("") }

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
        DisplayText("Forgot\nPassword")
        // Bottom spacing
        Spacer(modifier = Modifier.weight(1f))

        //2. Text Info.
        Text(
            text = "We will send a code on your mail",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        //3. Custom Input Field
        CustomInputField(
            value = email,
            onValueChange = { email = it },
            label = "Email Id",
            leadingIcon = Icons.Default.Email
        )
        Spacer(modifier = Modifier.height(20.dp))

        //4. Send Mail Button
        SubmitButton("Send Mail", onClick = { })
        
        Spacer(modifier = Modifier.weight(0.5f))
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun ForgotpassPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        ForgotpassScreen()
    }
}

@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun ForgotpassPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        ForgotpassScreen()
    }
}