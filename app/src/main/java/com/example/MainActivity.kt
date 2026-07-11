package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ads.AdManager
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AdMob and preload ads
        AdManager.initialize(this)
        
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            
            // Handle App Shortcuts and Notifications intent navigation
            androidx.compose.runtime.LaunchedEffect(intent) {
                intent?.getStringExtra("navigate_to")?.let { targetScreen ->
                    viewModel.currentScreen = targetScreen
                    viewModel.isAppUnlocked = true
                    viewModel.completeOnboarding()
                }
            }

            MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = viewModel.currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            "onboarding" -> OnboardingScreen(viewModel)
                            "pin_entry" -> PinEntryScreen(viewModel)
                            "dashboard" -> DashboardScreen(viewModel)
                            "notes" -> NotesScreen(viewModel)
                            "note_editor" -> NoteEditorScreen(viewModel)
                            "tasks" -> TasksScreen(viewModel)
                            "calendar" -> CalendarScreen(viewModel)
                            "expenses" -> ExpensesScreen(viewModel)
                            "expense_tracker" -> ExpensesScreen(viewModel)
                            "calculator" -> CalculatorScreen(viewModel)
                            "translator" -> TranslatorScreen(viewModel)
                            "file_manager" -> FileManagerScreen(viewModel)
                            "games" -> GamesScreen(viewModel)
                            "search" -> GlobalSearchScreen(viewModel)
                            "settings" -> SettingsScreen(viewModel)
                            else -> DashboardScreen(viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
