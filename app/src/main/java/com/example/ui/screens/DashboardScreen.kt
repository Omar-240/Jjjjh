package com.example.ui.screens

import android.graphics.Bitmap
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DashboardCard
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()
    var isEditMode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Internal Dashboard Prompt state for the AI Assistant widget
    var aiConsultantPrompt by remember { mutableStateOf("") }
    var aiConsultantResponse by remember { mutableStateOf<String?>(null) }
    var isConsultantThinking by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Decorative Ambient Neon Glows
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
                .blur(50.dp)
        )
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-120).dp, y = 150.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFF007F).copy(alpha = 0.12f), Color.Transparent)
                    )
                )
                .blur(60.dp)
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // High-end glowing logo mark
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(accentColor, Color(0xFFFF007F))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Studio Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Lumina Studio",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFFFAFAFC),
                                    letterSpacing = 0.5.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Text(
                                        text = "PRO WORKSPACE ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD0BCFF),
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { isEditMode = !isEditMode },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F1E29))
                                .border(1.dp, Color(0xFF323045), CircleShape)
                                .testTag("dashboard_edit_button")
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Tune,
                                contentDescription = "Edit Dashboard",
                                tint = if (isEditMode) accentColor else Color(0xFFE2E2EA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFEFB8C8), Color(0xFF855462)),
                                    )
                                )
                                .border(1.5.dp, accentColor, CircleShape)
                                .clickable { viewModel.currentScreen = "settings" }
                                .testTag("dashboard_settings_button")
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // TRANSLUCENT PREMIUM SEARCH BAR
                    TextField(
                        value = viewModel.globalQuery,
                        onValueChange = {
                            viewModel.globalQuery = it
                            if (it.isNotBlank()) {
                                viewModel.currentScreen = "search"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF323045),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .testTag("dashboard_search_input"),
                        placeholder = { Text("Search tools, notes, or timelines...", color = Color(0xFF8E8D9C), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = Color(0xFF8E8D9C), modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF161520).copy(alpha = 0.85f),
                            unfocusedContainerColor = Color(0xFF161520).copy(alpha = 0.85f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFFF2F2F7),
                            unfocusedTextColor = Color(0xFFF2F2F7)
                        ),
                        singleLine = true
                    )

                    // CHANNELS & PRO ENGINE SECTORS
                    if (!isEditMode) {

                        // NEW INTERACTIVE AI CREATIVE COMPANION
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "AI Creative Consultant",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = accentColor,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ask Gemini for creative color palettes, film transition ideas, or photography aesthetics.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFAFAFC).copy(alpha = 0.7f),
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = aiConsultantPrompt,
                                        onValueChange = { aiConsultantPrompt = it },
                                        placeholder = { Text("Describe aesthetic (e.g., Neon Vaporwave)", fontSize = 11.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = accentColor,
                                            unfocusedBorderColor = Color(0xFF2E2C3F),
                                            focusedLabelColor = accentColor,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (aiConsultantPrompt.isNotBlank()) {
                                                isConsultantThinking = true
                                                coroutineScope.launch {
                                                    try {
                                                        // Request to Gemini for visual inspiration
                                                        val resp = viewModel.generateStudioInspiration(aiConsultantPrompt)
                                                        aiConsultantResponse = resp
                                                    } catch (e: Exception) {
                                                        aiConsultantResponse = "Try checking your connection or Secrets API Key settings."
                                                    } finally {
                                                        isConsultantThinking = false
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.height(44.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                        contentPadding = PaddingValues(horizontal = 14.dp)
                                    ) {
                                        if (isConsultantThinking) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Text("Ask", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }

                                aiConsultantResponse?.let { resp ->
                                    val regex = Regex("\\[HEX:(#[0-9A-Fa-f]{6})\\]")
                                    val match = regex.find(resp)
                                    val extractedHex = match?.groupValues?.getOrNull(1)
                                    val cleanText = resp.replace(regex, "").trim()

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF141320), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFF26243A), RoundedCornerShape(12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Suggested Aesthetic Blueprint:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = accentColor,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = cleanText,
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                lineHeight = 15.sp
                                            )

                                            extractedHex?.let { hex ->
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        viewModel.customAccentColorHex = hex
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(android.graphics.Color.parseColor(hex))
                                                    ),
                                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Palette,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Apply AI $hex Theme",
                                                        color = Color.Black,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // WORKSPACE UTILITIES & CREATIVE ASSETS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditMode) "Arrange Workspace Tools" else "Creative Suite Utilities",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFFA2A1B0),
                            letterSpacing = 1.sp
                        )

                        if (!isEditMode) {
                            Text(
                                text = "Drag or Hide in Edit Mode",
                                fontSize = 10.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isEditMode) {
                        // REORDERING LIST VIEW IN EDIT MODE
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            viewModel.dashboardCards.forEachIndexed { idx, card ->
                                EditCardRow(
                                    card = card,
                                    index = idx,
                                    size = viewModel.dashboardCards.size,
                                    accentColor = accentColor,
                                    onToggleEnabled = { viewModel.toggleCardEnabled(card.name) },
                                    onTogglePin = { viewModel.toggleCardPinned(card.name) },
                                    onMoveUp = { viewModel.moveCardUp(idx) },
                                    onMoveDown = { viewModel.moveCardDown(idx) }
                                )
                            }
                        }
                    } else {
                        // HIGHLY STYLED UTILITY GRID
                        // We filter out photo/video editor as they are already highlighted as primary cards
                        val filteredCards = viewModel.dashboardCards.filter { 
                            it.isEnabled && 
                            it.name.lowercase() != "photo editor" && 
                            it.name.lowercase() != "video editor" 
                        }

                        if (filteredCards.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No utility modules enabled.\nTap the Edit (sliders) icon in the top right.",
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF8E8D9C),
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 800.dp), // Adjust container height dynamically
                                userScrollEnabled = false // Inner scrolling disabled as parent is scrollable
                            ) {
                                items(filteredCards.size) { idx ->
                                    val card = filteredCards[idx]
                                    PremiumUtilityItem(
                                        card = card,
                                        accentColor = accentColor,
                                        onClick = {
                                            viewModel.currentScreen = card.name.lowercase().replace(" ", "_")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }

                // Docked professional AdMob Banner at the bottom of the Dashboard
                com.example.ads.AdBannerView(
                    adUnitId = com.example.ads.AdManager.BANNER_DASHBOARD,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@Composable
fun PremiumUtilityItem(card: DashboardCard, accentColor: Color, onClick: () -> Unit) {
    // Elegant, highly customized premium slate/metal colored utilities matching premium aesthetic
    val surfaceColor = Color(0xFF161522).copy(alpha = 0.85f)
    val strokeColor = Color(0xFF2E2C3F)
    
    // Custom labels specifically themed as production assistants
    val (themedTitle, themedSubtitle) = when (card.name.lowercase()) {
        "notes" -> Pair("Production Notes", "Rich scripts & checklists")
        "tasks" -> Pair("Project Boards", "To-Do steps & milestones")
        "calendar" -> Pair("Timeline Dates", "Release dates & schedule")
        "expense tracker" -> Pair("Budget Tracker", "License fees & costs")
        "calculator" -> Pair("Scale Calculator", "Aspect ratios & specs")
        "translator" -> Pair("Voice Translator", "Script transcript translation")
        "file manager" -> Pair("Assets Storage", "Secure audio/graphic files")
        else -> Pair(card.name, card.description)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .border(1.dp, strokeColor, RoundedCornerShape(20.dp))
            .testTag("tool_card_${card.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF232230)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconForCard(card.iconName),
                        contentDescription = card.name,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (card.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = accentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column {
                Text(
                    text = themedTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = themedSubtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF8E8D9C),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun EditCardRow(
    card: DashboardCard,
    index: Int,
    size: Int,
    accentColor: Color,
    onToggleEnabled: () -> Unit,
    onTogglePin: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onToggleEnabled) {
                    Icon(
                        imageVector = if (card.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle visibility",
                        tint = if (card.isEnabled) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = card.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (card.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = card.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTogglePin, enabled = card.isEnabled) {
                    Icon(
                        imageVector = if (card.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Toggle Pin",
                        tint = if (card.isPinned) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onMoveUp, enabled = index > 0) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Move Up",
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onMoveDown, enabled = index < size - 1) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Move Down",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun getIconForCard(name: String): ImageVector {
    return when (name) {
        "notes" -> Icons.Default.Description
        "tasks" -> Icons.Default.AssignmentTurnedIn
        "calendar" -> Icons.Default.DateRange
        "expenses" -> Icons.Default.Payments
        "calculator" -> Icons.Default.Calculate
        "translator" -> Icons.Default.Translate
        "file_manager" -> Icons.Default.Folder
        "games" -> Icons.Default.Gamepad
        else -> Icons.Default.Apps
    }
}
