package com.altf4.ourfinance.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.altf4.ourfinance.data.model.EditHistory
import com.altf4.ourfinance.data.model.ExpenseEntry
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.utils.UserManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditexpenseentryScreen(
    entry: ExpenseEntry,
    currentUser: GoogleUser,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: (Double, String, Long, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine if the current user is allowed to edit this entry
    // Rule: User can only edit entries created by them.
    // If they change the person, their access changes to the new person.
    val originalPerson = entry.person
    var person by remember { mutableStateOf(entry.person) }
    // FIX: Base edit access on the ORIGINAL person of the entry,
    // so the user doesn't lose access mid-edit if they change the name.
    val canEdit = originalPerson == currentUser.apiParamName

    var amount by remember { mutableStateOf("%.2f".format(entry.amount)) }
    var amountError by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(entry.category) }
    
    val initialCal = remember(entry.timestamp) {
        Calendar.getInstance().apply {
            try {
                time = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).parse(entry.timestamp) ?: Date()
            } catch (e: Exception) {
                time = Date()
            }
        }
    }
    var selectedDate by remember { mutableStateOf(initialCal) }
    var description by remember { mutableStateOf(entry.description) }
    
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isPersonDropdownExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale.US) }

    // Logic to detect if ANY field has been changed from its original state
    val isDataChanged = remember(amount, category, selectedDate, description, person) {
        val originalAmountStr = "%.2f".format(entry.amount)
        val originalDateStr = initialCal.timeInMillis.let { 
            val cal = Calendar.getInstance().apply { timeInMillis = it }
            dateFormatter.format(cal.time)
        }
        val currentDateStr = dateFormatter.format(selectedDate.time)
        
        amount != originalAmountStr || 
        category != entry.category || 
        currentDateStr != originalDateStr || 
        description != entry.description ||
        person != entry.person
    }

    val handleAmountChange: (String) -> Unit = { input ->
        if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d{0,2}$"""))) {
            amount = input
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomTopBar(
                title = "Expense Entry",
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
                    // Save button only shown if user has edit rights AND data has changed
                    if (canEdit && isDataChanged) {
                        IconButton(
                            onClick = {
                                val finalAmount = amount.toDoubleOrNull() ?: 0.0
                                if (finalAmount > 0) {
                                    amountError = false
                                    onSaveClick(finalAmount, category, selectedDate.timeInMillis, description, person)
                                } else {
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
                            .clickable(enabled = !isSaving && canEdit) {
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
                            color = if (canEdit) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
                    modifier = Modifier.fillMaxWidth(),     //.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category Dropdown
                    FormField(label = "Category", modifier = Modifier.weight(1f)) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.09f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = !isSaving && canEdit) { isCategoryDropdownExpanded = true }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category,
                                    color = if (canEdit) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
                                    text = { Text("Groceries", style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        category = "Groceries"
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Miscellaneous", style = MaterialTheme.typography.bodyMedium) },
                                    onClick = {
                                        category = "Miscellaneous"
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Person Field (Editable ONLY if user created it)
                    FormField(label = "Person", modifier = Modifier.weight(1f)) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.09f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(enabled = !isSaving && canEdit) { isPersonDropdownExpanded = true }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = UserManager.getProfilePicture(person)
                                    ),
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .size(if (isSmallScreen) 20.dp else 24.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = if (person == currentUser.apiParamName) "You" else person,
                                    color = if (canEdit) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                if (canEdit) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = isPersonDropdownExpanded,
                                onDismissRequest = { isPersonDropdownExpanded = false }
                            ) {
                                listOf("Arnab", "Sadman", "Sabbir").forEach { name ->
                                    DropdownMenuItem(
                                        text = { Text(if (name == currentUser.apiParamName) "You" else name, style = MaterialTheme.typography.bodyMedium) },
                                        onClick = {
                                            person = name
                                            isPersonDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // --- C. AMOUNT FIELD ---
                FormField(label = "Amount") {    //, modifier = Modifier.weight(1f)
                    TextField(
                        value = amount,
                        onValueChange = {
                            if (canEdit) {
                                handleAmountChange(it)
                                amountError = false
                            }
                        },
                        readOnly = !canEdit,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.115f),
                        enabled = !isSaving,
                        isError = amountError,
                        shape = RoundedCornerShape(12.dp),
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
                FormField(label = "Description") {   //, modifier = Modifier.weight(2f)
                    TextField(
                        value = description,
                        onValueChange = { if (canEdit) description = it },
                        readOnly = !canEdit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.3f),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
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

                // --- E. EDIT HISTORY SECTION ---
                if (entry.editCredential.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        entry.editCredential.take(if (isSmallScreen) 1 else 3).forEach { history ->
                            EditHistoryItem(
                                history = history,
                                currentUserName = currentUser.apiParamName,
                                currentUserDisplayName = currentUser.displayName ?: ""
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditHistoryItem(history: EditHistory, currentUserName: String, currentUserDisplayName: String) {
    // Logic to check if the history item belongs to the current user
    val isMe = history.name == currentUserName || history.name == currentUserDisplayName
    val displayName = if (isMe) "You" else history.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = UserManager.getProfilePicture(history.name)
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayName,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = history.time,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        content()
    }
}

@Preview(showBackground = true, name = "Edit Expense Entry Light")
@Composable
fun EditexpenseentryScreenPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockEntry = ExpenseEntry(
        id = "abc12345",
        timestamp = "05/01/2026 12:00:00",
        person = "Arnab",
        description = "Aloo, Dim, Bread, Peyaj, Coffee, Chini, Tel, Dal",
        amount = 1500.0,
        category = "Groceries",
        isEdited = true,
        editCredential = listOf(
            EditHistory("Arnab", "2nd May 12:55 AM"),
            EditHistory("Arnab", "5th May 09:21 AM")
        )
    )
    OurFinanceTheme(darkTheme = false) {
        EditexpenseentryScreen(
            entry = mockEntry,
            currentUser = mockUser,
            isSaving = false,
            onBackClick = {},
            onSaveClick = { _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Edit Expense Entry Dark")
@Composable
fun EditexpenseentryScreenPreviewDark() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockEntry = ExpenseEntry(
        id = "abc12345",
        timestamp = "05/01/2026 12:00:00",
        person = "Arnab",
        description = "Aloo, Dim, Bread, Peyaj, Coffee, Chini, Tel, Dal",
        amount = 1500.0,
        category = "Groceries",
        isEdited = true,
        editCredential = listOf(
            EditHistory("Arnab", "2nd May 12:55 AM")
        )
    )
    OurFinanceTheme(darkTheme = true) {
        EditexpenseentryScreen(
            entry = mockEntry,
            currentUser = mockUser,
            isSaving = false,
            onBackClick = {},
            onSaveClick = { _, _, _, _, _ -> }
        )
    }
}

