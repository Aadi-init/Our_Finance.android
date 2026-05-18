package com.altf4.ourfinance.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.altf4.ourfinance.R
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.spring
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import com.altf4.ourfinance.ui.theme.OurFinanceTheme

@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onVisibilityToggle: () -> Unit = {}
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

// 1. The "OR" Separator
@Composable
fun OrSeparator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = " OR ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 2. The Google Button
@Composable
fun GoogleButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 100% Native icon. No external .png or .xml files required.
            Icon(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Account Logo",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Continue with Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SubmitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Always include a modifier for flexibility
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ColumnScope.DisplayText(text: String) { // Added 'ColumnScope.' here

    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        fontSize = 40.sp,
        lineHeight = 50.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )

}

@Composable
fun OtpBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = {
            if (it.length <= 1) onValueChange(it)
        },
        modifier = modifier
            .size(width = 64.dp, height = 56.dp), // Square-ish shape
        shape = RoundedCornerShape(15.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    )
}

// 1. Define the tabs with your exact custom XML drawable resources
enum class NavScreen(val iconResId: Int, val contentDescription: String) {
    Dashboard(R.drawable.ic_dashboard, "Dashboard"),
    Expenses(R.drawable.ic_expenses, "Expenses"),
    Settlement(R.drawable.ic_settlements, "Settlements"),
    Accessibility(R.drawable.ic_accessibility, "Accessibility")
}

@Composable
fun PillNavigationBar(
    currentScreen: NavScreen,
    onScreenSelected: (NavScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    // 2. Automatically link to your theme.kt color assignments
    val trayColor = MaterialTheme.colorScheme.surfaceContainerLow
    val sliderColor = MaterialTheme.colorScheme.surfaceVariant
    val selectedIconColor = MaterialTheme.colorScheme.primary
    val unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant

    val items = NavScreen.entries
    val selectedIndex = currentScreen.ordinal

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(50))
            .background(trayColor)
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val itemWidth = totalWidth / items.size

        // Smooth spring physics slider transition
        val animatedOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "PillSliderOffset"
        )

        // Sliding background capsule
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(sliderColor)
        )

        // Interactive touch icons layer
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { screen ->
                val isSelected = screen == currentScreen

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Removes standard circle ripple for a clean sliding look
                        ) {
                            onScreenSelected(screen)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        // 3. Render your custom XML drawables using painterResource
                        painter = painterResource(id = screen.iconResId),
                        contentDescription = screen.contentDescription,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) selectedIconColor else unselectedIconColor
                    )
                }
            }
        }
    }
}


@Preview(name = "Light Mode UI", showBackground = true)
@Preview(
    name = "Dark Mode UI",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES // Forces the design panel into dark mode
)
@Composable
fun PillNavigationBarPreview() {
    OurFinanceTheme {
        // Keeps track of the active tab click state inside the preview canvas
        var selectedScreen by remember { mutableStateOf(NavScreen.Dashboard) }

        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    PillNavigationBar(
                        currentScreen = selectedScreen,
                        onScreenSelected = { selectedScreen = it }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}