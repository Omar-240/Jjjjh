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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.FileItem
import com.example.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()
    val context = LocalContext.current

    var showCreateDirDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var selectedFileItemForRename by remember { mutableStateOf<FileItem?>(null) }

    var dirNameInput by remember { mutableStateOf("") }
    var fileNameInput by remember { mutableStateOf("") }
    var fileContentInput by remember { mutableStateOf("") }
    var renameInput by remember { mutableStateOf("") }

    // Re-load list when folder path or query changes
    LaunchedEffect(viewModel.currentFileDirPath, viewModel.fileSearchQuery) {
        viewModel.refreshFilesList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local File Explorer", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        val current = File(viewModel.currentFileDirPath)
                        val parent = current.parentFile
                        val appFilesDir = context.filesDir
                        if (parent != null && current.absolutePath != appFilesDir.absolutePath) {
                            viewModel.currentFileDirPath = parent.absolutePath
                        } else {
                            viewModel.currentScreen = "dashboard"
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDirDialog = true }) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = accentColor)
                    }
                    IconButton(onClick = { showCreateFileDialog = true }) {
                        Icon(Icons.Default.NoteAdd, contentDescription = "New File", tint = accentColor)
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
            // CURRENT PATH BREADCRUMB
            val displayPath = viewModel.currentFileDirPath.substringAfter("files")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Home, contentDescription = "Root Workspace", tint = accentColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (displayPath.isBlank()) "/Workspace" else "/Workspace$displayPath",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            // SEARCH BAR
            OutlinedTextField(
                value = viewModel.fileSearchQuery,
                onValueChange = { viewModel.fileSearchQuery = it },
                placeholder = { Text("Search local files...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("file_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                ),
                singleLine = true
            )

            // CLIPBOARD ACTION INDICATOR
            if (viewModel.fileSelectedForCopy != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${if (viewModel.isMoveOperation) "Move" else "Copy"}: ${viewModel.fileSelectedForCopy?.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.pasteFile() }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = accentColor)
                            }
                            IconButton(onClick = {
                                viewModel.fileSelectedForCopy = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel paste", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // FILE DIRECTORIES LIST
            if (viewModel.currentFilesList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "This folder is empty.\nTap folder/file icons in top right to create local content.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(viewModel.currentFilesList.size) { idx ->
                        val item = viewModel.currentFilesList[idx]
                        FileRowItem(
                            item = item,
                            accentColor = accentColor,
                            onClick = {
                                if (item.isDirectory) {
                                    viewModel.currentFileDirPath = item.path
                                } else {
                                    // Simulated simple text file viewing
                                    val content = try { File(item.path).readText() } catch (e: Exception) { "Binary file" }
                                    Toast.makeText(context, "${item.name}:\n$content", Toast.LENGTH_LONG).show()
                                }
                            },
                            onCopy = { viewModel.copyFileSelected(item, false) },
                            onMove = { viewModel.copyFileSelected(item, true) },
                            onRename = {
                                selectedFileItemForRename = item
                                renameInput = item.name
                                showRenameDialog = true
                            },
                            onDelete = { viewModel.deleteFile(item) }
                        )
                    }
                }
            }
        }
    }

    // CREATE FOLDER DIALOG
    if (showCreateDirDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDirDialog = false },
            title = { Text("New Folder", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = dirNameInput,
                    onValueChange = { dirNameInput = it },
                    label = { Text("Folder Name") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createFolder(dirNameInput)
                        dirNameInput = ""
                        showCreateDirDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDirDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // CREATE FILE DIALOG
    if (showCreateFileDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFileDialog = false },
            title = { Text("New Text File", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { fileNameInput = it },
                        label = { Text("Filename (e.g. log.txt)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = fileContentInput,
                        onValueChange = { fileContentInput = it },
                        label = { Text("Content") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(100.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createFile(fileNameInput, fileContentInput)
                        fileNameInput = ""
                        fileContentInput = ""
                        showCreateFileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Save File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // RENAME DIALOG
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename File", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New Name") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedFileItemForRename?.let {
                            viewModel.renameFile(it, renameInput)
                        }
                        showRenameDialog = false
                        selectedFileItemForRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FileRowItem(
    item: FileItem,
    accentColor: Color,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val formattedDate = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(item.lastModified))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("file_row_${item.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (item.isDirectory) accentColor.copy(alpha = 0.15f)
                            else Color.Gray.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                        contentDescription = "File Type Icon",
                        tint = if (item.isDirectory) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (item.isDirectory) "Folder • $formattedDate" else "${item.sizeBytes / 1024} KB • $formattedDate",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Actions Menu")
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = "Copy") },
                        onClick = {
                            onCopy()
                            expandedMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Move") },
                        leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = "Move") },
                        onClick = {
                            onMove()
                            expandedMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Rename") },
                        onClick = {
                            onRename()
                            expandedMenu = false
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            onDelete()
                            expandedMenu = false
                        }
                    )
                }
            }
        }
    }
}
