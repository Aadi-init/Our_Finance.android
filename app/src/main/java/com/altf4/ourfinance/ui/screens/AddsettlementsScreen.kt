package com.altf4.ourfinance.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altf4.ourfinance.R
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddsettlementsScreen(
    currentUser: String,
    onBackClick: () -> Unit,
    onSaveClick: (Double, String, String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var formula by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Sent") } // "Sent" or "Received"
    var person by remember { mutableStateOf(if (currentUser == "Arnab") "Sadman" else "Arnab") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var isPersonDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    
    // Simple live evaluation logic
    val liveResult = remember(formula) {
        evaluateFormula(formula)
    }

    val dateFormatter = remember { SimpleDateFormat("d'th' MMM", Locale.US) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomTopBar(
                title = "Add Settlements",
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Go Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBackClick() }
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        val amount = liveResult.toDoubleOrNull() ?: 0.0
                        onSaveClick(amount, selectedType, person, selectedDate.timeInMillis)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save),
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // --- 1. SETTLEMENT TYPE SLIDER ---
            SettlementTypeSlider(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )

            // --- 2. DISPLAY AREA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (formula.isEmpty()) "0" else formula,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    maxLines = 3,
                    lineHeight = 60.sp
                )
                if (formula.isNotEmpty()) {
                    Text(
                        text = liveResult,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        textAlign = TextAlign.End
                    )
                }
            }

            // --- 3. METADATA SECTION ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Person Group
                Column(
                    modifier = Modifier.clickable { isPersonDropdownExpanded = true }
                ) {
                    Text(
                        text = "Person",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                    Text(
                        text = person,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    DropdownMenu(
                        expanded = isPersonDropdownExpanded,
                        onDismissRequest = { isPersonDropdownExpanded = false }
                    ) {
                        val roommates = listOf("Arnab", "Sadman", "Sabbir").filter { it != currentUser }
                        roommates.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    person = name
                                    isPersonDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date Group
                Column(
                    modifier = Modifier.clickable {
                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDate = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }
                            },
                            selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH)
                        )
                        datePickerDialog.show()
                    },
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Date",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                    Text(
                        text = dateFormatter.format(selectedDate.time),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. CALCULATOR KEYPAD ---
            val keys = listOf(
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "+",
                "0", "=", "<", "-"
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                items(keys) { key ->
                    CalculatorButton(
                        text = key,
                        onClick = {
                            when (key) {
                                "<" -> if (formula.isNotEmpty()) formula = formula.dropLast(1)
                                "=" -> {
                                    val result = evaluateFormula(formula)
                                    if (result.isNotEmpty()) formula = result
                                }
                                "/", "*", "+", "-" -> {
                                    if (formula.isNotEmpty()) {
                                        val lastChar = formula.last()
                                        if (lastChar in "+-*/") {
                                            formula = formula.dropLast(1) + key
                                        } else if (formula.any { it in "+-*/" }) {
                                            val result = evaluateFormula(formula)
                                            if (result.isNotEmpty()) {
                                                formula = result + key
                                            }
                                        } else {
                                            formula += key
                                        }
                                    }
                                }
                                else -> formula += key
                            }
                        },
                        onLongClick = {
                            if (key == "<") {
                                formula = ""
                            }
                        },
                        isOperator = key in listOf("/", "*", "+", "-", "=", "<")
                    )
                }
            }
        }
    }
}

@Composable
fun SettlementTypeSlider(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(4.dp)
    ) {
        val width = maxWidth
        val tabWidth = width / 2
        
        val animatedOffset by animateDpAsState(
            targetValue = if (selectedType == "Sent") 0.dp else tabWidth,
            label = "SliderOffset"
        )

        // Active Pill
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTypeSelected("Sent") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sent",
                    fontWeight = FontWeight.Medium,
                    color = if (selectedType == "Sent") MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceDim
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTypeSelected("Received") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Received",
                    fontWeight = FontWeight.Medium,
                    color = if (selectedType == "Received") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceDim
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isOperator: Boolean
) {
    val backgroundColor = if (isOperator) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun evaluateFormula(formula: String): String {
    if (formula.isEmpty()) return ""
    try {
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()
        
        for (char in formula) {
            if (char.isDigit() || char == '.') {
                currentNumber.append(char)
            } else if (char in "+-*/") {
                if (currentNumber.isNotEmpty()) tokens.add(currentNumber.toString())
                tokens.add(char.toString())
                currentNumber = StringBuilder()
            }
        }
        if (currentNumber.isNotEmpty()) tokens.add(currentNumber.toString())

        if (tokens.isEmpty()) return ""
        
        var result = tokens[0].toDoubleOrNull() ?: 0.0
        var i = 1
        while (i < tokens.size) {
            val op = tokens[i]
            val nextVal = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
            result = when (op) {
                "+" -> result + nextVal
                "-" -> result - nextVal
                "*" -> result * nextVal
                "/" -> if (nextVal != 0.0) result / nextVal else 0.0
                else -> result
            }
            i += 2
        }
        
        return if (result % 1 == 0.0) result.toInt().toString() else "%.2f".format(result)
    } catch (e: Exception) {
        return ""
    }
}

@Preview(showBackground = true, name = "Add Settlements Light")
@Composable
fun AddsettlementsScreenPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        AddsettlementsScreen(currentUser = "Arnab", onBackClick = {}, onSaveClick = { _, _, _, _ -> })
    }
}

@Preview(showBackground = true, name = "Add Settlements Dark")
@Composable
fun AddsettlementsScreenPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        AddsettlementsScreen(currentUser = "Arnab", onBackClick = {}, onSaveClick = { _, _, _, _ -> })
    }
}
