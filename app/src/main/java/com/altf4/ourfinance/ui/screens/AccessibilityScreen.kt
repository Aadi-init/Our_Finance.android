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
import androidx.navigation.compose.rememberNavController
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@Composable
fun AccessibilityScreen(
    currentUser: GoogleUser,
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel,
    navController: NavController,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkModeEnabled by themeViewModel.isDarkMode.collectAsState()
    val dynamicIconEnabled by themeViewModel.isDynamicIconEnabled.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    AccessibilityScreenContent(
        currentUser = currentUser,
        darkModeEnabled = darkModeEnabled,
        dynamicIconEnabled = dynamicIconEnabled,
        onDarkModeToggle = { themeViewModel.toggleDarkMode(it) },
        onDynamicIconToggle = { themeViewModel.setDynamicIconEnabled(it) },
        onUpdateProfile = { name, imageUri, onResult ->
            coroutineScope.launch {
                val nameUpdateSuccess = authViewModel.updateUserProfile(name = name)
                if (imageUri != null) {
                    authViewModel.uploadProfileImage(imageUri, context) { success, message ->
                        onResult(success, message)
                    }
                } else {
                    onResult(nameUpdateSuccess, if (nameUpdateSuccess) null else "Failed to update name")
                }
            }
        },
        onUpdatePassword = { password, onResult ->
            coroutineScope.launch {
                val success = authViewModel.updatePasswordOnly(password)
                onResult(success)
            }
        },
        onLogoutClick = onLogoutClick,
        navController = navController,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreenContent(
    currentUser: GoogleUser,
    darkModeEnabled: Boolean,
    dynamicIconEnabled: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onDynamicIconToggle: (Boolean) -> Unit,
    onUpdateProfile: (String, Uri?, (Boolean, String?) -> Unit) -> Unit,
    onUpdatePassword: (String, (Boolean) -> Unit) -> Unit,
    onLogoutClick: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    initialProfileSheetOpen: Boolean = false,
    initialPasswordSheetOpen: Boolean = false
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("OurFinancePrefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var notificationsEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("push_notifications_enabled", true))
    }

    // Profile Bottom Sheet states
    var isProfileSheetOpen by remember { mutableStateOf(initialProfileSheetOpen) }
    val profileSwipeOffsetY = remember { Animatable(0f) }
    var isSaving by remember { mutableStateOf(false) }

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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                val cachedPath = saveUriToInternalStorage(
                    context = context,
                    uri = uri,
                    fileName = "profile_picture_${currentUser.apiParamName}.jpg"
                )
                if (cachedPath != null) {
                    editedPhotoUrl = cachedPath
                } else {
                    Toast.makeText(context, "Error caching image locally.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    LaunchedEffect(isProfileSheetOpen) {
        if (isProfileSheetOpen) profileSwipeOffsetY.snapTo(0f)
    }

    LaunchedEffect(isPasswordSheetOpen) {
        if (isPasswordSheetOpen) {
            passwordSwipeOffsetY.snapTo(0f)
            newPassword = ""
            confirmPassword = ""
            newPasswordVisible = false
            confirmPasswordVisible = false
        }
    }

    LaunchedEffect(currentUser) {
        editedName = currentUser.displayName ?: ""
        editedPhotoUrl = currentUser.profilePictureUrl ?: ""
    }

    val isPasswordMismatched = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
    var toastShownForMismatch by remember { mutableStateOf(false) }

    LaunchedEffect(newPassword, confirmPassword) {
        if (isPasswordMismatched) {
            if (!toastShownForMismatch) {
                Toast.makeText(context, "The passwords should match.", Toast.LENGTH_SHORT).show()
                toastShownForMismatch = true
            }
        } else {
            toastShownForMismatch = false
        }
    }

    val isProfileEdited = editedName != (currentUser.displayName ?: "") || selectedImageUri != null
    val isAnySheetOpen = isProfileSheetOpen || isPasswordSheetOpen

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .blur(if (isAnySheetOpen) 12.dp else 0.dp),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CustomTopBar(
                    title = "Accessibility",
                    actions = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.035f)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .clickable { navController.navigate(Screen.About.route) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "V 1.0",
                                    style = MaterialTheme.typography.labelMedium,
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
                                NavScreen.Dashboard -> navController.popBackStack(Screen.Dashboard.route, false)
                                NavScreen.Expenses -> navController.navigate(Screen.Expenses.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                                NavScreen.Settlement -> navController.navigate(Screen.Settlements.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                                else -> {}
                            }
                        }
                    },
                    modifier = Modifier.navigationBarsPadding()
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
                        .padding(horizontal = 15.dp, vertical = if (isSmallScreen) 10.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 10.dp else 20.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.225f),   //.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
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
                                        .size(if (isSmallScreen) 40.dp else 48.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = editedName.ifEmpty { "User" },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontSize = if (isSmallScreen) 18.sp else 22.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = currentUser.email,
                                        style = MaterialTheme.typography.bodyMedium,
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
                                    style = MaterialTheme.typography.bodyLarge,
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

                    Card(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f),   //.weight(1.5f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                    style = MaterialTheme.typography.bodyLarge,
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

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                ScaleableSwitch(
                                    checked = darkModeEnabled,
                                    onCheckedChange = { onDarkModeToggle(it) },
                                    scale = 0.9f
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                ScaleableSwitch(
                                    checked = dynamicIconEnabled,
                                    onCheckedChange = { onDynamicIconToggle(it) },
                                    scale = 0.9f
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.275f).clickable { onLogoutClick() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
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
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

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
                        if (!isSaving) {
                            isProfileSheetOpen = false
                            isPasswordSheetOpen = false
                        }
                    }
            )
        }

        // PROFILE SHEET
        val profileBorderColor = MaterialTheme.colorScheme.surfaceVariant
        AnimatedVisibility(
            visible = isProfileSheetOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(500)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(450))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .offset { IntOffset(0, profileSwipeOffsetY.value.roundToInt()) }
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .drawWithContent {
                        drawContent()
                        val strokePx = 2.dp.toPx()
                        val cornerRadiusPx = 34.dp.toPx()
                        val width = size.width
                        val topBorderPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, cornerRadiusPx)
                            quadraticTo(0f, 0f, cornerRadiusPx, 0f)
                            lineTo(width - cornerRadiusPx, 0f)
                            quadraticTo(width, 0f, width, cornerRadiusPx)
                        }
                        drawPath(topBorderPath, profileBorderColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(24.dp).pointerInput(Unit) {
                            if (!isSaving) {
                                detectDragGestures(
                                    onDragEnd = {
                                        if (profileSwipeOffsetY.value > 80f) isProfileSheetOpen = false
                                        else coroutineScope.launch { profileSwipeOffsetY.animateTo(0f, tween(200)) }
                                    },
                                    onDragCancel = { coroutineScope.launch { profileSwipeOffsetY.animateTo(0f, tween(200)) } },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        coroutineScope.launch { profileSwipeOffsetY.snapTo((profileSwipeOffsetY.value + dragAmount.y).coerceAtLeast(0f)) }
                                    }
                                )
                            }
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.width(36.dp).height(5.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.outlineVariant))
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Box(
                        modifier = Modifier.size(130.dp).clip(CircleShape).clickable(enabled = !isSaving) {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = editedPhotoUrl.ifEmpty {
                                    "https://ui-avatars.com/api/?name=${currentUser.apiParamName}&background=22C55E&color=fff"
                                }
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Name", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            BasicTextField(
                                value = editedName,
                                onValueChange = { if (!isSaving) editedName = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                                decorationBox = { inner ->
                                    Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) { inner() }
                                }
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Email", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
                                Text(text = currentUser.email, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    AnimatedVisibility(visible = isProfileEdited, enter = fadeIn() + scaleIn(initialScale = 0.85f), exit = fadeOut() + scaleOut(targetScale = 0.85f)) {
                        Button(
                            onClick = {
                                if (!isSaving) {
                                    isSaving = true
                                    onUpdateProfile(editedName, selectedImageUri) { success, message ->
                                        isSaving = false
                                        if (success) {
                                            selectedImageUri = null
                                            isProfileSheetOpen = false
                                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, message ?: "Error.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.6f).height(46.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground, contentColor = MaterialTheme.colorScheme.onPrimary),
                            enabled = !isSaving
                        ) {
                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.5.dp)
                            else Text(text = "Save", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // PASSWORD SHEET
        val passwordBorderColor = MaterialTheme.colorScheme.surfaceVariant
        AnimatedVisibility(
            visible = isPasswordSheetOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(500)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(450))
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.60f)
                    .offset { IntOffset(0, passwordSwipeOffsetY.value.roundToInt()) }
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .drawWithContent {
                        drawContent()
                        val strokePx = 2.dp.toPx()
                        val cornerRadiusPx = 34.dp.toPx()
                        val width = size.width
                        val topBorderPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, cornerRadiusPx)
                            quadraticTo(0f, 0f, cornerRadiusPx, 0f)
                            lineTo(width - cornerRadiusPx, 0f)
                            quadraticTo(width, 0f, width, cornerRadiusPx)
                        }
                        drawPath(topBorderPath, passwordBorderColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    }
            ) {
                val sheetHeight = maxHeight
                val isSmallSheet = sheetHeight < 400.dp

                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(24.dp).pointerInput(Unit) {
                            if (!isSaving) {
                                detectDragGestures(
                                    onDragEnd = {
                                        if (passwordSwipeOffsetY.value > 80f) isPasswordSheetOpen = false
                                        else coroutineScope.launch { passwordSwipeOffsetY.animateTo(0f, tween(200)) }
                                    },
                                    onDragCancel = { coroutineScope.launch { passwordSwipeOffsetY.animateTo(0f, tween(200)) } },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        coroutineScope.launch { passwordSwipeOffsetY.snapTo((passwordSwipeOffsetY.value + dragAmount.y).coerceAtLeast(0f)) }
                                    }
                                )
                            }
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.width(36.dp).height(5.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.outlineVariant))
                    }

                    Spacer(modifier = Modifier.height(if (isSmallSheet) 12.dp else 28.dp))

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (isSmallSheet) 8.dp else 16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "New Password", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            CustomInputField(value = newPassword, onValueChange = { if (!isSaving) newPassword = it }, label = "", leadingIcon = Icons.Default.Lock, isPassword = true, passwordVisible = newPasswordVisible, onVisibilityToggle = { newPasswordVisible = !newPasswordVisible })
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Confirm Password", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            CustomInputField(value = confirmPassword, onValueChange = { if (!isSaving) confirmPassword = it }, label = "", leadingIcon = Icons.Default.Lock, isPassword = true, passwordVisible = confirmPasswordVisible, onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible })
                            if (isPasswordMismatched) {
                                Text(text = "Passwords should match.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    val isPasswordMatching = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword == confirmPassword
                    AnimatedVisibility(visible = isPasswordMatching, enter = fadeIn() + scaleIn(initialScale = 0.85f), exit = fadeOut() + scaleOut(targetScale = 0.85f)) {
                        Button(
                            onClick = {
                                if (!isSaving) {
                                    isSaving = true
                                    onUpdatePassword(newPassword) { success ->
                                        isSaving = false
                                        if (success) {
                                            isPasswordSheetOpen = false
                                            Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(0.6f).height(46.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground, contentColor = MaterialTheme.colorScheme.onPrimary),
                            enabled = !isSaving
                        ) {
                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.5.dp)
                            else Text(text = "Save", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun saveUriToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
    return try {
        val destinationFolder = File(context.filesDir, "profile_pictures").apply { if (!exists()) mkdirs() }
        val destinationFile = File(destinationFolder, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destinationFile).use { output -> input.copyTo(output) }
        }
        destinationFile.absolutePath
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true, name = "Accessibility Light")
@Composable
fun AccessibilityScreenPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    OurFinanceTheme(darkTheme = false) {
        AccessibilityScreenContent(
            currentUser = mockUser,
            darkModeEnabled = false,
            dynamicIconEnabled = false,
            onDarkModeToggle = {},
            onDynamicIconToggle = {},
            onUpdateProfile = { _, _, _ -> },
            onUpdatePassword = { _, _ -> },
            onLogoutClick = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Accessibility Dark")
@Composable
fun AccessibilityScreenPreviewDark() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    OurFinanceTheme(darkTheme = true) {
        AccessibilityScreenContent(
            currentUser = mockUser,
            darkModeEnabled = true,
            dynamicIconEnabled = false,
            onDarkModeToggle = {},
            onDynamicIconToggle = {},
            onUpdateProfile = { _, _, _ -> },
            onUpdatePassword = { _, _ -> },
            onLogoutClick = {},
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, name = "Profile Sheet Light")
@Composable
fun ProfileSheetPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    OurFinanceTheme(darkTheme = false) {
        AccessibilityScreenContent(
            currentUser = mockUser,
            darkModeEnabled = false,
            dynamicIconEnabled = false,
            onDarkModeToggle = {},
            onDynamicIconToggle = {},
            onUpdateProfile = { _, _, _ -> },
            onUpdatePassword = { _, _ -> },
            onLogoutClick = {},
            navController = rememberNavController(),
            initialProfileSheetOpen = true
        )
    }
}

@Preview(showBackground = true, name = "Password Sheet Light")
@Composable
fun PasswordSheetPreviewLight() {
    val mockUser = GoogleUser("Arnab Banik", "arnab.banik299@gmail.com", null, "Arnab")
    OurFinanceTheme(darkTheme = false) {
        AccessibilityScreenContent(
            currentUser = mockUser,
            darkModeEnabled = false,
            dynamicIconEnabled = false,
            onDarkModeToggle = {},
            onDynamicIconToggle = {},
            onUpdateProfile = { _, _, _ -> },
            onUpdatePassword = { _, _ -> },
            onLogoutClick = {},
            navController = rememberNavController(),
            initialPasswordSheetOpen = true
        )
    }
}
