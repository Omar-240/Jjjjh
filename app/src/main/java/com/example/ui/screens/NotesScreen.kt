package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Note
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: MainViewModel) {
    val activeNotes by viewModel.activeNotes.collectAsStateWithLifecycle()
    val archivedNotes by viewModel.archivedNotes.collectAsStateWithLifecycle()
    val trashNotes by viewModel.trashNotes.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("active") } // active, archived, trash
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val accentColor = viewModel.getAccentColor()

    val currentNotes = when (activeTab) {
        "archived" -> archivedNotes
        "trash" -> trashNotes
        else -> activeNotes
    }

    val categories = listOf("All", "Personal", "Work", "Ideas", "Study")

    val filteredNotes = currentNotes.filter { note ->
        val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || note.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workspace Notes", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = "dashboard" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { activeTab = "active" }) {
                        Icon(
                            imageVector = Icons.Default.StickyNote2,
                            contentDescription = "Active Notes",
                            tint = if (activeTab == "active") accentColor else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { activeTab = "archived" }) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Archived Notes",
                            tint = if (activeTab == "archived") accentColor else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { activeTab = "trash" }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Trash Bin",
                            tint = if (activeTab == "trash") accentColor else MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (activeTab == "active") {
                ExtendedFloatingActionButton(
                    text = { Text("New Note", color = Color.White) },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White) },
                    onClick = { viewModel.startNewNote() },
                    containerColor = accentColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_note_fab")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
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
                    .padding(bottom = 66.dp)
            ) {
                // SEARCH & CATEGORY FILTER
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .testTag("notes_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                ),
                singleLine = true
            )

            // CATEGORIES ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            selectedBorderColor = accentColor
                        )
                    )
                }
            }

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (activeTab) {
                                "archived" -> Icons.Default.Archive
                                "trash" -> Icons.Default.DeleteForever
                                else -> Icons.Default.StickyNote2
                            },
                            contentDescription = "Empty notes",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (activeTab) {
                                "archived" -> "No archived notes."
                                "trash" -> "Trash is empty."
                                else -> "Write your first note to capture ideas."
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                if (activeTab == "trash") {
                    Button(
                        onClick = { viewModel.emptyTrashNotes() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("Empty Trash Bin", color = Color.White)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredNotes.size) { idx ->
                        val note = filteredNotes[idx]
                        NoteGridCard(
                            note = note,
                            activeTab = activeTab,
                            onEdit = { viewModel.editNote(note) },
                            onTrash = { viewModel.trashNote(note) },
                            onArchive = { viewModel.archiveNote(note) },
                            onRestore = { viewModel.restoreNote(note) },
                            onDeletePerm = { viewModel.deleteNotePermanently(note.id) }
                        )
                    }
                }
            }
            
            } // Close Column

            // Docked professional AdMob Banner at the bottom of Notes Screen
            com.example.ads.AdBannerView(
                adUnitId = com.example.ads.AdManager.BANNER_GENERIC,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
fun NoteGridCard(
    note: Note,
    activeTab: String,
    onEdit: () -> Unit,
    onTrash: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDeletePerm: () -> Unit
) {
    val cardColor = remember(note.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(note.colorHex))
        } catch (e: Exception) {
            Color(0xFF6366F1)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable { if (activeTab == "active") onEdit() }
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, cardColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(cardColor.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = note.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = cardColor
                        )
                    }

                    if (note.isPinned && activeTab == "active") {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = cardColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = note.content,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            // BOTTOM CONTROL ROW FOR ARCHIVE/TRASH/DELETE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeTab == "active") {
                    IconButton(onClick = onArchive, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Archive,
                            contentDescription = "Archive",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onTrash, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Move to trash",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else if (activeTab == "archived" || activeTab == "trash") {
                    IconButton(onClick = onRestore, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = "Restore note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (activeTab == "trash") {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDeletePerm, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = "Delete permanently",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// NOTE WRITING EDITOR SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val accentColor = viewModel.getAccentColor()
    val noteColors = listOf("#6366F1", "#10B981", "#EF4444", "#F59E0B", "#14B8A6", "#EC4899")
    val categories = listOf("Personal", "Work", "Ideas", "Study", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.selectedNote == null) "Create Note" else "Edit Note", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = "notes" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.noteIsPinnedInput = !viewModel.noteIsPinnedInput }
                    ) {
                        Icon(
                            imageVector = if (viewModel.noteIsPinnedInput) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (viewModel.noteIsPinnedInput) accentColor else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = {
                            com.example.ads.AdManager.showInterstitialSave(context) {
                                viewModel.saveNote()
                            }
                        },
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save note", tint = accentColor)
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
            // NOTE TITLE
            OutlinedTextField(
                value = viewModel.noteTitleInput,
                onValueChange = { viewModel.noteTitleInput = it },
                label = { Text("Title") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_title_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor)
            )

            // NOTE CATEGORY SELECTOR
            Text("Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = viewModel.noteCategoryInput == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.noteCategoryInput = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor
                        )
                    )
                }
            }

            // COLOR SELECTOR
            Text("Label Color", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                noteColors.forEach { colorHex ->
                    val c = Color(android.graphics.Color.parseColor(colorHex))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (viewModel.noteColorInput == colorHex) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = CircleShape
                            )
                            .clickable { viewModel.noteColorInput = colorHex }
                    )
                }
            }

            // NOTE CONTENT BODY
            OutlinedTextField(
                value = viewModel.noteContentInput,
                onValueChange = { viewModel.noteContentInput = it },
                label = { Text("Content") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp)
                    .testTag("note_content_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                maxLines = 100
            )
        }
    }
}
