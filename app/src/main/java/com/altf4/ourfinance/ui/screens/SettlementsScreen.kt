package com.altf4.ourfinance.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.altf4.ourfinance.R
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.data.model.TransactionEntry
import com.altf4.ourfinance.data.model.EditHistory
import com.altf4.ourfinance.navigation.Screen
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.NavScreen
import com.altf4.ourfinance.ui.PillNavigationBar
import com.altf4.ourfinance.ui.SyncActionButton
import com.altf4.ourfinance.ui.state.SettlementsUiState
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.ui.viewmodel.SettlementsViewModel
import com.altf4.ourfinance.utils.UserManager
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade

private fun formatToFigmaTk(value: Double): String {
    val absValue = kotlin.math.abs(value)
    val formatter = java.text.NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return if (value < 0) "-Tk. ${formatter.format(absValue)}" else "Tk. ${formatter.format(absValue)}"
}

@Composable
fun SettlementsScreen(
    viewModel: SettlementsViewModel,
    currentUser: GoogleUser,
    navController: NavController,
    onAddSettlementClick: () -> Unit,
    onEntryClick: (TransactionEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val filterPerson by viewModel.filterPerson.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    LaunchedEffect(currentUser.apiParamName) {
        if (!uiState.isInitialized) {
            viewModel.fetchSettlements(currentUser.apiParamName)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PillNavigationBar(
                currentScreen = NavScreen.Settlement,
                onScreenSelected = { selectedScreen ->
                    when (selectedScreen) {
                        NavScreen.Dashboard -> {
                            navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        }
                        NavScreen.Expenses -> {
                            navController.navigate(Screen.Expenses.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                        NavScreen.Accessibility -> {
                            navController.navigate(Screen.Accessibility.route) {
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
        ) {
            SettlementsHeader(
                state = uiState,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                currentUser = currentUser,
                onDateSelected = { m, y -> viewModel.setDateFilter(m, y, currentUser.apiParamName) },
                onRefreshClick = { viewModel.fetchSettlements(currentUser.apiParamName, forceRefresh = true) }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                SettlementsContent(
                    state = uiState,
                    currentUser = currentUser,
                    filterPerson = filterPerson,
                    filterType = filterType,
                    onPersonFilterChange = { viewModel.setPersonFilter(it, currentUser.apiParamName) },
                    onTypeFilterChange = { viewModel.setTypeFilter(it, currentUser.apiParamName) },
                    onAddSettlementClick = onAddSettlementClick,
                    onEntryClick = onEntryClick,
                    bottomPadding = paddingValues.calculateBottomPadding()
                )
            }
        }
    }
}

@Composable
fun SettlementsHeader(
    state: SettlementsUiState,
    selectedMonth: Int,
    selectedYear: Int,
    currentUser: GoogleUser,
    onDateSelected: (Int, Int) -> Unit,
    onRefreshClick: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, selectedMonth)
        set(Calendar.YEAR, selectedYear)
    }
    val monthName = SimpleDateFormat("MMM yyyy", Locale.US).format(calendar.time)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Settlements",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                SyncActionButton(onClick = onRefreshClick)

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                        .clickable {
                            val datePickerDialog = DatePickerDialog(
                                context,
                                { _, year, month, _ -> onDateSelected(month, year) },
                                selectedYear,
                                selectedMonth,
                                1
                            )
                            datePickerDialog.show()
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = monthName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(text = "To Be Settled", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
        Text(
            text = formatToFigmaTk(state.toBeSettled),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = if (state.toBeSettled >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val peers = listOf("Arnab", "Sadman", "Sabbir").filter { it != currentUser.apiParamName }

            peers.forEach { peerName ->
                val balance = state.peerBalances[peerName] ?: 0.0
                Column(horizontalAlignment = if (peerName == peers.first()) Alignment.Start else Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(model = UserManager.getProfilePicture(peerName)),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = peerName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Text(
                        text = formatToFigmaTk(balance),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SettlementsContent(
    state: SettlementsUiState,
    currentUser: GoogleUser,
    filterPerson: String,
    filterType: String,
    onPersonFilterChange: (String) -> Unit,
    onTypeFilterChange: (String) -> Unit,
    onAddSettlementClick: () -> Unit,
    onEntryClick: (TransactionEntry) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. SCROLLABLE SETTLEMENTS LIST (Occupies full space behind overlays)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(
                top = 60.dp, // Ensures top item starts below floating filter bar
                bottom = bottomPadding + 20.dp, // Transparent scrolling behind navigation bar
                start = 10.dp,
                end = 10.dp
            )
        ) {
            items(state.filteredEntries) { entry ->
                TransactionItem(entry = entry, currentUserName = currentUser.apiParamName, onClick = { onEntryClick(entry) })
            }
        }

        // 2. TRANSLUCENT FLOATING FILTER HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f) // Glassmorphic translucency
                )
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterPill(
                        label = "Person :",
                        value = filterPerson,
                        options = listOf("All", "Arnab", "Sadman", "Sabbir").filter { it != currentUser.apiParamName || it == "All" },
                        onSelected = onPersonFilterChange
                    )
                    FilterPill(
                        label = "Type :",
                        value = filterType,
                        options = listOf("All", "Sent", "Received"),
                        onSelected = onTypeFilterChange
                    )
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                        .clickable { onAddSettlementClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            .clickable { expanded = true }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label ",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Dropdown",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(16.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TransactionItem(
    entry: TransactionEntry,
    currentUserName: String,
    onClick: () -> Unit
) {
    val dateNumeral = entry.timestamp.split(" ").firstOrNull()?.split("/")?.getOrNull(1)?.trimStart('0') ?: "1"
    val isSent = entry.from == currentUserName

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date Box
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color.Black, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dateNumeral,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First Image (Base layer)
                Image(
                    painter = rememberAsyncImagePainter(model = UserManager.getProfilePicture(entry.from)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .zIndex(0f),
                    contentScale = ContentScale.Crop
                )

                // Second Image (Overlapping layer)
                Image(
                    painter = rememberAsyncImagePainter(model = UserManager.getProfilePicture(entry.to)),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(x = (-2).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .zIndex(1f),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${if (entry.from == currentUserName) "You" else entry.from} \u2192 ${if (entry.to == currentUserName) "You" else entry.to}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (entry.isEdited) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = entry.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatToFigmaTk(entry.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isSent) Icons.Default.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                    contentDescription = if (isSent) "Sent Transaction" else "Received Transaction",
                    modifier = Modifier.size(12.dp),
                    tint = if (isSent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (isSent) "Sent" else "Received",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Settlements Light")
@Composable
fun SettlementsPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockEntries = listOf(
        TransactionEntry(
            id = "1",
            timestamp = "06/01/2026 10:00:00",
            from = "Arnab",
            to = "Sadman",
            amount = 500.0,
            description = "Lunch at Chillox",
            isEdited = false,
            editCredential = emptyList()
        ),
        TransactionEntry(
            id = "2",
            timestamp = "06/02/2026 14:30:00",
            from = "Sabbir",
            to = "Arnab",
            amount = 1200.0,
            description = "Internet Bill",
            isEdited = true,
            editCredential = listOf(EditHistory("Sabbir", "06/02/2026 15:00:00"))
        ),
        TransactionEntry(
            id = "3",
            timestamp = "06/03/2026 09:15:00",
            from = "Arnab",
            to = "Sabbir",
            amount = 300.0,
            description = "Snacks",
            isEdited = false,
            editCredential = emptyList()
        )
    )
    val mockState = SettlementsUiState(
        isInitialized = true,
        totalSent = 5000.0,
        totalReceived = 2000.0,
        toBeSettled = 3000.0,
        peerBalances = mapOf("Sadman" to 1500.0, "Sabbir" to 1500.0),
        filteredEntries = mockEntries
    )

    OurFinanceTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(
                bottomBar = {
                    PillNavigationBar(
                        currentScreen = NavScreen.Settlement,
                        onScreenSelected = {},
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    SettlementsHeader(
                        state = mockState,
                        selectedMonth = 4,
                        selectedYear = 2026,
                        currentUser = mockUser,
                        onDateSelected = { _, _ -> },
                        onRefreshClick = {}
                    )
                    Box(modifier = Modifier.fillMaxSize()) {
                        SettlementsContent(
                            state = mockState,
                            currentUser = mockUser,
                            filterPerson = "All",
                            filterType = "All",
                            onPersonFilterChange = {},
                            onTypeFilterChange = {},
                            onAddSettlementClick = {},
                            onEntryClick = {},
                            bottomPadding = paddingValues.calculateBottomPadding()
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    name = "Settlements Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SettlementsPreviewDark() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockEntries = listOf(
        TransactionEntry(
            id = "1",
            timestamp = "06/01/2026 10:00:00",
            from = "Arnab",
            to = "Sadman",
            amount = 500.0,
            description = "Lunch at Chillox",
            isEdited = false,
            editCredential = emptyList()
        ),
        TransactionEntry(
            id = "2",
            timestamp = "06/02/2026 14:30:00",
            from = "Sabbir",
            to = "Arnab",
            amount = 1200.0,
            description = "Internet Bill",
            isEdited = true,
            editCredential = listOf(EditHistory("Sabbir", "06/02/2026 15:00:00"))
        )
    )
    val mockState = SettlementsUiState(
        isInitialized = true,
        totalSent = 5000.0,
        totalReceived = 2000.0,
        toBeSettled = 3000.0,
        peerBalances = mapOf("Sadman" to 1500.0, "Sabbir" to 1500.0),
        filteredEntries = mockEntries
    )

    OurFinanceTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(
                bottomBar = {
                    PillNavigationBar(
                        currentScreen = NavScreen.Settlement,
                        onScreenSelected = {},
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    SettlementsHeader(
                        state = mockState,
                        selectedMonth = 5,
                        selectedYear = 2026,
                        currentUser = mockUser,
                        onDateSelected = { _, _ -> },
                        onRefreshClick = {}
                    )
                    Box(modifier = Modifier.fillMaxSize()) {
                        SettlementsContent(
                            state = mockState,
                            currentUser = mockUser,
                            filterPerson = "All",
                            filterType = "All",
                            onPersonFilterChange = {},
                            onTypeFilterChange = {},
                            onAddSettlementClick = {},
                            onEntryClick = {},
                            bottomPadding = paddingValues.calculateBottomPadding()
                        )
                    }
                }
            }
        }
    }
}