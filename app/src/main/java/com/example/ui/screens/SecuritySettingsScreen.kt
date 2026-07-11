package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.viewmodel.MainViewModel
import java.text.DecimalFormat

// --- ONBOARDING SCREEN ---
@Composable
fun OnboardingScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()
    var currentSlide by remember { mutableStateOf(0) }

    val slides = listOf(
        Triple(
            "Creative Workspace",
            "Welcome to Lumina Studio Pro, an all-in-one professional creative suite. Empower your designs with responsive, offline-first production tools.",
            Icons.Default.Brush
        ),
        Triple(
            "AI Utilities Included",
            "Write color-coded notes, track financial spending charts, calculate scientific formulas, and translate text instantly using Gemini AI.",
            Icons.Default.AutoAwesome
        ),
        Triple(
            "Local Sandbox Secured",
            "Private local storage files, on-device matrix photo filter rendering, and a personal calendar schedule. All secured with optional PIN lock.",
            Icons.Default.Lock
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Logo & Branding
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.app_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Lumina Studio Pro",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Ultimate High-End Creative and Utility Workspace",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD0BCFF),
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Carousel Slide content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = slides[currentSlide].third,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = slides[currentSlide].first,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = slides[currentSlide].second,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // Bottom Actions & Dot Indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    slides.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (currentSlide == idx) 20.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (currentSlide == idx) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentSlide < slides.size - 1) {
                            currentSlide++
                        } else {
                            viewModel.completeOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_next_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (currentSlide == slides.size - 1) "Enter Workspace" else "Next",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// --- PIN SECURITY LOCK ENTRY SCREEN ---
@Composable
fun PinEntryScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Security", tint = accentColor, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(14.dp))
                Text("Secure Workspace Lock", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Please enter your 4-digit PIN", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Dots Display
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = viewModel.pinEntryInput.length > i
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(width = 2.dp, color = accentColor, shape = CircleShape)
                            .background(if (isFilled) accentColor else Color.Transparent)
                    )
                }
            }

            viewModel.pinEntryError?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Numeric keypad grid
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 30.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )

                keys.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { digit ->
                            if (digit.isEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.2f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .clickable {
                                            if (digit == "DEL") {
                                                viewModel.deletePinDigit()
                                            } else {
                                                viewModel.handlePinInput(digit)
                                            }
                                        }
                                        .testTag("pin_key_$digit"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (digit == "DEL") {
                                        Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = accentColor)
                                    } else {
                                        Text(digit, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- GLOBAL SEARCH RESULT PANEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()
    val df = DecimalFormat("$#,##0.00")

    val notes by viewModel.activeNotes.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    val query = viewModel.globalQuery

    val matchingNotes = notes.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
    val matchingTasks = tasks.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    val matchingEvents = events.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
    val matchingExpenses = expenses.filter { it.title.contains(query, ignoreCase = true) }

    val totalResultsCount = matchingNotes.size + matchingTasks.size + matchingEvents.size + matchingExpenses.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Results", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.globalQuery = ""
                        viewModel.currentScreen = "dashboard"
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.globalQuery,
                onValueChange = { viewModel.globalQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .testTag("global_search_input"),
                placeholder = { Text("Type query to search everything...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                singleLine = true
            )

            Text(
                text = "Found $totalResultsCount matching entries",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (totalResultsCount == 0) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No results match your search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Notes Matches
                    if (matchingNotes.isNotEmpty()) {
                        item { Text("Notes", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 14.sp) }
                        items(matchingNotes.size) { idx ->
                            val note = matchingNotes[idx]
                            Card(
                                onClick = { viewModel.editNote(note) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(note.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(note.content, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }

                    // Tasks Matches
                    if (matchingTasks.isNotEmpty()) {
                        item { Text("Tasks", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp)) }
                        items(matchingTasks.size) { idx ->
                            val task = matchingTasks[idx]
                            Card(
                                onClick = { viewModel.currentScreen = "tasks" },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = accentColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // Events Matches
                    if (matchingEvents.isNotEmpty()) {
                        item { Text("Events", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp)) }
                        items(matchingEvents.size) { idx ->
                            val event = matchingEvents[idx]
                            Card(
                                onClick = { viewModel.currentScreen = "calendar" },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (event.description.isNotBlank()) {
                                        Text(event.description, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }

                    // Expenses Matches
                    if (matchingExpenses.isNotEmpty()) {
                        item { Text("Expenses", fontWeight = FontWeight.Bold, color = accentColor, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp)) }
                        items(matchingExpenses.size) { idx ->
                            val exp = matchingExpenses[idx]
                            Card(
                                onClick = { viewModel.currentScreen = "expense_tracker" },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(exp.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        df.format(exp.amount),
                                        fontWeight = FontWeight.Bold,
                                        color = if (exp.isExpense) Color(0xFFEF4444) else Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SETTINGS CONTROL PANEL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()
    val context = LocalContext.current

    val accents = listOf("Emerald", "Indigo", "Crimson", "Gold", "Slate")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workspace Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = "dashboard" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SYSTEM THEME TOGGLER
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("OLED Deep Black Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = if (viewModel.isDarkMode) "Pure obsidian black active (Ultra Low Fatigue)" else "Studio Slate dark active (Cinematic low contrast)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = viewModel.isDarkMode,
                        onCheckedChange = { viewModel.isDarkMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                    )
                }
            }

            // ACCENT COLOR PICKER
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Workspace Accent Color", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        accents.forEach { acc ->
                            val col = when (acc) {
                                "Emerald" -> Color(0xFF93D39B)
                                "Indigo" -> Color(0xFFD0BCFF)
                                "Crimson" -> Color(0xFFE46962)
                                "Gold" -> Color(0xFFEFB8C8)
                                else -> Color(0xFFCAC4D0) // Slate
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        width = if (viewModel.accentColorName == acc) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.accentColorName = acc }
                                    .testTag("settings_color_$acc")
                            )
                        }
                    }
                }
            }

            // PIN PASSWORD SETTINGS
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Secure App PIN Lock", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Asks for PIN passcode on launch", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = viewModel.isPinLockEnabled,
                            onCheckedChange = { viewModel.isPinLockEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    if (viewModel.isPinLockEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = viewModel.appPinCode,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    viewModel.appPinCode = it
                                }
                            },
                            label = { Text("Passcode PIN (4 digits)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor)
                        )
                    }
                }
            }

            // VERSION & ABOUT INFO
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Smart Toolbox v1.5", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = accentColor)
                    Text("Designed with Jetpack Compose 2026", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Developer: o0778940548@gmail.com\nSecure offline-first local database. All logs, notes, financial data and documents are stored sandbox-encrypted on device.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
