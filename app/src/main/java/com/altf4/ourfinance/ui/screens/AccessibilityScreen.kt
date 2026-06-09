package com.altf4.ourfinance.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.altf4.ourfinance.R
import com.altf4.ourfinance.data.model.GoogleUser
import com.altf4.ourfinance.navigation.Screen
import com.altf4.ourfinance.ui.CustomInputField
import com.altf4.ourfinance.ui.CustomTopBar
import com.altf4.ourfinance.ui.NavScreen
import com.altf4.ourfinance.ui.PillNavigationBar
import com.altf4.ourfinance.ui.ScaleableSwitch
import com.altf4.ourfinance.ui.theme.OurFinanceTheme
import com.altf4.ourfinance.ui.viewmodel.AuthViewModel
import com.altf4.ourfinance.ui.viewmodel.ThemeViewModel
import com.altf4.ourfinance.utils.IconSwitcher
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(
    currentUser: GoogleUser,
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel, // Injected AuthViewModel to execute updates
    navController: NavController,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialProfileSheetOpen: Boolean = false, // Added for interactive profile preview mapping
    initialPasswordSheetOpen: Boolean = false // Added for interactive password preview mapping
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("OurFinancePrefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    val darkModeEnabled by themeViewModel.isDarkMode.collectAsState()
    val dynamicIconEnabled by themeViewModel.isDynamicIconEnabled.collectAsState()
    var notificationsEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("push_notifications_enabled", true))
    }

    // Profile Bottom Sheet states
    var isProfileSheetOpen by remember { mutableStateOf(initialProfileSheetOpen) }
    val profileSwipeOffsetY = remember { Animatable(0f) }
    var isSaving by remember { mutableStateOf(false) } // Loading indicator toggle

    // Password Bottom Sheet states
    var isPasswordSheetOpen by remember { mutableStateOf(initialPasswordSheetOpen) }
    val passwordSwipeOffsetY = remember { Animatable(0f) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Dynamic state holds for editing profile
    var editedName by remember { mutableStateOf(currentUser.displayName ?: "") }
    var editedPhotoUrl by remember { mutableStateOf(currentUser.profilePictureUrl ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Register Android standard visual media picker activity launcher contract
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                // Instantly copy selected photo to our private app storage sandbox directory
                val cachedPath = saveUriToInternalStorage(
                    context = context,
                    uri = uri,
                    fileName = "profile_picture_${currentUser.apiParamName}.jpg"
                )

                if (cachedPath != null) {
                    // Update our visual state holder directly using the persistent local file path
                    editedPhotoUrl = cachedPath
                } else {
                    Toast.makeText(context, "Error caching selected image locally.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Reset profile translation offset when the sheet is opened or dismissed
    LaunchedEffect(isProfileSheetOpen) {
        if (isProfileSheetOpen) {
            profileSwipeOffsetY.snapTo(0f)
        }
    }

    // Reset password translation offset when the sheet is opened or dismissed
    LaunchedEffect(isPasswordSheetOpen) {
        if (isPasswordSheetOpen) {
            passwordSwipeOffsetY.snapTo(0f)
            newPassword = ""
            confirmPassword = ""
            newPasswordVisible = false
            confirmPasswordVisible = false
        }
    }

    // Synchronize user profile state once user loads
    LaunchedEffect(currentUser) {
        editedName = currentUser.displayName ?: ""
        editedPhotoUrl = currentUser.profilePictureUrl ?: ""
    }

    // Password fields filled and mismatch popup triggers
    val isPasswordMismatched = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
    var toastShownForMismatch by remember { mutableStateOf(false) }

    LaunchedEffect(newPassword, confirmPassword) {
        if (isPasswordMismatched) {
            if (!toastShownForMismatch) {
                Toast.makeText(context, "The New Password and Confirm Password should match.", Toast.LENGTH_SHORT).show()
                toastShownForMismatch = true
            }
        } else {
            toastShownForMismatch = false
        }
    }

    val isProfileEdited = editedName != (currentUser.displayName ?: "") ||
            editedPhotoUrl != (currentUser.profilePictureUrl ?: "")

    val isAnySheetOpen = isProfileSheetOpen || isPasswordSheetOpen

    Box(modifier = Modifier.fillMaxSize()) {
        // Main base screen structure
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .blur(if (isAnySheetOpen) 12.dp else 0.dp), // Gaussian blur overlay backdrop if any sheet is open
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CustomTopBar(
                    title = "Accessibility",
                    actions = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .clickable { navController.navigate(Screen.About.route) }
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "V 1.0",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_go),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                PillNavigationBar(
                    currentScreen = NavScreen.Accessibility,
                    onScreenSelected = { screen ->
                        if (!isAnySheetOpen) {
                            when (screen) {
                                NavScreen.Dashboard -> {
                                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                                }
                                NavScreen.Expenses -> {
                                    navController.navigate(Screen.Expenses.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                                NavScreen.Settlement -> {
                                    navController.navigate(Screen.Settlements.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                                else -> {}
                            }
                        }
                    },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 15.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // --- Account Profile & Security Card ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Row 1: Profile Information (Triggers the bottom sheet modal overlay)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isProfileSheetOpen = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = editedPhotoUrl.ifEmpty {
                                        "https://ui-avatars.com/api/?name=${currentUser.apiParamName}&background=22C55E&color=fff"
                                    }
                                ),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = editedName.ifEmpty { "User" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = currentUser.email,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Icon(
                                painter = painterResource(id = R.drawable.ic_go),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Row 2: Security Configuration (Triggers Password Modal overlay)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPasswordSheetOpen = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Password",
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_go),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // --- App Preferences ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        // Row 1: Notifications
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Notifications",
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            ScaleableSwitch(
                                checked = notificationsEnabled,
                                onCheckedChange = {
                                    notificationsEnabled = it
                                    sharedPrefs.edit().putBoolean("push_notifications_enabled", it).apply()
                                },
                                scale = 0.9f
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Row 2: Dark Mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dark_mode),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Dark Mode",
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            ScaleableSwitch(
                                checked = darkModeEnabled,
                                onCheckedChange = { isEnabled ->
                                    themeViewModel.toggleDarkMode(isEnabled)
                                    sharedPrefs.edit().putBoolean("is_dark_mode", isEnabled).apply()
                                },
                                scale = 0.9f
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        // Row 3: Dynamic Icon
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_dynamic),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Dynamic Icon",
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            ScaleableSwitch(
                                checked = dynamicIconEnabled,
                                onCheckedChange = { isEnabled ->
                                    themeViewModel.setDynamicIconEnabled(isEnabled)
                                    sharedPrefs.edit().putBoolean("dynamic_icon_enabled", isEnabled).apply()
                                },
                                scale = 0.9f
                            )
                        }
                    }
                }

                // --- Logout Card ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogoutClick() },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Backdrop dimming transition state (fades out organically with drag swipe offset)
        val dynamicAlpha by animateFloatAsState(
            targetValue = if (isAnySheetOpen) {
                val activeOffset = if (isProfileSheetOpen) profileSwipeOffsetY.value else passwordSwipeOffsetY.value
                val reduction = (activeOffset / 300f).coerceIn(0f, 1f)
                (0.1f * (1f - reduction)).coerceIn(0f, 0.1f)
            } else 0f,
            label = "alpha"
        )

        AnimatedVisibility(
            visible = isAnySheetOpen,
            enter = fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = dynamicAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Dismiss active edit panel safely on backdrop touch if we are not saving
                        if (!isSaving) {
                            isProfileSheetOpen = false
                            isPasswordSheetOpen = false
                        }
                    }
            )
        }

        // --- BOTTOM SHEET SLIDE-UP MODAL PANEL (USER PROFILE) ---
        val onPrimaryColor = MaterialTheme.colorScheme.surfaceVariant
        AnimatedVisibility(
            visible = isProfileSheetOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 500)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 450)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .offset { IntOffset(0, profileSwipeOffsetY.value.roundToInt()) } // Follows hand-pace movement
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .drawWithContent {
                        // 1. Render the actual bottom sheet background content first
                        drawContent()

                        // 2. Configure precise layout bounds in pixels
                        val strokePx = 2.dp.toPx()
                        val cornerRadiusPx = 34.dp.toPx()
                        val width = size.width

                        // Create a precise path mapping only the curved top contour
                        val topBorderPath = androidx.compose.ui.graphics.Path().apply {
                            // Start on the left vertical edge right where the top-left curve begins
                            moveTo(0f, cornerRadiusPx)

                            // Curve up and right to the flat top edge
                            quadraticTo(0f, 0f, cornerRadiusPx, 0f)

                            // Draw a flat line across the top edge to the start of the next curve
                            lineTo(width - cornerRadiusPx, 0f)

                            // Curve down and right to complete the top-right corner
                            quadraticTo(width, 0f, width, cornerRadiusPx)
                        }

                        // 3. Render the path strictly onto the upper perimeter
                        drawPath(
                            path = topBorderPath,
                            color = onPrimaryColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokePx,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round // Keeps the edges smooth
                            )
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // A. Dismissal Handle / Drag Indicator (Wrapped in large touch target for seamless dragging)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .pointerInput(Unit) {
                                if (!isSaving) { // Disable dragging while saving
                                    detectDragGestures(
                                        onDragEnd = {
                                            // Lifted touch: dismiss if swiped past threshold, otherwise snap back up smoothly
                                            if (profileSwipeOffsetY.value > 80f) {
                                                isProfileSheetOpen = false
                                            } else {
                                                coroutineScope.launch {
                                                    profileSwipeOffsetY.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(durationMillis = 200)
                                                    )
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coroutineScope.launch {
                                                profileSwipeOffsetY.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = tween(durationMillis = 200)
                                                )
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            // Accumulate offset downwards only
                                            coroutineScope.launch {
                                                profileSwipeOffsetY.snapTo(
                                                    (profileSwipeOffsetY.value + dragAmount.y).coerceAtLeast(0f)
                                                )
                                            }
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // B. Enlarged Profile Photo
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            //.border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable(enabled = !isSaving) {
                                // Tapping immediately triggers Android's secure Photo Picker (no dialog prompt)
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = editedPhotoUrl.ifEmpty {
                                    "https://ui-avatars.com/api/?name=${currentUser.apiParamName}&background=22C55E&color=fff"
                                }
                            ),
                            contentDescription = "User Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // C. Input Form Field
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Name Field (Editable)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Name",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            BasicTextField(
                                value = editedName,
                                onValueChange = { if (!isSaving) editedName = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // 2. Email Field (Read-Only)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Email",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = currentUser.email,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // D. Primary "Save" Action Button (Only renders conditionally)
                    AnimatedVisibility(
                        visible = isProfileEdited,
                        enter = fadeIn() + scaleIn(initialScale = 0.85f),
                        exit = fadeOut() + scaleOut(targetScale = 0.85f)
                    ) {
                        Button(
                            onClick = {
                                if (!isSaving) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        val nameUpdateSuccess = authViewModel.updateUserProfile(
                                            name = editedName
                                        )
                                        
                                        if (selectedImageUri != null) {
                                            authViewModel.uploadProfileImage(
                                                imageUri = selectedImageUri!!,
                                                context = context
                                            ) { success, message ->
                                                isSaving = false
                                                if (success) {
                                                    selectedImageUri = null
                                                    isProfileSheetOpen = false
                                                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, message ?: "Failed to upload image.", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        } else {
                                            isSaving = false
                                            if (nameUpdateSuccess) {
                                                isProfileSheetOpen = false
                                                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Connection error. Failed to save profile.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(46.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Save",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // --- BOTTOM SHEET SLIDE-UP MODAL PANEL (PASSWORD CHANGE) ---
        val onBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
        AnimatedVisibility(
            visible = isPasswordSheetOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 500)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 450)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.60f)
                    .offset { IntOffset(0, passwordSwipeOffsetY.value.roundToInt()) } // Follows hand-pace movement
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .drawWithContent {
                        // 1. Render the actual bottom sheet background content first
                        drawContent()

                        // 2. Configure precise layout bounds in pixels
                        val strokePx = 2.dp.toPx()
                        val cornerRadiusPx = 34.dp.toPx()
                        val width = size.width

                        // Create a precise path mapping only the curved top contour
                        val topBorderPath = androidx.compose.ui.graphics.Path().apply {
                            // Start on the left vertical edge right where the top-left curve begins
                            moveTo(0f, cornerRadiusPx)

                            // Curve up and right to the flat top edge
                            quadraticTo(0f, 0f, cornerRadiusPx, 0f)

                            // Draw a flat line across the top edge to the start of the next curve
                            lineTo(width - cornerRadiusPx, 0f)

                            // Curve down and right to complete the top-right corner
                            quadraticTo(width, 0f, width, cornerRadiusPx)
                        }

                        // 3. Render the path strictly onto the upper perimeter
                        drawPath(
                            path = topBorderPath,
                            color = onBackgroundColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokePx,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round // Keeps the edges smooth
                            )
                        )
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // A. Dismissal Handle / Drag Indicator (Wrapped in large touch target for seamless dragging)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .pointerInput(Unit) {
                                if (!isSaving) { // Disable dragging while saving
                                    detectDragGestures(
                                        onDragEnd = {
                                            // Lifted touch: dismiss if swiped past threshold, otherwise snap back up smoothly
                                            if (passwordSwipeOffsetY.value > 80f) {
                                                isPasswordSheetOpen = false
                                            } else {
                                                coroutineScope.launch {
                                                    passwordSwipeOffsetY.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(durationMillis = 200)
                                                    )
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            coroutineScope.launch {
                                                passwordSwipeOffsetY.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = tween(durationMillis = 200)
                                                )
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            // Accumulate offset downwards only
                                            coroutineScope.launch {
                                                passwordSwipeOffsetY.snapTo(
                                                    (passwordSwipeOffsetY.value + dragAmount.y).coerceAtLeast(0f)
                                                )
                                            }
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // B. Password Input Form Fields (Vertical stack using CustomInputField)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. New Password Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "New Password",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            CustomInputField(
                                value = newPassword,
                                onValueChange = { if (!isSaving) newPassword = it },
                                label = "",
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = newPasswordVisible,
                                onVisibilityToggle = { newPasswordVisible = !newPasswordVisible }
                            )
                        }

                        // 2. Confirm Password Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Confirm Password",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            CustomInputField(
                                value = confirmPassword,
                                onValueChange = { if (!isSaving) confirmPassword = it },
                                label = "",
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = confirmPasswordVisible,
                                onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
                            )

                            if (isPasswordMismatched) {
                                Text(
                                    text = "The New Password and Confirm Password should match.",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // C. Primary "Save" Action Button (Only visible if passwords are filled and match)
                    val isPasswordMatching = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword == confirmPassword
                    AnimatedVisibility(
                        visible = isPasswordMatching,
                        enter = fadeIn() + scaleIn(initialScale = 0.85f),
                        exit = fadeOut() + scaleOut(targetScale = 0.85f)
                    ) {
                        Button(
                            onClick = {
                                if (!isSaving) {
                                    isSaving = true
                                    coroutineScope.launch {
                                        val isSuccess = authViewModel.updatePasswordOnly(newPassword)
                                        isSaving = false
                                        if (isSuccess) {
                                            isPasswordSheetOpen = false
                                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to update password. Try again later.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(46.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Save",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Copies a system content URI (e.g. from Photo Picker) to the app's secure internal sandbox.
 * This guarantees the path remains persistent and accessible even after process death or reboots.
 */
private fun saveUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    return try {
        // Create dedicated directory inside internal private app files filesDir
        val destinationFolder = File(context.filesDir, "profile_pictures").apply {
            if (!exists()) mkdirs()
        }
        val destinationFile = File(destinationFolder, fileName)

        // Read stream and copy bytes to local sandbox file
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // Return absolute path to store and load later
        destinationFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Preview(showBackground = true, name = "Accessibility Light")
@Composable
fun AccessibilityScreenPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockContext = LocalContext.current
    val mockApp = mockContext.applicationContext as android.app.Application
    OurFinanceTheme(darkTheme = false) {
        AccessibilityScreen(
            currentUser = mockUser,
            themeViewModel = ThemeViewModel(),
            authViewModel = AuthViewModel(mockApp),
            navController = NavController(mockContext),
            onLogoutClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Accessibility Dark")
@Composable
fun AccessibilityScreenPreviewDark() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockContext = LocalContext.current
    val mockApp = mockContext.applicationContext as android.app.Application
    OurFinanceTheme(darkTheme = true) {
        AccessibilityScreen(
            currentUser = mockUser,
            themeViewModel = ThemeViewModel(),
            authViewModel = AuthViewModel(mockApp),
            navController = NavController(mockContext),
            onLogoutClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Profile Sheet Light")
@Composable
fun ProfileSheetPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockContext = LocalContext.current
    val mockApp = mockContext.applicationContext as android.app.Application
    OurFinanceTheme(darkTheme = false) {
        AccessibilityScreen(
            currentUser = mockUser,
            themeViewModel = ThemeViewModel(),
            authViewModel = AuthViewModel(mockApp),
            navController = NavController(mockContext),
            onLogoutClick = {},
            initialProfileSheetOpen = true
        )
    }
}

@Preview(showBackground = true, name = "Profile Sheet Dark")
@Composable
fun ProfileSheetPreviewDark() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockContext = LocalContext.current
    val mockApp = mockContext.applicationContext as android.app.Application
    OurFinanceTheme(darkTheme = true) {
        AccessibilityScreen(
            currentUser = mockUser,
            themeViewModel = ThemeViewModel(),
            authViewModel = AuthViewModel(mockApp),
            navController = NavController(mockContext),
            onLogoutClick = {},
            initialProfileSheetOpen = true
        )
    }
}

@Preview(showBackground = true, name = "Password Sheet Light")
@Composable
fun PasswordSheetPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockContext = LocalContext.current
    val mockApp = mockContext.applicationContext as android.app.Application
    OurFinanceTheme(darkTheme = false) {
        AccessibilityScreen(
            currentUser = mockUser,
            themeViewModel = ThemeViewModel(),
            authViewModel = AuthViewModel(mockApp),
            navController = NavController(mockContext),
            onLogoutClick = {},
            initialPasswordSheetOpen = true
        )
    }
}

@Preview(showBackground = true, name = "Password Sheet Dark")
@Composable
fun PasswordSheetPreviewDark() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    val mockContext = LocalContext.current
    val mockApp = mockContext.applicationContext as android.app.Application
    OurFinanceTheme(darkTheme = true) {
        AccessibilityScreen(
            currentUser = mockUser,
            themeViewModel = ThemeViewModel(),
            authViewModel = AuthViewModel(mockApp),
            navController = NavController(mockContext),
            onLogoutClick = {},
            initialPasswordSheetOpen = true
        )
    }
}