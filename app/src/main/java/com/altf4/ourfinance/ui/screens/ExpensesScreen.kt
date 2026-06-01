package com.altf4.ourfinance.ui.screens

import android.app.DatePickerDialog
import android.content.res.Configuration
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.altf4.ourfinance.R
import com.altf4.ourfinance.data.model.ExpenseEntry
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.navigation.Screen
import com.altf4.ourfinance.ui.NavScreen
import com.altf4.ourfinance.ui.PillNavigationBar
import com.altf4.ourfinance.ui.SyncActionButton
import com.altf4.ourfinance.ui.state.ExpensesUiState
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.ui.viewmodel.ExpensesViewModel
import com.altf4.ourfinance.utils.UserManager
import java.text.SimpleDateFormat
import java.util.*

private fun formatToFigmaTk(value: Double): String {
    val absValue = kotlin.math.abs(value)
    val formatter = java.text.NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return if (value < 0) "-Tk. ${formatter.format(absValue)}" else "Tk. ${formatter.format(absValue)}"
}

@Composable
fun ExpensesScreen(
    viewModel: ExpensesViewModel,
    currentUser: GoogleUser,
    navController: NavController,
    onAddExpenseClick: () -> Unit,
    onEntryClick: (ExpenseEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    LaunchedEffect(currentUser.apiParamName) {
        if (!uiState.isInitialized) {
            viewModel.fetchExpenses(currentUser.apiParamName)
        }
    }

    ExpensesScreenContent(
        uiState = uiState,
        selectedMonth = selectedMonth,
        selectedYear = selectedYear,
        filterType = filterType,
        currentUser = currentUser,
        navController = navController,
        onDateSelected = { m, y -> viewModel.setDateFilter(m, y, currentUser.apiParamName) },
        onRefreshClick = { viewModel.fetchExpenses(currentUser.apiParamName, forceRefresh = true) },
        onFilterChange = { viewModel.setFilterType(it, currentUser.apiParamName) },
        onAddExpenseClick = onAddExpenseClick,
        onEntryClick = onEntryClick,
        onBackClick = {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
            }
        },
        modifier = modifier
    )
}

@Composable
fun ExpensesScreenContent(
    uiState: ExpensesUiState,
    selectedMonth: Int,
    selectedYear: Int,
    filterType: String,
    currentUser: GoogleUser,
    navController: NavController,
    onDateSelected: (Int, Int) -> Unit,
    onRefreshClick: () -> Unit,
    onFilterChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onEntryClick: (ExpenseEntry) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PillNavigationBar(
                currentScreen = NavScreen.Expenses,
                onScreenSelected = { selectedScreen ->
                    when (selectedScreen) {
                        NavScreen.Dashboard -> {
                            navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        }
                        NavScreen.Settlement -> {
                            navController.navigate(Screen.Settlements.route) {
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
            ExpensesHeader(
                state = uiState,
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onDateSelected = onDateSelected,
                onRefreshClick = onRefreshClick
            )
            ExpensesContent(
                state = uiState,
                currentUser = currentUser,
                filterType = filterType,
                onFilterChange = onFilterChange,
                onAddExpenseClick = onAddExpenseClick,
                onEntryClick = onEntryClick,
                bottomPadding = paddingValues.calculateBottomPadding()
            )
        }
    }
}

@Composable
fun ExpensesHeader(
    state: ExpensesUiState,
    selectedMonth: Int,
    selectedYear: Int,
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
                text = "Expenses",
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

        Text(
            text = "To Be Adjusted",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = formatToFigmaTk(state.toBeAdjusted),
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = if (state.toBeAdjusted > 0) MaterialTheme.colorScheme.error else Color(0xFF22C55E)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Expense",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = formatToFigmaTk(state.totalExpense),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Your Contribution",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = formatToFigmaTk(state.userContribution),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun ExpensesContent(
    state: ExpensesUiState,
    currentUser: GoogleUser,
    filterType: String,
    onFilterChange: (String) -> Unit,
    onAddExpenseClick: () -> Unit,
    onEntryClick: (ExpenseEntry) -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        var expanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expanded = true }
            ) {
                Text(
                    text = filterType,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.onBackground
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Your Entries") },
                        onClick = {
                            onFilterChange("Your Entries")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("All Entries") },
                        onClick = {
                            onFilterChange("All Entries")
                            expanded = false
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .clickable { onAddExpenseClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add Expenses",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = bottomPadding + 20.dp)
        ) {
            items(state.filteredEntries) { entry ->
                ExpenseItem(
                    entry = entry,
                    currentUserName = currentUser.apiParamName,
                    onClick = { onEntryClick(entry) }
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(
    entry: ExpenseEntry,
    currentUserName: String,
    onClick: () -> Unit
) {
    val dateNumeral = entry.timestamp.split(" ").firstOrNull()?.split("/")?.getOrNull(1)?.trimStart('0') ?: "1"

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
                Image(
                    painter = rememberAsyncImagePainter(model = UserManager.getProfilePicture(entry.person)),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (entry.person == currentUserName) "You" else entry.person,
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = entry.category,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview(showBackground = true, name = "Expenses Light")
@Composable
fun ExpensesPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockEntries = listOf(
        ExpenseEntry("1", "05/10/2026 14:30:00", "Arnab", "Groceries", 1200.0, "Food", false, emptyList()),
        ExpenseEntry("2", "05/08/2026 10:15:00", "Sadman", "Electricity", 850.0, "Utilities", true, emptyList())
    )
    val mockState = ExpensesUiState(
        isInitialized = true,
        allEntries = mockEntries,
        filteredEntries = mockEntries,
        totalExpense = 2050.0,
        userContribution = 1200.0,
        toBeAdjusted = -516.0
    )

    OurFinanceTheme(darkTheme = false) {
        ExpensesScreenContent(
            uiState = mockState,
            selectedMonth = 4,
            selectedYear = 2026,
            filterType = "All Entries",
            currentUser = mockUser,
            navController = NavController(LocalContext.current),
            onDateSelected = { _, _ -> },
            onRefreshClick = {},
            onFilterChange = {},
            onAddExpenseClick = {},
            onEntryClick = {},
            onBackClick = {}
        )
    }
}