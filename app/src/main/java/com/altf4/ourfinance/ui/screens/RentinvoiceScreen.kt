package com.altf4.ourfinance.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.altf4.ourfinance.R
import com.altf4.ourfinance.data.model.DashboardResponse
import com.altf4.ourfinance.data.model.RentInvoiceBreakdown
import com.altf4.ourfinance.data.network.RetrofitClient
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.utils.MockDashboardData
import com.altf4.ourfinance.utils.toTkFormat
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun RentinvoiceScreen(
    data: DashboardResponse,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    // --- DYNAMIC DATE LOGIC ---
    val calendar = remember { Calendar.getInstance() }
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val daysLeft = if (today < 10) 10 - today else 0

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    val currentMonthYear = monthYearFormat.format(calendar.time).uppercase(Locale.US)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomTopBar(
                title = "Rent Invoice",
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Go Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBackClick() }
                    )
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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 2. HEAVY WEIGHT TOP LOGIC (CENTERED CIRCLE) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (isSmallScreen) 1f else 1.2f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .requiredSize(if (isSmallScreen) 120.dp else 160.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = "Due in",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$daysLeft",
                            style = (if (isSmallScreen) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp)).copy(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Days",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- 3. BOTTOM CARDS & BUTTON CONTAINER ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(if (isSmallScreen) 2f else 1.8f)
                        .padding(horizontal = 15.dp, vertical = if (isSmallScreen) 8.dp else 24.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 16.dp)
                ) {
                    // --- TOTAL RENT CARD ---
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(0.7f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentMonthYear,
                                style = MaterialTheme.typography.titleMedium,
                                //fontSize = if (isSmallScreen) 16.sp else 22.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = data.totalRent.toTkFormat(),
                                style = MaterialTheme.typography.titleMedium,
                                //fontSize = if (isSmallScreen) 16.sp else 22.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // --- INVOICE BREAKDOWN CARD ---
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(if (isSmallScreen) 3f else 4f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            // Dashed Line Divider
                            val outlineColor = MaterialTheme.colorScheme.outline
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                            ) {
                                val yCenter = size.height / 2
                                drawLine(
                                    color = outlineColor,
                                    start = Offset(0f, yCenter),
                                    end = Offset(size.width, yCenter),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 20f), 0f)
                                )
                            }

                            val breakdown = data.invoiceBreakdown

                            BreakdownRow(title = "Rental Bill", amount = breakdown.rent, isSmall = isSmallScreen)
                            BreakdownRow(title = "Electricity Bill", amount = breakdown.electricity, isSmall = isSmallScreen)
                            BreakdownRow(title = "Internet Bill", amount = breakdown.internet, isSmall = isSmallScreen)
                            BreakdownRow(title = "Water Filter Bill", amount = breakdown.waterFilter, isSmall = isSmallScreen)
                            BreakdownRow(title = "Househelp Bill", amount = breakdown.househelp, isSmall = isSmallScreen)
                            BreakdownRow(title = "Other Bills", amount = breakdown.others, isSmall = isSmallScreen)

                            BreakdownRow(
                                title = "Adjustments",
                                amount = breakdown.adjustments,
                                isAdjustment = true,
                                isSmall = isSmallScreen
                            )
                        }
                    }

                    // --- SMART DOWNLOAD BUTTON ---
                    Button(
                        onClick = {
                            onDownloadClick()

                            if (data.rentStatus.equals("Outdated", ignoreCase = true)) {
                                Toast.makeText(context, "Rent has not been updated yet. Download Invoice later.", Toast.LENGTH_LONG).show()
                            } else {
                                if (!isDownloading) {
                                    isDownloading = true
                                    coroutineScope.launch {
                                        try {
                                            val apiUser = when {
                                                data.fullName.contains("Arnab", ignoreCase = true) -> "Arnab"
                                                data.fullName.contains("Sadman", ignoreCase = true) -> "Sadman"
                                                else -> "Sabbir"
                                            }

                                            val response = RetrofitClient.apiService.downloadInvoice(username = apiUser)

                                            if (response.pdfBase64 != null && response.fileName != null) {
                                                savePdfToDownloads(context, response.pdfBase64, response.fileName)
                                            } else {
                                                Toast.makeText(context, "Failed to fetch invoice. Please try again.", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Network Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isDownloading = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.8f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Download",
                                style = MaterialTheme.typography.titleMedium,
                                //fontSize = if (isSmallScreen) 16.sp else 19.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    title: String,
    amount: Double,
    isAdjustment: Boolean = false,
    isSmall: Boolean = false
) {
    val contentColor = if (isAdjustment) {
        if (amount < 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    val titleColor = if (isAdjustment) contentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (isSmall) 12.sp else 14.sp,
            color = titleColor
        )
        Text(
            text = amount.toTkFormat(),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = if (isSmall) 12.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

/**
 * Main Save Entrypoint: Selects appropriate strategy depending on device's operating system version
 */
private fun savePdfToDownloads(context: Context, base64: String, fileName: String) {
    try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            savePdfUsingMediaStore(context, bytes, fileName)
        } else {
            savePdfUsingLegacyStorage(context, bytes, fileName)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Modern MediaStore Save Workflow: Used on devices running Android 10+ (API 29+)
 */
@RequiresApi(Build.VERSION_CODES.Q)
private fun savePdfUsingMediaStore(context: Context, bytes: ByteArray, fileName: String) {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }

    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    if (uri != null) {
        resolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(bytes)
        }
        Toast.makeText(context, "Invoice downloaded", Toast.LENGTH_SHORT).show()
        // Offer options to open the PDF immediately
        openPdfFile(context, uri)
    } else {
        Toast.makeText(context, "Failed to create file in Downloads", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Legacy File Stream Workflow: Fallback for older devices running Android 7.0 - 9.0 (API 24 - 28)
 */
private fun savePdfUsingLegacyStorage(context: Context, bytes: ByteArray, fileName: String) {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) {
        downloadsDir.mkdirs()
    }

    val file = File(downloadsDir, fileName)
    FileOutputStream(file).use { outputStream ->
        outputStream.write(bytes)
    }
    Toast.makeText(context, "Invoice downloaded to: ${file.name}", Toast.LENGTH_SHORT).show()

    // Legacy File Uri Sharing using safe FileProvider to prevent FileUriExposedException
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        openPdfFile(context, uri)
    } catch (e: Exception) {
        // Fallback to basic Uri if FileProvider isn't declared
        val uri = Uri.fromFile(file)
        openPdfFile(context, uri)
    }
}

/**
 * Universal Intent Launcher: Fires Android's native system picker to view the PDF file on any PDF Reader app
 */
private fun openPdfFile(context: Context, uri: Uri) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Wrap with a clean chooser popup
        val chooserIntent = Intent.createChooser(intent, "Open Invoice with...")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooserIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "No app found to open PDF files. Please install a PDF Viewer.", Toast.LENGTH_LONG).show()
    }
}

@Preview(showBackground = true, name = "Rent Invoice Light")
@Composable
fun RentinvoiceScreenPreviewLight() {
    OurFinanceTheme(darkTheme = false) {
        RentinvoiceScreen(
            data = MockDashboardData,
            onBackClick = {},
            onDownloadClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Rent Invoice Dark")
@Composable
fun RentinvoiceScreenPreviewDark() {
    OurFinanceTheme(darkTheme = true) {
        RentinvoiceScreen(
            data = MockDashboardData,
            onBackClick = {},
            onDownloadClick = {}
        )
    }
}