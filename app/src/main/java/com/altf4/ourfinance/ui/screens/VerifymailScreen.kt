package com.altf4.ourfinance.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.material3.MaterialTheme
import com.altf4.ourfinance.ui.DisplayText
import com.altf4.ourfinance.ui.OtpBox
import com.altf4.ourfinance.ui.SubmitButton

@Composable
fun VerifymailScreen() {
    // State for each of the 4 digits
    var digit1 by remember { mutableStateOf("") }
    var digit2 by remember { mutableStateOf("") }
    var digit3 by remember { mutableStateOf("") }
    var digit4 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Greeting
        // Top spacing
        Spacer(modifier = Modifier.height(112.dp))
        DisplayText("Verify\nYour Email")
        // Bottom spacing
        Spacer(modifier = Modifier.height(112.dp))

        //2. Text Info.
        Text(
            text = "Enter the 4 digit code sent to your mail",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(5.dp))
        //3. 4-Digit OTP Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // Spreads them out like Figma
        ) {
            OtpBox(value = digit1, onValueChange = { digit1 = it })
            OtpBox(value = digit2, onValueChange = { digit2 = it })
            OtpBox(value = digit3, onValueChange = { digit3 = it })
            OtpBox(value = digit4, onValueChange = { digit4 = it })
        }

        Spacer(modifier = Modifier.height(5.dp))

        //4. Resend Code Button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = { /* TODO: Forgot Password Logic */ }) {
                Text(
                    text = "Resend Code",
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        //5. Verify Button
        SubmitButton("Verify", onClick = { })


    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun VerifymailPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        VerifymailScreen()
    }
}

@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun VerifymailPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        VerifymailScreen()
    }
}