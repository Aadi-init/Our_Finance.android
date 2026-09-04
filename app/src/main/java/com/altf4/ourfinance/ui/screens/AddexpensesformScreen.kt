package com.altf4.ourfinance.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.altf4.ourfinance.R
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.utils.UserManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddexpensesformScreen(
    initialAmount: Double,
    initialCategory: String,
    initialTimestamp: Long,
    currentUser: GoogleUser,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: (Double, String, Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by remember { mutableStateOf("%.2f".format(initialAmount)) }
    var amountError by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(initialCategory) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = initialTimestamp }) }
    var description by remember { mutableStateOf("") }
    
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale.US) }

    val handleAmountChange: (String) -> Unit = { input ->
        // Allow empty for clearing, or digits and at most one decimal point with up to 2 digits after it
        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
            amount = input
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomTopBar(
                title = "Add Expenses",
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Go Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(enabled = !isSaving) { onBackClick() }
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            val finalAmount = amount.toDoubleOrNull() ?: 0.0
                            if (finalAmount > 0) {
                                amountError = false
                                onSaveClick(finalAmount, category, selectedDate.timeInMillis, description)
                            } else {
                                // Validation: amount must be greater than zero
                                amountError = true
                                android.widget.Toast.makeText(context, "Please enter a valid positive amount", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_save),
                                contentDescription = "Save",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenHeight = maxHeight
            val isSmallScreen = screenHeight < 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = if (isSmallScreen) 10.dp else 20.dp),
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 12.dp)
            ) {
                // --- A. DATE PICKER FIELD ---
                FormField(label = "Date") { //, modifier = Modifier.weight(1f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.08f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = !isSaving) {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        selectedDate = Calendar.getInstance().apply {
                                            set(year, month, day)
                                        }
                                    },
                                    selectedDate.get(Calendar.YEAR),
                                    selectedDate.get(Calendar.MONTH),
                                    selectedDate.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dateFormatter.format(selectedDate.time),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Select Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // --- B. TWO-COLUMN ROW: CATEGORY & PERSON ---
                Row(
                    modifier = Modifier.fillMaxWidth(),    //.weight(0.8f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category Dropdown
                    FormField(label = "Category", modifier = Modifier.weight(1f)) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.09f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = !isSaving) { isCategoryDropdownExpanded = true }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = isCategoryDropdownExpanded,
                                onDismissRequest = { isCategoryDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Groceries") },
                                    onClick = {
                                        category = "Groceries"
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Miscellaneous") },
                                    onClick = {
                                        category = "Miscellaneous"
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Person Field (Read-only)
                    FormField(label = "Person", modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.09f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = currentUser.profilePictureUrl ?: "https://ui-avatars.com/api/?name=${currentUser.apiParamName}&background=22C55E&color=fff"
                                ),
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(if (isSmallScreen) 20.dp else 24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "You",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // --- C. AMOUNT FIELD ---
                FormField(label = "Amount") { //, modifier = Modifier.weight(0.4f)
                    TextField(
                        value = amount,
                        onValueChange = {
                            handleAmountChange(it)
                            amountError = false
                        },
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.115f),
                        enabled = !isSaving,
                        isError = amountError,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        prefix = {
                            Text(
                                text = "Tk. ",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }

                // --- D. DESCRIPTION FIELD ---
                FormField(label = "Description") { //, modifier = Modifier.weight(1f)
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.3f),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = {
                            Text(
                                text = "Add description",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        content()
    }
}

@Preview(showBackground = true, name = "Add Expenses Form Light")
@Composable
fun AddexpensesformScreenPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "email@test.com", null, "Arnab")
    OurFinanceTheme(darkTheme = false) {
        AddexpensesformScreen(
            initialAmount = 1500.0,
            initialCategory = "Groceries",
            initialTimestamp = System.currentTimeMillis(),
            currentUser = mockUser,
            isSaving = false,
            onBackClick = {},
            onSaveClick = { _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Add Expenses Form Dark")
@Composable
fun AddexpensesformScreenPreviewDark() {
    val mockUser = GoogleUser("Arnab Banik", "email@test.com", null, "Arnab")
    OurFinanceTheme(darkTheme = true) {
        AddexpensesformScreen(
            initialAmount = 1500.0,
            initialCategory = "Groceries",
            initialTimestamp = System.currentTimeMillis(),
            currentUser = mockUser,
            isSaving = false,
            onBackClick = {},
            onSaveClick = { _, _, _, _ -> }
        )
    }
}
