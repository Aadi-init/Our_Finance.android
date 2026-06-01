package com.altf4.ourfinance.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.altf4.ourfinance.R
import com.altf4.ourfinance.data.model.DashboardResponse
import com.altf4.ourfinance.data.model.RentInvoiceBreakdown
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.utils.MockDashboardData
import com.altf4.ourfinance.utils.toTkFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.text.PlatformTextStyle

@Composable
fun RentinvoiceScreen(
    data: DashboardResponse,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- DYNAMIC DATE LOGIC ---
    val calendar = remember { Calendar.getInstance() }
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val daysLeft = if (today < 10) 10 - today else 0

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    val currentMonthYear = monthYearFormat.format(calendar.time).uppercase(Locale.US)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // --- 1. REUSABLE TOP TITLE BAR ---
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

        // --- 2. HEAVY WEIGHT TOP LOGIC (CENTERED CIRCLE) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .requiredSize(160.dp)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = "Due in",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    // Note: Mapped to onSurfaceVariant since that holds your "TextHint" gray color
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$daysLeft",
                    fontSize = 60.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = LocalTextStyle.current.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Text(
                    text = "Days",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 3. BOTTOM CARDS & BUTTON CONTAINER ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TOTAL RENT CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentMonthYear,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = data.totalRent.toTkFormat(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // --- INVOICE BREAKDOWN CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dashed Line Divider
                    val outlineColor = MaterialTheme.colorScheme.outline
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp) // 1.dp height is perfect for a clean divider line
                    ) {
                        val yCenter = size.height / 2

                        drawLine(
                            color = outlineColor,
                            start = Offset(0f, yCenter), // Centered on Y-axis
                            end = Offset(size.width, yCenter), // Centered on Y-axis
                            strokeWidth = 1.dp.toPx(), // Explicit line thickness
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 20f), 0f) // Adjusted dash pattern
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    val breakdown = data.invoiceBreakdown

                    // Invoice List Items
                    BreakdownRow(title = "Rental Bill", amount = breakdown.rent)
                    BreakdownRow(title = "Electricity Bill", amount = breakdown.electricity)
                    BreakdownRow(title = "Internet Bill", amount = breakdown.internet)
                    BreakdownRow(title = "Water Filter Bill", amount = breakdown.waterFilter)
                    BreakdownRow(title = "Househelp Bill", amount = breakdown.househelp)
                    BreakdownRow(title = "Other Bills", amount = breakdown.others)

                    // Adjustments Row (Custom Color Logic)
                    BreakdownRow(
                        title = "Adjustments",
                        amount = breakdown.adjustments,
                        isAdjustment = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(0.dp))

            // --- DOWNLOAD BUTTON ---
            Button(
                onClick = onDownloadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(50) // Full pill shape matching Figma
            ) {
                Text(
                    text = "Download",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    title: String,
    amount: Double,
    isAdjustment: Boolean = false
) {
    // Determine color logic strictly based on prompt rules
    val contentColor = if (isAdjustment) {
        if (amount < 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    val titleColor = if (isAdjustment) contentColor else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = titleColor
        )
        Text(
            text = amount.toTkFormat(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
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