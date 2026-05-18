package com.altf4.ourfinance.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.altf4.ourfinance.data.model.DashboardResponse
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.ui.state.DashboardUiState
import com.altf4.ourfinance.ui.viewmodel.DashboardViewModel
import com.altf4.ourfinance.data.model.RentInvoiceBreakdown
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import java.util.Locale
import com.altf4.ourfinance.ui.PillNavigationBar
import com.altf4.ourfinance.ui.NavScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Custom high-fidelity currency formatter that matches Figma sign placement exactly: e.g., -Tk. 186.00
 */
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
    currentUser: GoogleUser,
    onInfoClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddSettlementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var currentNavScreen by remember { mutableStateOf(NavScreen.Dashboard) }

    LaunchedEffect(key1 = currentUser.apiParamName) {
        viewModel.fetchDashboardData(currentUser.apiParamName)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,

        bottomBar = {
            PillNavigationBar(
                currentScreen = currentNavScreen,
                onScreenSelected = { selectedScreen ->
                    currentNavScreen = selectedScreen
                    // Handle your navController actions here
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
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        data = state.data,
                        currentUser = currentUser,
                        onInfoClick = onInfoClick,
                        onAddExpenseClick = onAddExpenseClick,
                        onAddSettlementClick = onAddSettlementClick
                    )
                }
                is DashboardUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.fetchDashboardData(currentUser.apiParamName) }) {
                            Text("Retry Sync")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    data: DashboardResponse,
    currentUser: GoogleUser,
    onInfoClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddSettlementClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Crucial: Fixes the white-screen bug in previews
            //.verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = currentUser.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${data.fullName}&background=22C55E&color=fff"
                    ),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp) // Adjusted sizing to match Figma prominent layout hierarchy
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Hello!",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )

                Text(
                    text = currentUser.displayName ?: data.fullName,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
//        // --- FIXED: CENTRALLY ALIGNED FIGMA HEADER SECTION ---
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 55.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), // Clean breathing room right above the nav tray limits
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- 1. YOUR RENT CARD ---
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
                                contentDescription = "Info Breakdown",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
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

                        val isPending = data.rentStatus.equals("Pending", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
                                .padding(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = data.rentStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }

            // --- 2. YOUR EXPENSE CARD ---
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
                                tint = MaterialTheme.colorScheme.onPrimary
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

            // --- 3. YOUR SETTLEMENT CARD ---
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
                                tint = MaterialTheme.colorScheme.onPrimary
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
                            text = formatToFigmaTk(data.settlement),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (data.settlement < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                        )

                        RoommateAvatarStack(currentUser = currentUser.apiParamName)
                    }
                }
            }
        }
    }
}
//        }
//
//        // --- 1. YOUR RENT CARD ---
//        val breakdown = data.invoiceBreakdown
//        val totalCalculatedRent = (breakdown.rent + breakdown.electricity + breakdown.internet +
//                breakdown.waterFilter + breakdown.househelp + breakdown.others + breakdown.adjustments)
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//        ) {
//            Column(modifier = Modifier.padding(15.dp)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Top
//                ) {
//                    Column {
//                        Text("Your Rent", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
//                        Spacer(modifier = Modifier.height(3.dp))
//                        Text(
//                            text = formatToFigmaTk(totalCalculatedRent),
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                    }
//
//                    IconButton(
//                        onClick = onInfoClick,
//                        colors = IconButtonDefaults.iconButtonColors(
//                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                            contentColor = MaterialTheme.colorScheme.onPrimary
//                        ),
//                        modifier = Modifier.size(30.dp)
//                    ) {
//                        Icon(
//                            painter = androidx.compose.ui.res.painterResource(id = com.altf4.ourfinance.R.drawable.ic_invoice),
//                            contentDescription = "Info Breakdown",
//                            modifier = Modifier.size(20.dp),
//                        )
//
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    val calendar = java.util.Calendar.getInstance()
//                    val monthYearFormatter = java.text.SimpleDateFormat("MMMM yyyy", Locale.US)
//                    val dynamicDueDate = "10th ${monthYearFormatter.format(calendar.time)}"
//
//                    Text(
//                        text = "Due on $dynamicDueDate",
//                        fontSize = 12.sp,
//                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 1f)
//                    )
//
//                    val isPending = data.rentStatus.equals("Pending", ignoreCase = true)
//                    Box(
//                        modifier = Modifier
//                            .clip(RoundedCornerShape(50))
//                            .background(if (isPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
//                            .padding(horizontal = 10.dp, vertical = 0.dp)
//                    ) {
//                        Text(
//                            text = data.rentStatus,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.onPrimary
//                        )
//                    }
//                }
//            }
//        }
//
//        // --- 2. YOUR EXPENSE CARD ---
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//        ) {
//            Column(modifier = Modifier.padding(15.dp)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Top
//                ) {
//                    Column {
//                        Text("Your Expense", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
//                        Spacer(modifier = Modifier.height(3.dp))
//                        Text(
//                            text = formatToFigmaTk(data.yourExpense),
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                    }
//
//                    IconButton(
//                        onClick = onAddExpenseClick,
//                        colors = IconButtonDefaults.iconButtonColors(
//                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                            contentColor = MaterialTheme.colorScheme.onPrimary
//                        ),
//                        modifier = Modifier.size(30.dp)
//                    ) {
//                        Icon(
//                            painter = androidx.compose.ui.res.painterResource(id = com.altf4.ourfinance.R.drawable.ic_add),
//                            contentDescription = "Add Expenses",
//                            modifier = Modifier.size(20.dp),
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    Column {
//                        Text("Contributions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 1f))
//                        Spacer(modifier = Modifier.height(0.dp))
//                        Text(
//                            text = formatToFigmaTk(data.contributions),
//                            fontSize = 15.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = MaterialTheme.colorScheme.onSurface
//                        )
//                    }
//
//                    Column(horizontalAlignment = Alignment.End) {
//                        Text("Balance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 1f))
//                        Spacer(modifier = Modifier.height(0.dp))
//                        Text(
//                            text = formatToFigmaTk(data.balance),
//                            fontSize = 15.sp,
//                            fontWeight = FontWeight.Bold,
//                            // FIXED: Mapped to tertiary (green) for negative ledger balance as requested in design sheet
//                            color = if (data.balance < 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
//            }
//        }
//
//        // --- 3. YOUR SETTLEMENT CARD ---
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//        ) {
//            Column(modifier = Modifier.padding(15.dp)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Top
//                ) {
//                    Column {
//                        Text("Your Settlement", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
//                    }
//
//                    IconButton(
//                        onClick = onAddSettlementClick,
//                        colors = IconButtonDefaults.iconButtonColors(
//                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                            contentColor = MaterialTheme.colorScheme.onPrimary
//                        ),
//                        modifier = Modifier.size(30.dp)
//                    ) {
//                        Icon(
//                            painter = androidx.compose.ui.res.painterResource(id = com.altf4.ourfinance.R.drawable.ic_add),
//                            contentDescription = "Add Settlements",
//                            modifier = Modifier.size(20.dp),
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                // FIXED: Embedded clean right-hand edge stacked flow matching Figma perfectly
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.Bottom
//                ) {
//                    // Left Side: Amount text displays baseline aligned on the left
//                    Text(
//                        text = formatToFigmaTk(data.settlement),
//                        fontSize = 22.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = if (data.settlement < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
//                    )
//
//                    // Right Side: Avatars display right-aligned without nested Box wrappers
//                    RoommateAvatarStack(currentUser = currentUser.apiParamName)
//                }
//            }
//        }
//    }
//}

@Composable
fun RoommateAvatarStack(currentUser: String) {
    val roommates = when (currentUser) {
        "Arnab" -> listOf("Sadman", "Sabbir")
        "Sadman" -> listOf("Arnab", "Sabbir")
        else -> listOf("Arnab", "Sadman")
    }

    // FIXED: Uses negative row spacing layout to force pixel-perfect overlap depth
    Row(
        horizontalArrangement = Arrangement.spacedBy((-10).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        roommates.forEach { name ->
            Image(
                painter = rememberAsyncImagePainter(
                    model = "https://ui-avatars.com/api/?name=$name&background=3D3D3D&color=FFF&size=128"
                ),
                contentDescription = "Roommate $name",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

// --- MOCK OBJECTS FOR PREVIEW INJECTION ---
private val MockDashboardData = DashboardResponse(
    fullName = "Arnab Banik",
    rentStatus = "Pending",
    totalRent = 7935.0,
    yourExpense = 972.0,
    contributions = 2073.0,
    balance = -186.0,
    settlement = -972.0,
    invoiceBreakdown = RentInvoiceBreakdown(
        rent = 5733.0,
        electricity = 486.0,
        internet = 245.0,
        waterFilter = 517.0,
        househelp = 0.0,
        others = 0.0,
        adjustments = 437.0
    )
)

private val MockGoogleUser = GoogleUser(
    displayName = "Arnab Banik",
    email = "arnab.banik299@gmail.com",
    profilePictureUrl = null,
    apiParamName = "Arnab"
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
                    currentUser = MockGoogleUser,
                    onInfoClick = {},
                    onAddExpenseClick = {},
                    onAddSettlementClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode")
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
                    currentUser = MockGoogleUser,
                    onInfoClick = {},
                    onAddExpenseClick = {},
                    onAddSettlementClick = {}
                )
            }
        }
    }
}