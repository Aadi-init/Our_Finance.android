package com.altf4.ourfinance.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altf4.ourfinance.R
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.theme.OurFinanceTheme

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomTopBar(
                title = "About",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            //modifier = Modifier.size(15.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Brand Identity
            Icon(
                painter = painterResource(id = R.drawable.ic_logo_foreground),
                contentDescription = null,
                modifier = Modifier.size(180.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Our Finance",
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Version 1.0",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Description Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Our Finance is built specifically for our ALT F4 House to keep our lives simple and transparent.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "It tracks collective monthly bills, expenses split balances, and settlements in real time. No awkward math, no forgotten expenses, just clean data and clear insights so everyone stays perfectly on the same page.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Designed and Developed By:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Arnab Banik",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SocialIcon(R.drawable.ic_facebook, "Facebook") {
                                uriHandler.openUri("https://www.facebook.com/share/1C1F2c8qra/")
                            }
                            SocialIcon(R.drawable.ic_instagram_alt, "Instagram") {
                                uriHandler.openUri("https://www.instagram.com/banik.da?igsh=MTc4YjhvbjBobDBkcg==")
                            }
                            SocialIcon(R.drawable.ic_linkedin, "LinkedIn") {
                                uriHandler.openUri("https://www.linkedin.com/in/arnab-banik-adi?utm_source=share_via&utm_content=profile&utm_medium=member_android")
                            }
                            SocialIcon(R.drawable.ic_github, "GitHub") {
                                uriHandler.openUri("https://github.com/Aadi-init")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialIcon(iconResId: Int, contentDescription: String, onClick: () -> Unit) {
    Icon(
        painter = painterResource(id = iconResId),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(24.dp)
            .clickable { onClick() },
        tint = Color.Unspecified // Social icons usually have their own colors
    )
}

@Preview(showBackground = true, name = "About Light")
@Composable
fun AboutScreenPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        AboutScreen(onBackClick = {})
    }
}

@Preview(showBackground = true, name = "About Dark")
@Composable
fun AboutScreenPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        AboutScreen(onBackClick = {})
    }
}
