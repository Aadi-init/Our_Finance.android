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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CallMade
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.altf4.ourfinance.R
import com.altf4.ourfinance.data.model.EditHistory
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.data.model.TransactionEntry
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.utils.UserManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditsettlemententryScreen(
    entry: TransactionEntry,
    currentUser: GoogleUser,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: (Double, String, String, Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Audit Rule: User can edit if they were the sender OR receiver
    val canEditInitial = entry.from == currentUser.apiParamName || entry.to == currentUser.apiParamName

    // State based on current values
    val currentTypeInitial = if (entry.to == currentUser.apiParamName) "Received" else "Sent"
    val currentPersonInitial = if (entry.to == currentUser.apiParamName) entry.from else entry.to

    var type by remember { mutableStateOf(currentTypeInitial) }
    var person by remember { mutableStateOf(currentPersonInitial) }
    var amount by remember { mutableStateOf("%.2f".format(entry.amount)) }
    var amountError by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf(entry.description) }

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

    var isTypeDropdownExpanded by remember { mutableStateOf(false) }
    var isPersonDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale.US) }

    // Change Detection
    val isDataChanged = remember(amount, type, person, selectedDate, description) {
        val originalAmountStr = "%.2f".format(entry.amount)
        val originalDateStr = dateFormatter.format(initialCal.time)
        val currentDateStr = dateFormatter.format(selectedDate.time)

        amount != originalAmountStr ||
        type != currentTypeInitial ||
        person != currentPersonInitial ||
        currentDateStr != originalDateStr ||
        description != entry.description
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
                title = "Settlement Entry",
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
                    if (canEditInitial && isDataChanged) {
                        IconButton(
                            onClick = {
                                val finalAmount = amount.toDoubleOrNull() ?: 0.0
                                if (finalAmount > 0) {
                                    amountError = false
                                    onSaveClick(finalAmount, type, person, selectedDate.timeInMillis, description)
                                } else {
                                    amountError = true
                                    android.widget.Toast.makeText(context, "Please enter a valid positive amount", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- A. DATE PICKER FIELD ---
            FormField(label = "Date") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = !isSaving && canEditInitial) {
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    selectedDate = Calendar.getInstance().apply { set(year, month, day) }
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
                        color = if (canEditInitial) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // --- B. TWO-COLUMN ROW: CATEGORY & PERSON ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category (Received/Sent)
                FormField(label = "Category", modifier = Modifier.weight(1f)) {
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = !isSaving && canEditInitial) { isTypeDropdownExpanded = true }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val icon = if (type == "Received") Icons.AutoMirrored.Filled.CallReceived else Icons.Default.CallMade
                            val color = if (type == "Received") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = type, color = color, fontSize = 16.sp)
                            }
                            if (canEditInitial) {
                                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        DropdownMenu(expanded = isTypeDropdownExpanded, onDismissRequest = { isTypeDropdownExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Received") },
                                onClick = { type = "Received"; isTypeDropdownExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Sent") },
                                onClick = { type = "Sent"; isTypeDropdownExpanded = false }
                            )
                        }
                    }
                }

                // Person
                FormField(label = "Person", modifier = Modifier.weight(1f)) {
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = !isSaving && canEditInitial) { isPersonDropdownExpanded = true }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = UserManager.getProfilePicture(person)),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (person == currentUser.apiParamName) "You" else person,
                                    color = if (canEditInitial) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (canEditInitial) {
                                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        DropdownMenu(expanded = isPersonDropdownExpanded, onDismissRequest = { isPersonDropdownExpanded = false }) {
                            listOf("Arnab", "Sadman", "Sabbir").filter { it != currentUser.apiParamName }.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = { person = name; isPersonDropdownExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // --- C. AMOUNT FIELD ---
            FormField(label = "Amount") {
                TextField(
                    value = amount,
                    onValueChange = { if (canEditInitial) { handleAmountChange(it); amountError = false } },
                    readOnly = !canEditInitial,
                    modifier = Modifier.fillMaxWidth(),
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
                    prefix = { Text("Tk. ", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
            }

            // --- D. DESCRIPTION FIELD ---
            FormField(label = "Description") {
                TextField(
                    value = description,
                    onValueChange = { if (canEditInitial) description = it },
                    readOnly = !canEditInitial,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
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
                    placeholder = { Text("Add description", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), fontSize = 14.sp) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
            }

            // --- E. EDIT HISTORY SECTION ---
            if (entry.editCredential.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                entry.editCredential.forEach { history ->
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

@Composable
private fun EditHistoryItem(history: EditHistory, currentUserName: String, currentUserDisplayName: String) {
    val isMe = history.name == currentUserName || history.name == currentUserDisplayName
    val displayName = if (isMe) "You" else history.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(50))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(model = UserManager.getProfilePicture(history.name)),
                contentDescription = null,
                modifier = Modifier.size(16.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = displayName, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Text(text = history.time, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    }
}

@Composable
private fun FormField(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.surfaceContainerLowest)
        content()
    }
}

@Preview(showBackground = true, name = "Edit Settlement Entry Light")
@Composable
fun EditsettlemententryScreenPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockEntry = TransactionEntry(
        id = "abc12345",
        timestamp = "05/01/2026 12:00:00",
        from = "Sadman",
        to = "Arnab",
        amount = 1500.0,
        description = "Bazar shared cost",
        isEdited = true,
        editCredential = listOf(
            EditHistory("Arnab Banik", "2nd May 12:55 AM"),
            EditHistory("Sadman Hossain", "5th May 09:21 AM")
        )
    )
    OurFinanceTheme(darkTheme = false) {
        EditsettlemententryScreen(
            entry = mockEntry,
            currentUser = mockUser,
            isSaving = false,
            onBackClick = {},
            onSaveClick = { _, _, _, _, _ -> }
        )
    }
}
