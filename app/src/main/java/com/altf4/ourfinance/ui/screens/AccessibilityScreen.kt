package com.altf4.ourfinance.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.altf4.ourfinance.R
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.navigation.Screen
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.NavScreen
import com.altf4.ourfinance.ui.PillNavigationBar
import com.altf4.ourfinance.ui.ScaleableSwitch
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.ui.viewmodel.ThemeViewModel

@Composable
fun AccessibilityScreen(
    currentUser: GoogleUser,
    themeViewModel: ThemeViewModel,
    navController: NavController,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("OurFinancePrefs", Context.MODE_PRIVATE) }
    
    val darkModeEnabled by themeViewModel.isDarkMode.collectAsState()
    var notificationsEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("push_notifications_enabled", true)) 
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomTopBar(
                title = "Accessibility",
                actions = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.outlineVariant)
                            .padding(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "V 1.0",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        bottomBar = {
            PillNavigationBar(
                currentScreen = NavScreen.Accessibility,
                onScreenSelected = { screen ->
                    when (screen) {
                        NavScreen.Dashboard -> {
                            navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        }
                        NavScreen.Expenses -> {
                            navController.navigate(Screen.Expenses.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        NavScreen.Settlement -> {
                            navController.navigate(Screen.Settlements.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        else -> {}
                    }
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 2. Account Profile & Security Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Row 1: Profile Information
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Handle Profile Click */ }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = currentUser.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${currentUser.apiParamName}&background=22C55E&color=fff"
                            ),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.displayName ?: "User",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = currentUser.email,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            painter = painterResource(id = R.drawable.ic_go),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Row 2: Security Configuration
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Handle Change Password */ }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lock),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Change Password",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.ic_go),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // --- 3. App Preferences ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Row 1: Notifications
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_notification),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Notifications",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        ScaleableSwitch(
                            checked = notificationsEnabled,
                            onCheckedChange = { 
                                notificationsEnabled = it 
                                sharedPrefs.edit().putBoolean("push_notifications_enabled", it).apply()
                            },
                            scale = 0.9f // You can change this value to resize the switch!
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    // Row 2: Dark Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_dark_mode),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Dark Mode",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        ScaleableSwitch(
                            checked = darkModeEnabled,
                            onCheckedChange = { themeViewModel.toggleDarkMode(it) },
                            scale = 0.9f // You can change this value to resize the switch!
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- 4. Logout Button ---
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "Logout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Accessibility Light")
@Composable
fun AccessibilityScreenPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    OurFinanceTheme(darkTheme = false) {
        AccessibilityScreen(
            currentUser = mockUser,
            themeViewModel = ThemeViewModel(),
            navController = NavController(androidx.compose.ui.platform.LocalContext.current),
            onLogoutClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Accessibility Dark")
@Composable
fun AccessibilityScreenPreviewDark() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    OurFinanceTheme(darkTheme = true) {
        AccessibilityScreen(
            currentUser = mockUser,
            themeViewModel = ThemeViewModel(),
            navController = NavController(androidx.compose.ui.platform.LocalContext.current),
            onLogoutClick = {}
        )
    }
}
