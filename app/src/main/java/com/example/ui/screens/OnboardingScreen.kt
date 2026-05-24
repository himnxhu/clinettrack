package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.ClientTrackViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: ClientTrackViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var currentStep by remember { mutableStateOf(1) } // Steps 1 to 4

    // Step 1: Account setup fields
    var ownerName by remember { mutableStateOf("") }
    var ownerEmail by remember { mutableStateOf("") }
    var ownerPassword by remember { mutableStateOf("") }
    var ownerWhatsapp by remember { mutableStateOf("") }

    // Step 2: Business Type selection
    val bizTypes = listOf(
        BizTypeItem("Gym / Fitness", Icons.Default.Star, "Gyms, training centers & yoga clubs"),
        BizTypeItem("Dance Academy", Icons.Default.PlayArrow, "Dance training and performance programs"),
        BizTypeItem("Coaching / Classes", Icons.Default.Build, "Tuition classes, test preparation & schools"),
        BizTypeItem("Beauty Salon", Icons.Default.Face, "Beauty salons, cosmetics & spa styling clinics"),
        BizTypeItem("Sports Club", Icons.Default.Info, "Football, cricket & professional sports squads"),
        BizTypeItem("Yoga / Wellness", Icons.Default.Favorite, "Yoga centers, physiotherapy & well-being"),
        BizTypeItem("Clinic", Icons.Default.Menu, "Local outpatient clinics, diagnostics & dental desks"),
        BizTypeItem("Music School", Icons.Default.PlayArrow, "Instrument training and composition rooms"),
        BizTypeItem("Other", Icons.Default.Settings, "General subscription & recurring billing businesses")
    )
    var selectedBizType by remember { mutableStateOf("Gym / Fitness") }

    // Step 3: Business profile setup
    var businessName by remember { mutableStateOf("") }
    var locationCity by remember { mutableStateOf("") }
    var defaultFeeAmtStr by remember { mutableStateOf("1500") }
    var feeDueDayStr by remember { mutableStateOf("5") }

    // Local validation flags
    var showErrorMsg by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "ClientTrack",
                fontSize = 28.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "App User Flow Blueprint",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Progress Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP $currentStep OF 4",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${(currentStep * 25)}% Completed",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { currentStep / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content Holder with Crossfade for premium visual feel
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    1 -> StepAccountSetup(
                        name = ownerName,
                        onNameChange = { ownerName = it; showErrorMsg = null },
                        email = ownerEmail,
                        onEmailChange = { ownerEmail = it; showErrorMsg = null },
                        password = ownerPassword,
                        onPasswordChange = { ownerPassword = it; showErrorMsg = null },
                        whatsapp = ownerWhatsapp,
                        onWhatsappChange = { ownerWhatsapp = it; showErrorMsg = null }
                    )
                    2 -> StepBusinessTypeSelection(
                        items = bizTypes,
                        selectedType = selectedBizType,
                        onSelectType = { selectedBizType = it }
                    )
                    3 -> StepBusinessProfileSetup(
                        bizType = selectedBizType,
                        businessName = businessName,
                        onBusinessNameChange = { businessName = it; showErrorMsg = null },
                        location = locationCity,
                        onLocationChange = { locationCity = it },
                        defaultFee = defaultFeeAmtStr,
                        onDefaultFeeChange = { defaultFeeAmtStr = it; showErrorMsg = null },
                        dueDay = feeDueDayStr,
                        onDueDayChange = { feeDueDayStr = it; showErrorMsg = null }
                    )
                    4 -> StepSummaryAndLaunch(
                        ownerName = ownerName,
                        ownerEmail = ownerEmail,
                        whatsapp = ownerWhatsapp,
                        bizType = selectedBizType,
                        bizName = businessName,
                        defFee = defaultFeeAmtStr,
                        dueDay = feeDueDayStr
                    )
                }
            }

            if (showErrorMsg != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error Icon",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = showErrorMsg ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Bottom Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            showErrorMsg = null
                            currentStep--
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("onboarding_back_button"),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Back", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        // Form validations
                        when (currentStep) {
                            1 -> {
                                if (ownerName.isBlank()) {
                                    showErrorMsg = "Please enter your full name."
                                    return@Button
                                }
                                if (ownerEmail.isBlank() || !ownerEmail.contains("@")) {
                                    showErrorMsg = "Please enter a valid business email."
                                    return@Button
                                }
                                if (ownerPassword.length < 6) {
                                    showErrorMsg = "Password must be at least 6 characters."
                                    return@Button
                                }
                                if (ownerWhatsapp.isBlank() || ownerWhatsapp.length < 8) {
                                    showErrorMsg = "Please specify a valid WhatsApp number."
                                    return@Button
                                }
                                currentStep = 2
                            }
                            2 -> {
                                // Default autofill suggestions based on standard profiles
                                if (businessName.isBlank()) {
                                    businessName = when (selectedBizType) {
                                        "Gym / Fitness" -> "$ownerName's Fitness Arena"
                                        "Dance Academy" -> "Rhythm Beats Academy"
                                        "Coaching / Classes" -> "Success Point Tuition"
                                        "Beauty Salon" -> "Glow & Style Wellness Hub"
                                        "Sports Club" -> "United Sports Club"
                                        "Yoga / Wellness" -> "Prana Yoga Studio"
                                        "Clinic" -> "CarePoint Consulting Clinic"
                                        "Music School" -> "Melody Notes School"
                                        else -> "$ownerName's Business Studio"
                                    }
                                }
                                currentStep = 3
                            }
                            3 -> {
                                if (businessName.isBlank()) {
                                    showErrorMsg = "Business Name cannot be empty."
                                    return@Button
                                }
                                val fee = defaultFeeAmtStr.toDoubleOrNull()
                                if (fee == null || fee <= 0.0) {
                                    showErrorMsg = "Please enter a valid numeric default fee amount."
                                    return@Button
                                }
                                val day = feeDueDayStr.toIntOrNull()
                                if (day == null || day !in 1..31) {
                                    showErrorMsg = "Due day must be between 1 and 31."
                                    return@Button
                                }
                                currentStep = 4
                            }
                            4 -> {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                val finalFee = defaultFeeAmtStr.toDoubleOrNull() ?: 1500.0
                                val finalDueDay = feeDueDayStr.toIntOrNull() ?: 5
                                viewModel.completeSignup(
                                    ownerName = ownerName.trim(),
                                    ownerEmail = ownerEmail.trim(),
                                    passwordPlain = ownerPassword,
                                    whatsapp = ownerWhatsapp.trim(),
                                    bizType = selectedBizType,
                                    bizName = businessName.trim(),
                                    defFee = finalFee,
                                    dueDay = finalDueDay
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("onboarding_continue_button")
                        .then(if (currentStep == 1) Modifier.fillMaxWidth() else Modifier),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text(
                        text = if (currentStep == 4) "Launch Theme & Desk" else "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (currentStep == 4) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

@Composable
fun StepAccountSetup(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    whatsapp: String,
    onWhatsappChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Create Admin & Owner Profile",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Setup owner details so receipts & billing notifications are officially signed by you.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, "Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("signup_name_input")
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Business Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, "Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("signup_email_input")
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Account secure password") },
            leadingIcon = { Icon(Icons.Default.Lock, "Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("signup_password_input")
        )

        OutlinedTextField(
            value = whatsapp,
            onValueChange = onWhatsappChange,
            label = { Text("Owner WhatsApp/Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, "WhatsApp") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            placeholder = { Text("e.g. +91 98765 43210") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("signup_whatsapp_input")
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alert notifications",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WhatsApp reminder services use this default sender route setup.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StepBusinessTypeSelection(
    items: List<BizTypeItem>,
    selectedType: String,
    onSelectType: (String) -> Unit
) {
    Column {
        Text(
            text = "Select your Business Category",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Personalize customer cards, fee reminders, and milestones based on industry presets.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                val isSelected = item.name == selectedType
                Surface(
                    onClick = { onSelectType(item.name) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("biz_type_${item.name.replace(" ", "_").replace("/", "and")}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.name,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepBusinessProfileSetup(
    bizType: String,
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    defaultFee: String,
    onDefaultFeeChange: (String) -> Unit,
    dueDay: String,
    onDueDayChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Business Profile Setup",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Customized defaults dynamically preset for your category: $bizType.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            label = { Text("What's your Business/Studio Name?") },
            leadingIcon = { Icon(Icons.Default.Home, "Business Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("signup_biz_name_input")
        )

        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Location / City (Optional)") },
            leadingIcon = { Icon(Icons.Default.LocationOn, "Location") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = defaultFee,
                onValueChange = onDefaultFeeChange,
                label = { Text("Default Fee (₹)") },
                leadingIcon = { Icon(Icons.Default.ShoppingCart, "Fee") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("signup_default_fee_input")
            )

            OutlinedTextField(
                value = dueDay,
                onValueChange = onDueDayChange,
                label = { Text("Due Day (1-31)") },
                leadingIcon = { Icon(Icons.Default.DateRange, "Due Day") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .testTag("signup_due_day_input")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "💼 Smart presets active for $bizType:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                val notesText = when (bizType) {
                    "Gym / Fitness" -> "Adds body composition progress charts and target milestones automatically."
                    "Dance Academy" -> "Includes weekly attendance trackers and rhythm assessment presets."
                    "Coaching / Classes" -> "Pre-configures standard monthly subject exam tracking for children."
                    "Beauty Salon" -> "Presets customized package lists and styling preferences."
                    else -> "Enables regular billing cycles with smart WhatsApp follow-up reminders."
                }
                Text(
                    text = notesText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StepSummaryAndLaunch(
    ownerName: String,
    ownerEmail: String,
    whatsapp: String,
    bizType: String,
    bizName: String,
    defFee: String,
    dueDay: String
) {
    Column {
        Text(
            text = "Review & Launch Onboarding",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Your blueprint is configured correctly. Prepare to enter ClientTrack billing desk.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow(label = "Business Owner", value = ownerName, icon = Icons.Default.Person)
                SummaryRow(label = "Contact Email", value = ownerEmail, icon = Icons.Default.Email)
                SummaryRow(label = "WhatsApp Sync Route", value = whatsapp, icon = Icons.Default.Phone)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                SummaryRow(label = "Business Category", value = bizType, icon = Icons.Default.Info)
                SummaryRow(label = "Studio Name", value = bizName, icon = Icons.Default.Home)
                SummaryRow(label = "Preset Default Fee", value = "₹$defFee /month", icon = Icons.Default.Star)
                SummaryRow(label = "Recurring Due Date", value = "Day $dueDay of each month", icon = Icons.Default.DateRange)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Safe Lock Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Your configurations are kept 100% locally in secure device cache using modern SQLite Room schemas.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

data class BizTypeItem(
    val name: String,
    val icon: ImageVector,
    val description: String
)
