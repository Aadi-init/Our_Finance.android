package com.altf4.ourfinance.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 1. BASE COLORS (Raw Palette)
// ==========================================

val SolidWhite = Color(0xFFFFFFFF)
val SolidBlack = Color(0xFF000000)

val AccentRedNegative = Color(0xFFFF383C)  // Negative amounts, Pending, Logout
val AccentGreenPositive = Color(0xFF34C759) // Positive amounts, Paid

val RowSeparatorGray = Color(0xFF757575)   // Rent Invoice separator (Both modes)

// Opacity Colors
val Black50Opacity = Color(0x80000000)     // 50% Opacity Black
val White50Opacity = Color(0x80FFFFFF)     // 50% Opacity White
val White75Opacity = Color(0xBFFFFFFF)     // 75% Opacity White

// Light Mode Specifics
val AppBgLightHex = Color(0xFFF0F0F0)
val NavTrayLightHex = Color(0xFFE0E0E0)
val LightGrayHintHex = Color(0xFFA9A9A9)
val OperatorBtnLightHex = Color(0xFFD6D6D6)

// Dark Mode Specifics
val AppBgDarkHex = Color(0xFF141414)
val NavTrayDarkHex = Color(0xFF1B1B1B)
val NavSliderDarkHex = Color(0xFF242527)
val CardSurfaceDarkHex = Color(0xFF1D1E20)
val PillBoxDarkHex = Color(0xFF474747)
val DarkGrayHintHex = Color(0xFF717171)
val UnhighlightedTextDarkHex = Color(0xFF7B7B7B)
val OperatorBtnDarkHex = Color(0xFF3D3D3D)

val DashboardBtnBgLight = Color(0xFFEBEBEB)
val DashboardBtnBgDark = Color(0xFF3E3E3E)


// ==========================================
// 2. SEMANTIC COLORS (Self-Explanatory Use Cases)
// ==========================================

// --- Backgrounds ---
val AppBackgroundLight = AppBgLightHex
val AppBackgroundDark = AppBgDarkHex

// --- Navigation Bar ---
val NavBarTrayLight = NavTrayLightHex
val NavBarTrayDark = NavTrayDarkHex
val NavBarSliderLight = SolidWhite
val NavBarSliderDark = NavSliderDarkHex
val NavBarIconActiveLight = SolidBlack
val NavBarIconActiveDark = SolidWhite
val NavBarIconInactiveLight = LightGrayHintHex
val NavBarIconInactiveDark = DarkGrayHintHex

// --- Cards & Surfaces (Tabs, Invoice, Transactions) ---
val SurfaceBackgroundLight = SolidWhite
val SurfaceBackgroundDark = CardSurfaceDarkHex
val TextBoxBackgroundLight = SolidWhite
val TextBoxBackgroundDark = NavSliderDarkHex // #242527
val MonthPillBackgroundLight = OperatorBtnLightHex // #D6D6D6
val MonthPillBackgroundDark = PillBoxDarkHex // #474747

// --- Typography ---
val TextPrimaryLight = SolidBlack
val TextPrimaryDark = SolidWhite
val TextHintLight = LightGrayHintHex       // Email ID, Password, Info Text
val TextHintDark = DarkGrayHintHex         // Email ID, Password, Info Text
val TextSecondaryLight = Black50Opacity    // Faded titles, separator lines
val TextSecondaryDark = White50Opacity     // Faded titles, separator lines
val TextInvoiceDark = White75Opacity       // Rent invoice specific text
val TextUnhighlightedPillDark = UnhighlightedTextDarkHex // Lend/Borrow inactive

// --- Buttons & Interactive Elements ---
val PrimaryButtonBgLight = SolidBlack
val PrimaryButtonBgDark = SolidWhite
val PrimaryButtonTextLight = SolidWhite
val PrimaryButtonTextDark = SolidBlack

// --- Calculator Specifics ---
val CalcNumberBgLight = SolidWhite
val CalcNumberBgDark = NavSliderDarkHex    // #242527
val CalcOperatorBgLight = OperatorBtnLightHex
val CalcOperatorBgDark = OperatorBtnDarkHex
val CalcTextLight = SolidBlack
val CalcTextDark = SolidWhite