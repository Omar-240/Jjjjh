package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(viewModel: MainViewModel) {
    val accentColor = viewModel.getAccentColor()
    val context = LocalContext.current

    val languages = listOf("Spanish", "French", "German", "Japanese", "Chinese", "Arabic", "Italian", "Hindi")

    // TTS initialization
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        onDispose {
            tts?.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Translator", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // SOURCE TEXT INPUT CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "Auto-Detected (English)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = viewModel.translatorSourceText,
                        onValueChange = { viewModel.translatorSourceText = it },
                        placeholder = { Text("Enter text to translate...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("translator_source_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // TARGET LANGUAGE SELECTOR
            Text("Target Language", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { lang ->
                    val isSelected = viewModel.translatorTargetLang == lang
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.translatorTargetLang = lang },
                        label = { Text(lang) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor
                        )
                    )
                }
            }

            // TRANSLATE BUTTON
            Button(
                onClick = { viewModel.translate() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("translate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                enabled = !viewModel.isTranslating && viewModel.translatorSourceText.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isTranslating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Translate, contentDescription = "Translate", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Translate Text", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // TARGET TRANSLATED TEXT DISPLAY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.06f)),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = viewModel.translatorTargetLang,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = viewModel.translatorTargetText.ifBlank { "Translation will appear here..." },
                        fontSize = 15.sp,
                        color = if (viewModel.translatorTargetText.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("translator_result_text")
                    )

                    if (viewModel.translatorTargetText.isNotBlank() && !viewModel.translatorTargetText.startsWith("Error:")) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy button
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Translation", viewModel.translatorTargetText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Pronounce text to speech button
                            IconButton(onClick = {
                                tts?.speak(viewModel.translatorTargetText, TextToSpeech.QUEUE_FLUSH, null, null)
                            }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Pronounce", tint = accentColor)
                            }
                        }
                    }
                }
            }

            // TRANSLATION HISTORY
            val history by viewModel.translationHistory
            if (history.isNotEmpty()) {
                Text(
                    "Translation History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 10.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    history.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(item.first, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.second, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = accentColor)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
