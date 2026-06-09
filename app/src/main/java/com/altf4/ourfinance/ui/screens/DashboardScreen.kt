package com.altf4.ourfinance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.altf4.ourfinance.data.model.NotificationItem
import com.altf4.ourfinance.data.model.DashboardResponse
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.ui.state.DashboardUiState
import com.altf4.ourfinance.ui.viewmodel.DashboardViewModel
import com.altf4.ourfinance.ui.viewmodel.SettlementsViewModel
import com.altf4.ourfinance.data.model.RentInvoiceBreakdown
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.ui.state.SettlementsUiState
import com.altf4.ourfinance.utils.UserManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date
import com.altf4.ourfinance.ui.PillNavigationBar
import com.altf4.ourfinance.ui.NavScreen
import androidx.navigation.NavController
import com.altf4.ourfinance.navigation.Screen
import com.altf4.ourfinance.R
import com.altf4.ourfinance.ui.SyncActionButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private fun formatToFigmaTk(value: Double): String {
    val absValue = java.lang.Math.abs(value)
    val formatter = java.text.NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return if (value < 0) "-Tk. ${formatter.format(absValue)}" else "Tk. ${formatter.format(absValue)}"
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    settlementsViewModel: SettlementsViewModel,
    currentUser: GoogleUser,
    navController: NavController,
    onInfoClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddSettlementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settlementState by settlementsViewModel.uiState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val hasNewNotifications by viewModel.hasNewNotifications.collectAsState()

    var isNotificationViewActive by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = currentUser.apiParamName) {
        UserManager.updateUser(currentUser)
        if (!uiState.isInitialized) {
            viewModel.fetchDashboardData(currentUser.apiParamName)
            settlementsViewModel.fetchSettlements(currentUser.apiParamName)
            viewModel.fetchNotifications(currentUser.apiParamName)
            viewModel.registerFcmToken(currentUser.apiParamName)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,

        bottomBar = {
            PillNavigationBar(
                currentScreen = NavScreen.Dashboard,
                onScreenSelected = { selectedScreen ->
                    when (selectedScreen) {
                        NavScreen.Dashboard -> {
                            if (isNotificationViewActive) isNotificationViewActive = false
                        }
                        NavScreen.Expenses -> {
                            navController.navigate(Screen.Expenses.route) {
                                launchSingleTop = true
                            }
                        }
                        NavScreen.Settlement -> {
                            navController.navigate(Screen.Settlements.route) {
                                launchSingleTop = true
                            }
                        }
                        NavScreen.Accessibility -> {
                            navController.navigate(Screen.Accessibility.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                },
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Generate empty structural data if data has not yet arrived
            val emptyData = DashboardResponse(
                fullName = currentUser.displayName ?: "Loading...",
                rentStatus = "--",
                totalRent = 0.0,
                yourExpense = 0.0,
                contributions = 0.0,
                balance = 0.0,
                settlement = 0.0,
                invoiceBreakdown = RentInvoiceBreakdown(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            )

            val dashboardData = uiState.data ?: emptyData
            val toBeSettled = if (settlementState.isInitialized) settlementState.toBeSettled else dashboardData.settlement

            // Layout renders immediately - no circular progress indicators blocking the view!
            DashboardContent(
                data = dashboardData,
                toBeSettled = toBeSettled,
                currentUser = currentUser,
                isNotificationViewActive = isNotificationViewActive,
                notifications = notifications,
                hasNewNotifications = hasNewNotifications,
                onNotificationClick = {
                    isNotificationViewActive = true
                    viewModel.markNotificationsAsSeen()
                },
                onBackFromNotifications = { isNotificationViewActive = false },
                onClearNotifications = { viewModel.clearNotifications() },
                onInfoClick = onInfoClick,
                onAddExpenseClick = onAddExpenseClick,
                onAddSettlementClick = onAddSettlementClick,
                onSyncClick = {
                    viewModel.fetchDashboardData(currentUser.apiParamName, forceRefresh = true)
                    settlementsViewModel.fetchSettlements(currentUser.apiParamName, forceRefresh = true)
                    viewModel.fetchNotifications(currentUser.apiParamName)
                }
            )
        }
    }
}

@Composable
fun DashboardContent(
    data: DashboardResponse,
    toBeSettled: Double,
    currentUser: GoogleUser,
    isNotificationViewActive: Boolean,
    notifications: List<NotificationItem>,
    hasNewNotifications: Boolean,
    onNotificationClick: () -> Unit,
    onBackFromNotifications: () -> Unit,
    onClearNotifications: () -> Unit,
    onInfoClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddSettlementClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // --- 1. CENTERED PROFILE SECTION ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f)
                .statusBarsPadding()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = UserManager.getProfilePicture(currentUser.apiParamName)
                ),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Hello!",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Text(
                text = currentUser.displayName ?: data.fullName,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // --- 2. ACTIONS ROW (Notification | Sync) ABOVE CARDS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification Button (Left)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .clickable {
                        if (isNotificationViewActive) {
                            onBackFromNotifications()
                        } else {
                            onNotificationClick()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(18.dp)
                )

                if (hasNewNotifications) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.5.dp, end = 7.5.dp)
                            .size(7.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                    )
                }
            }

            // Sync Button (Right)
            SyncActionButton(
                onClick = onSyncClick,
                containerColor = MaterialTheme.colorScheme.outlineVariant,
                contentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }

        if (isNotificationViewActive) {
            NotificationPanel(
                notifications = notifications,
                onBackClick = onBackFromNotifications,
                onClearClick = onClearNotifications,
                modifier = Modifier
                    .weight(2f)
                    .padding(bottom = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // --- 3. YOUR RENT CARD ---
                val breakdown = data.invoiceBreakdown
                val totalCalculatedRent = (breakdown.rent + breakdown.electricity + breakdown.internet +
                        breakdown.waterFilter + breakdown.househelp + breakdown.others + breakdown.adjustments)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Your Rent", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = formatToFigmaTk(totalCalculatedRent),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onInfoClick,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = com.altf4.ourfinance.R.drawable.ic_invoice),
                                    contentDescription = "Invoice Breakdown",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val calendar = java.util.Calendar.getInstance()
                            val monthYearFormatter = java.text.SimpleDateFormat("MMMM yyyy", Locale.US)
                            val dynamicDueDate = "10th ${monthYearFormatter.format(calendar.time)}"

                            Text(
                                text = "Due on $dynamicDueDate",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 1f)
                            )

                            val statusBgColor = when {
                                data.rentStatus.equals("Pending", ignoreCase = true) -> MaterialTheme.colorScheme.error
                                data.rentStatus.equals("Paid", ignoreCase = true) -> MaterialTheme.colorScheme.tertiary
                                data.rentStatus.equals("Outdated", ignoreCase = true) -> MaterialTheme.colorScheme.surfaceContainerHigh
                                else -> MaterialTheme.colorScheme.tertiary
                            }

                            val statusTextColor = if (data.rentStatus.equals("Outdated", ignoreCase = true)) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }

//                            val statusTextColor = MaterialTheme.colorScheme.onPrimary

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(statusBgColor)
                                    .padding(horizontal = 10.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = data.rentStatus,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusTextColor
                                )
                            }
                        }
                    }
                }

                // --- 4. YOUR EXPENSE CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Your Expense", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = formatToFigmaTk(data.yourExpense),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onAddExpenseClick,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = com.altf4.ourfinance.R.drawable.ic_add),
                                    contentDescription = "Add Expenses",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Contributions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 1f))
                                Text(
                                    text = formatToFigmaTk(data.contributions),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Balance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 1f))
                                Text(
                                    text = formatToFigmaTk(data.balance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (data.balance < 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // --- 5. YOUR SETTLEMENT CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("Your Settlement", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                            }

                            IconButton(
                                onClick = onAddSettlementClick,
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = com.altf4.ourfinance.R.drawable.ic_add),
                                    contentDescription = "Add Settlements",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = formatToFigmaTk(toBeSettled),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (toBeSettled < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )

                            RoommateAvatarStack(currentUser = currentUser.apiParamName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationPanel(
    notifications: List<NotificationItem>,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Panel Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onBackClick() }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.inverseOnSurface)
                        .clickable { onClearClick() }
                        .padding(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Clear",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Scrollable Notifications List
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No new notifications",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    notifications.forEach { item ->
                        NotificationItemRow(item)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemRow(item: NotificationItem) {
    val dayOfMonth = try {
        val date = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).parse(item.timestamp)
        if (date != null) {
            Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_MONTH).toString()
        } else {
            ""
        }
    } catch (_: Exception) {
        ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayOfMonth,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = item.header,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = item.body,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun RoommateAvatarStack(currentUser: String) {
    val roommates = when (currentUser) {
        "Arnab" -> listOf("Sadman", "Sabbir")
        "Sadman" -> listOf("Arnab", "Sabbir")
        else -> listOf("Arnab", "Sadman")
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy((-10).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        roommates.forEach { name ->
            Image(
                painter = rememberAsyncImagePainter(
                    model = UserManager.getProfilePicture(name)
                ),
                contentDescription = "Roommate $name",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    //.border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

// ... Preview bindings (MockDashboardData, MockGoogleUser, etc.) remain the same.
public val MockDashboardData = DashboardResponse(
    fullName = "Arnab Banik",
    rentStatus = "Outdated",
    totalRent = 7935.0,
    yourExpense = 972.0,
    contributions = 2073.0,
    balance = (2073.0 / 3.0) - 972.0,
    settlement = -972.0,
    invoiceBreakdown = RentInvoiceBreakdown(
        rent = 5733.0,
        electricity = 486.0,
        internet = 245.0,
        waterFilter = 517.0,
        househelp = 0.0,
        others = 0.0,
        adjustments = -437.0
    )
)

private val MockGoogleUser = GoogleUser(
    displayName = "Arnab Banik",
    email = "arnab.banik299@gmail.com",
    profilePictureUrl = null,
    apiParamName = "Arnab"
)

private val MockNotifications = listOf(
    NotificationItem(
        id = "1",
        timestamp = "05/10/2026 14:30:00",
        header = "Rent Has Been Updated",
        body = "Rent of May 2026 is due on 10th May 2026."
    ),
    NotificationItem(
        id = "2",
        timestamp = "05/04/2026 09:15:00",
        header = "New Settlement Added",
        body = "Sadman added a settlement of Tk. 500."
    )
)

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun DashboardPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        var currentNavScreen by remember { mutableStateOf(NavScreen.Dashboard) }
        Scaffold(
            bottomBar = {
                PillNavigationBar(
                    currentScreen = currentNavScreen,
                    onScreenSelected = { currentNavScreen = it }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                DashboardContent(
                    data = MockDashboardData,
                    toBeSettled = MockDashboardData.settlement,
                    currentUser = MockGoogleUser,
                    isNotificationViewActive = false,
                    notifications = emptyList(),
                    hasNewNotifications = true,
                    onNotificationClick = {},
                    onBackFromNotifications = {},
                    onClearNotifications = {},
                    onInfoClick = {},
                    onAddExpenseClick = {},
                    onAddSettlementClick = {},
                    onSyncClick = {}
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DashboardPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        var currentNavScreen by remember { mutableStateOf(NavScreen.Dashboard) }
        Scaffold(
            bottomBar = {
                PillNavigationBar(
                    currentScreen = currentNavScreen,
                    onScreenSelected = { currentNavScreen = it }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                DashboardContent(
                    data = MockDashboardData,
                    toBeSettled = MockDashboardData.settlement,
                    currentUser = MockGoogleUser,
                    isNotificationViewActive = false,
                    notifications = emptyList(),
                    hasNewNotifications = true,
                    onNotificationClick = {},
                    onBackFromNotifications = {},
                    onClearNotifications = {},
                    onInfoClick = {},
                    onAddExpenseClick = {},
                    onAddSettlementClick = {},
                    onSyncClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Notification Panel Light")
@Composable
fun NotificationPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        Scaffold(
            bottomBar = {
                PillNavigationBar(
                    currentScreen = NavScreen.Dashboard,
                    onScreenSelected = {}
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                DashboardContent(
                    data = MockDashboardData,
                    toBeSettled = MockDashboardData.settlement,
                    currentUser = MockGoogleUser,
                    isNotificationViewActive = true,
                    notifications = MockNotifications,
                    hasNewNotifications = false,
                    onNotificationClick = {},
                    onBackFromNotifications = {},
                    onClearNotifications = {},
                    onInfoClick = {},
                    onAddExpenseClick = {},
                    onAddSettlementClick = {},
                    onSyncClick = {}
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Notification Panel Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun NotificationPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        Scaffold(
            bottomBar = {
                PillNavigationBar(
                    currentScreen = NavScreen.Dashboard,
                    onScreenSelected = {}
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                DashboardContent(
                    data = MockDashboardData,
                    toBeSettled = MockDashboardData.settlement,
                    currentUser = MockGoogleUser,
                    isNotificationViewActive = true,
                    notifications = MockNotifications,
                    hasNewNotifications = false,
                    onNotificationClick = {},
                    onBackFromNotifications = {},
                    onClearNotifications = {},
                    onInfoClick = {},
                    onAddExpenseClick = {},
                    onAddSettlementClick = {},
                    onSyncClick = {}
                )
            }
        }
    }
}
