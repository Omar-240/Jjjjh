package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ai.GeminiClient
import com.example.data.*
import com.example.receiver.TaskReminderManager
import com.example.shortcuts.AppShortcutsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*

data class DashboardCard(
    val name: String,
    val description: String,
    val iconName: String,
    val isEnabled: Boolean = true,
    val isPinned: Boolean = false,
    val order: Int = 0
)

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ToolboxRepository(database)

    // Onboarding & Navigation
    var isOnboardingCompleted by mutableStateOf(false)
        private set
    var currentScreen by mutableStateOf("onboarding") // onboarding, pin_entry, dashboard, notes, tasks, calendar, expenses, calculator, translator, file_manager, games, search, settings
    var previousScreen by mutableStateOf("dashboard")

    // Security
    var isPinLockEnabled by mutableStateOf(false)
    var appPinCode by mutableStateOf("1234")
    var isAppUnlocked by mutableStateOf(false)
    var pinEntryInput by mutableStateOf("")
    var pinEntryError by mutableStateOf<String?>(null)

    // Settings & Personalization
    var isDarkMode by mutableStateOf(true) // default to premium dark mode
    var accentColorName by mutableStateOf("Indigo") // Emerald, Indigo, Crimson, Gold, Slate
    var customAccentColorHex by mutableStateOf("")
    var appLanguage by mutableStateOf("English")

    // Dashboard Customization State
    var dashboardCards by mutableStateOf(listOf<DashboardCard>())
        private set

    // Database Flows
    val activeNotes = repository.activeNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val archivedNotes = repository.archivedNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trashNotes = repository.trashNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allEvents = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Edit State Holders
    var selectedNote by mutableStateOf<Note?>(null)
    var selectedTask by mutableStateOf<Task?>(null)
    var selectedEvent by mutableStateOf<CalendarEvent?>(null)
    var selectedExpense by mutableStateOf<Expense?>(null)

    // Note Editor Form state
    var noteTitleInput by mutableStateOf("")
    var noteContentInput by mutableStateOf("")
    var noteCategoryInput by mutableStateOf("Personal")
    var noteColorInput by mutableStateOf("#FF9800")
    var noteIsPinnedInput by mutableStateOf(false)

    // Task Editor Form state
    var taskTitleInput by mutableStateOf("")
    var taskDescInput by mutableStateOf("")
    var taskCategoryInput by mutableStateOf("General")
    var taskPriorityInput by mutableStateOf("Medium")
    var taskDueDateInput by mutableStateOf(System.currentTimeMillis())
    
    // 5-Minute Reminder State
    var isFiveMinReminderEnabled by mutableStateOf(false)

    // Expense Editor Form state
    var expenseTitleInput by mutableStateOf("")
    var expenseAmountInput by mutableStateOf("")
    var expenseIsExpenseInput by mutableStateOf(true)
    var expenseCategoryInput by mutableStateOf("Food")
    var expenseDateInput by mutableStateOf(System.currentTimeMillis())
    var expenseWalletInput by mutableStateOf("Cash")
    var expenseBudgetLimit by mutableStateOf(500.0)

    // Event Editor Form state
    var eventTitleInput by mutableStateOf("")
    var eventDescInput by mutableStateOf("")
    var eventDateInput by mutableStateOf(System.currentTimeMillis())
    var eventColorInput by mutableStateOf("#2196F3")
    var eventIsHolidayInput by mutableStateOf(false)

    // Global Search Query
    var globalQuery by mutableStateOf("")

    // Calculator State
    var calcDisplay by mutableStateOf("0")
    var calcHistory by mutableStateOf(listOf<String>())
    var calcExpression by mutableStateOf("")
    var unitConvType by mutableStateOf("Length") // Length, Weight, Temp
    var unitConvValue by mutableStateOf("1")
    var unitConvFrom by mutableStateOf("Meters")
    var unitConvTo by mutableStateOf("Feet")
    var unitConvResult by mutableStateOf("3.2808")

    // Translator State
    var translatorSourceText by mutableStateOf("")
    var translatorTargetText by mutableStateOf("")
    var translatorTargetLang by mutableStateOf("Spanish")
    var isTranslating by mutableStateOf(false)
    var translationHistory = mutableStateOf(listOf<Pair<String, String>>())

    // File Manager State
    var currentFileDirPath by mutableStateOf("")
    var currentFilesList by mutableStateOf(listOf<FileItem>())
    var fileSearchQuery by mutableStateOf("")
    var fileSelectedForCopy by mutableStateOf<FileItem?>(null)
    var isMoveOperation by mutableStateOf(false)

    // Photo Editor State
    var photoEditorBitmap by mutableStateOf<Bitmap?>(null)
    var photoEditBrightness by mutableStateOf(0f) // -100 to 100
    var photoEditContrast by mutableStateOf(1f) // 0.5 to 2.0
    var photoEditSaturation by mutableStateOf(1f) // 0.0 to 2.0
    var photoEditRotation by mutableStateOf(0f) // 0, 90, 180, 270
    var photoEditFilter by mutableStateOf("None") // None, Grayscale, Sepia, Invert, Vintage, Warm, Cool
    var photoEditAiPrompt by mutableStateOf("")
    var photoEditAiResult by mutableStateOf<String?>(null)
    var isAiEnhancing by mutableStateOf(false)

    // Photo & Video Studio Gallery & Slideshow States
    var photoEditorGallery by mutableStateOf<List<Bitmap>>(emptyList())
    var photoEditorGalleryNames by mutableStateOf<List<String>>(emptyList())
    var selectedGalleryIndex by mutableStateOf(0)
    var videoTimelinePhotos by mutableStateOf<List<Bitmap>>(emptyList())
    var currentVideoFrameIndex by mutableStateOf(0)
    var isVideoPlaying by mutableStateOf(false)
    private var videoPlayJob: kotlinx.coroutines.Job? = null

    // Video Editor State
    var videoTrimStart by mutableStateOf(0f)
    var videoTrimEnd by mutableStateOf(1f)
    var videoSpeed by mutableStateOf("1.0x")
    var videoFilter by mutableStateOf("None")
    var videoTransition by mutableStateOf("Fade")
    var isVideoExporting by mutableStateOf(false)
    var videoExportProgress by mutableStateOf(0f)

    init {
        initDashboardCards()
        initFileManagerHome()
        initDemoDatabase()
        initPhotoVideoStudio()
        
        // Initialize reminders and dynamic shortcuts
        isFiveMinReminderEnabled = TaskReminderManager.isReminderEnabled(application)
        AppShortcutsManager.createShortcuts(application)
    }

    // Onboarding
    fun completeOnboarding() {
        isOnboardingCompleted = true
        if (isPinLockEnabled) {
            currentScreen = "pin_entry"
        } else {
            isAppUnlocked = true
            currentScreen = "dashboard"
        }
    }

    // Security PIN lock
    fun handlePinInput(digit: String) {
        if (pinEntryInput.length < 4) {
            pinEntryInput += digit
            pinEntryError = null
        }
        if (pinEntryInput.length == 4) {
            if (pinEntryInput == appPinCode) {
                isAppUnlocked = true
                pinEntryInput = ""
                currentScreen = "dashboard"
            } else {
                pinEntryInput = ""
                pinEntryError = "Incorrect PIN. Try again."
            }
        }
    }

    fun deletePinDigit() {
        if (pinEntryInput.isNotEmpty()) {
            pinEntryInput = pinEntryInput.dropLast(1)
        }
    }

    // Settings Accent Colors
    fun getAccentColor(): androidx.compose.ui.graphics.Color {
        if (customAccentColorHex.isNotBlank()) {
            try {
                return androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(customAccentColorHex))
            } catch (e: Exception) {
                // fallback
            }
        }
        return when (accentColorName) {
            "Emerald" -> androidx.compose.ui.graphics.Color(0xFF93D39B)
            "Indigo" -> androidx.compose.ui.graphics.Color(0xFFD0BCFF)
            "Crimson" -> androidx.compose.ui.graphics.Color(0xFFE46962)
            "Gold" -> androidx.compose.ui.graphics.Color(0xFFEFB8C8)
            else -> androidx.compose.ui.graphics.Color(0xFFCAC4D0) // Slate
        }
    }

    // Initialize Dashboard cards
    private fun initDashboardCards() {
        dashboardCards = listOf(
            DashboardCard("Notes", "Rich notes & voice checklists", "notes", true, true, 0),
            DashboardCard("Tasks", "To-Do organizer & daily planner", "tasks", true, true, 1),
            DashboardCard("Calendar", "Events & holiday schedule", "calendar", true, true, 2),
            DashboardCard("Expense Tracker", "Budgets & financial spending", "expenses", true, true, 3),
            DashboardCard("Calculator", "Scientific math & converter", "calculator", true, false, 4),
            DashboardCard("Translator", "Real-time AI voice translation", "translator", true, false, 5),
            DashboardCard("File Manager", "Secure document storage", "file_manager", true, false, 6),
            DashboardCard("Games", "10 Offline Classic Retro Games", "games", true, false, 7)
        )
    }

    fun toggleCardPinned(name: String) {
        dashboardCards = dashboardCards.map {
            if (it.name == name) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun toggleCardEnabled(name: String) {
        dashboardCards = dashboardCards.map {
            if (it.name == name) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun moveCardUp(index: Int) {
        if (index > 0) {
            val list = dashboardCards.toMutableList()
            val temp = list[index]
            list[index] = list[index - 1]
            list[index - 1] = temp
            dashboardCards = list
        }
    }

    fun moveCardDown(index: Int) {
        if (index < dashboardCards.size - 1) {
            val list = dashboardCards.toMutableList()
            val temp = list[index]
            list[index] = list[index + 1]
            list[index + 1] = temp
            dashboardCards = list
        }
    }

    // DEMO DATA PRE-POPULATION
    private fun initDemoDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if Notes is empty, then populate some cool notes
            database.noteDao().getActiveNotes().first().let { list ->
                if (list.isEmpty()) {
                    repository.insertNote(Note(title = "Welcome to Smart Toolbox", content = "This premium workspace is built for seamless speed and absolute organization.\n\nEnjoy clean Material 3 cards, an intelligent AI translator powered by Gemini, local file manipulation, interactive expense charts, a scientific calculator, and professional editors.", category = "Work", isPinned = true, colorHex = "#6366F1"))
                    repository.insertNote(Note(title = "Shopping Checklist", content = "- 🛒 Fresh Organic Avocados\n- 🥛 Almond Milk (Unsweetened)\n- 🥖 French Sourdough Bread\n- ☕ Espresso Coffee Beans\n- 🍫 85% Dark Chocolate", category = "Personal", isPinned = false, colorHex = "#10B981"))
                    repository.insertNote(Note(title = "App Architecture Ideas", content = "1. Maintain an elegant single-activity view stack.\n2. Store entities in encrypted local SQLite database via Room.\n3. Implement on-the-fly local matrix bitmap manipulations for Photo Editor filters.", category = "Ideas", isPinned = false, colorHex = "#F59E0B"))
                }
            }

            // Check if Tasks is empty
            database.taskDao().getAllTasks().first().let { list ->
                if (list.isEmpty()) {
                    repository.insertTask(Task(title = "Explore Notes and checklists", description = "Try creating a pinned note with custom color tags.", priority = "High", isCompleted = true))
                    repository.insertTask(Task(title = "Review monthly spending budget", description = "Add a lunch expense in Expense Tracker.", priority = "Medium", isCompleted = false))
                    repository.insertTask(Task(title = "Sync calendar events", description = "Create a client meeting reminder on the calendar.", priority = "Low", isCompleted = false))
                }
            }

            // Check if Expenses is empty
            database.expenseDao().getAllExpenses().first().let { list ->
                if (list.isEmpty()) {
                    repository.insertExpense(Expense(title = "Co-working Studio Rent", amount = 150.0, isExpense = true, category = "Office", wallet = "Bank Card"))
                    repository.insertExpense(Expense(title = "Acoustic Guitar", amount = 299.0, isExpense = true, category = "Leisure", wallet = "PayPal"))
                    repository.insertExpense(Expense(title = "Consulting Gig Payout", amount = 850.0, isExpense = false, category = "Income", wallet = "Bank Card"))
                    repository.insertExpense(Expense(title = "Organic Grocery Shop", amount = 45.5, isExpense = true, category = "Food", wallet = "Cash"))
                }
            }

            // Check if Events is empty
            database.eventDao().getAllEvents().first().let { list ->
                if (list.isEmpty()) {
                    repository.insertEvent(CalendarEvent(title = "Launch Smart Toolbox", description = "Google Play Store submission deadline.", eventDate = System.currentTimeMillis() + 86400000))
                    repository.insertEvent(CalendarEvent(title = "Deep Work Session", description = "Focused algorithm coding & asset render.", eventDate = System.currentTimeMillis() - 86400000))
                }
            }
        }
    }

    // NOTES METHODS
    fun startNewNote() {
        selectedNote = null
        noteTitleInput = ""
        noteContentInput = ""
        noteCategoryInput = "Personal"
        noteColorInput = "#6366F1"
        noteIsPinnedInput = false
        currentScreen = "note_editor"
    }

    fun editNote(note: Note) {
        selectedNote = note
        noteTitleInput = note.title
        noteContentInput = note.content
        noteCategoryInput = note.category
        noteColorInput = note.colorHex
        noteIsPinnedInput = note.isPinned
        currentScreen = "note_editor"
    }

    fun saveNote() {
        if (noteTitleInput.isBlank()) return
        val currentNote = selectedNote
        viewModelScope.launch(Dispatchers.IO) {
            if (currentNote == null) {
                repository.insertNote(Note(
                    title = noteTitleInput,
                    content = noteContentInput,
                    category = noteCategoryInput,
                    colorHex = noteColorInput,
                    isPinned = noteIsPinnedInput
                ))
            } else {
                repository.updateNote(currentNote.copy(
                    title = noteTitleInput,
                    content = noteContentInput,
                    category = noteCategoryInput,
                    colorHex = noteColorInput,
                    isPinned = noteIsPinnedInput
                ))
            }
            withContext(Dispatchers.Main) {
                currentScreen = "notes"
            }
        }
    }

    fun trashNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note.copy(isTrash = true))
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note.copy(isArchived = true))
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note.copy(isTrash = false, isArchived = false))
        }
    }

    fun deleteNotePermanently(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotePermanently(id)
        }
    }

    fun emptyTrashNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.emptyTrashNotes()
        }
    }

    // TASKS METHODS
    fun saveTask() {
        if (taskTitleInput.isBlank()) return
        val currentTask = selectedTask
        viewModelScope.launch(Dispatchers.IO) {
            if (currentTask == null) {
                repository.insertTask(Task(
                    title = taskTitleInput,
                    description = taskDescInput,
                    category = taskCategoryInput,
                    priority = taskPriorityInput,
                    dueDate = taskDueDateInput,
                    isCompleted = false
                ))
            } else {
                repository.updateTask(currentTask.copy(
                    title = taskTitleInput,
                    description = taskDescInput,
                    category = taskCategoryInput,
                    priority = taskPriorityInput,
                    dueDate = taskDueDateInput
                ))
            }
            withContext(Dispatchers.Main) {
                selectedTask = null
                taskTitleInput = ""
                taskDescInput = ""
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(id)
        }
    }

    // 5-Minute Reminder Methods
    fun toggleFiveMinReminder(enabled: Boolean) {
        isFiveMinReminderEnabled = enabled
        if (enabled) {
            TaskReminderManager.startReminder(getApplication())
        } else {
            TaskReminderManager.stopReminder(getApplication())
        }
    }

    fun sendTestReminder() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val intent = android.content.Intent(context, com.example.receiver.TaskReminderReceiver::class.java)
            context.sendBroadcast(intent)
        }
    }

    // EXPENSES METHODS
    fun saveExpense() {
        val amount = expenseAmountInput.toDoubleOrNull() ?: return
        if (expenseTitleInput.isBlank()) return
        val currentExpense = selectedExpense
        viewModelScope.launch(Dispatchers.IO) {
            if (currentExpense == null) {
                repository.insertExpense(Expense(
                    title = expenseTitleInput,
                    amount = amount,
                    isExpense = expenseIsExpenseInput,
                    category = expenseCategoryInput,
                    date = expenseDateInput,
                    wallet = expenseWalletInput
                ))
            } else {
                repository.updateExpense(currentExpense.copy(
                    title = expenseTitleInput,
                    amount = amount,
                    isExpense = expenseIsExpenseInput,
                    category = expenseCategoryInput,
                    date = expenseDateInput,
                    wallet = expenseWalletInput
                ))
            }
            withContext(Dispatchers.Main) {
                selectedExpense = null
                expenseTitleInput = ""
                expenseAmountInput = ""
            }
        }
    }

    fun deleteExpense(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(id)
        }
    }

    // EVENTS METHODS
    fun saveEvent() {
        if (eventTitleInput.isBlank()) return
        val currentEvent = selectedEvent
        viewModelScope.launch(Dispatchers.IO) {
            if (currentEvent == null) {
                repository.insertEvent(CalendarEvent(
                    title = eventTitleInput,
                    description = eventDescInput,
                    eventDate = eventDateInput,
                    colorHex = eventColorInput,
                    isHoliday = eventIsHolidayInput
                ))
            } else {
                repository.updateEvent(currentEvent.copy(
                    title = eventTitleInput,
                    description = eventDescInput,
                    eventDate = eventDateInput,
                    colorHex = eventColorInput,
                    isHoliday = eventIsHolidayInput
                ))
            }
            withContext(Dispatchers.Main) {
                selectedEvent = null
                eventTitleInput = ""
                eventDescInput = ""
            }
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(id)
        }
    }

    // CALCULATOR METHODS
    fun handleCalcAction(action: String) {
        when (action) {
            "C" -> {
                calcDisplay = "0"
                calcExpression = ""
            }
            "DEL" -> {
                if (calcDisplay.length > 1) {
                    calcDisplay = calcDisplay.dropLast(1)
                } else {
                    calcDisplay = "0"
                }
            }
            "=" -> {
                evaluateCalcExpression()
            }
            "+", "-", "*", "/" -> {
                if (calcExpression.isNotEmpty() && (calcExpression.endsWith("+") || calcExpression.endsWith("-") || calcExpression.endsWith("*") || calcExpression.endsWith("/"))) {
                    calcExpression = calcExpression.dropLast(1) + action
                } else {
                    calcExpression = calcDisplay + action
                }
                calcDisplay = "0"
            }
            "sin", "cos", "tan", "sqrt", "log" -> {
                val value = calcDisplay.toDoubleOrNull() ?: 0.0
                val result = when (action) {
                    "sin" -> Math.sin(Math.toRadians(value))
                    "cos" -> Math.cos(Math.toRadians(value))
                    "tan" -> Math.tan(Math.toRadians(value))
                    "sqrt" -> Math.sqrt(value)
                    "log" -> Math.log10(value)
                    else -> 0.0
                }
                val formatted = String.format(Locale.US, "%.5f", result).trimEnd('0').trimEnd('.')
                calcHistory = listOf("$action($value) = $formatted") + calcHistory.take(19)
                calcDisplay = formatted
            }
            else -> {
                if (calcDisplay == "0") {
                    calcDisplay = action
                } else {
                    calcDisplay += action
                }
            }
        }
    }

    private fun evaluateCalcExpression() {
        if (calcExpression.isEmpty()) return
        val num1 = calcExpression.dropLast(1).toDoubleOrNull() ?: return
        val op = calcExpression.last()
        val num2 = calcDisplay.toDoubleOrNull() ?: return
        val result = when (op) {
            '+' -> num1 + num2
            '-' -> num1 - num2
            '*' -> num1 * num2
            '/' -> if (num2 != 0.0) num1 / num2 else Double.NaN
            else -> 0.0
        }
        val formatted = if (result.isNaN()) "Error" else String.format(Locale.US, "%.5f", result).trimEnd('0').trimEnd('.')
        calcHistory = listOf("$num1 $op $num2 = $formatted") + calcHistory.take(19)
        calcDisplay = formatted
        calcExpression = ""
    }

    fun performUnitConversion() {
        val input = unitConvValue.toDoubleOrNull() ?: 1.0
        val result = when (unitConvType) {
            "Length" -> {
                when (unitConvFrom to unitConvTo) {
                    "Meters" to "Feet" -> input * 3.28084
                    "Feet" to "Meters" -> input / 3.28084
                    "Kilometers" to "Miles" -> input * 0.621371
                    "Miles" to "Kilometers" -> input / 0.621371
                    else -> input
                }
            }
            "Weight" -> {
                when (unitConvFrom to unitConvTo) {
                    "Kilograms" to "Pounds" -> input * 2.20462
                    "Pounds" to "Kilograms" -> input / 2.20462
                    "Grams" to "Ounces" -> input * 0.035274
                    "Ounces" to "Grams" -> input / 0.035274
                    else -> input
                }
            }
            "Temp" -> {
                when (unitConvFrom to unitConvTo) {
                    "Celsius" to "Fahrenheit" -> (input * 9/5) + 32
                    "Fahrenheit" to "Celsius" -> (input - 32) * 5/9
                    else -> input
                }
            }
            else -> input
        }
        unitConvResult = String.format(Locale.US, "%.4f", result).trimEnd('0').trimEnd('.')
    }

    // TRANSLATOR
    fun translate() {
        if (translatorSourceText.isBlank()) return
        isTranslating = true
        viewModelScope.launch {
            val result = GeminiClient.translateText(translatorSourceText, translatorTargetLang)
            withContext(Dispatchers.Main) {
                translatorTargetText = result
                isTranslating = false
                if (!result.startsWith("Error:")) {
                    translationHistory.value = listOf(Pair(translatorSourceText, result)) + translationHistory.value.take(9)
                }
            }
        }
    }

    // FILE MANAGER
    private fun initFileManagerHome() {
        val appFilesDir = getApplication<Application>().filesDir
        currentFileDirPath = appFilesDir.absolutePath

        // Create some pre-populated folders if they don't exist
        val docs = File(appFilesDir, "Documents")
        if (!docs.exists()) docs.mkdirs()
        val photos = File(appFilesDir, "Pictures")
        if (!photos.exists()) photos.mkdirs()

        val readme = File(appFilesDir, "toolbox_readme.txt")
        if (!readme.exists()) {
            readme.writeText("Smart Toolbox Security Workspace File System.\n\nCreated: 2026-07-10\nAuthor: o0778940548@gmail.com\nAll local operations are offline-first, private, and secured on device.")
        }

        refreshFilesList()
    }

    fun refreshFilesList() {
        val dir = File(currentFileDirPath)
        if (dir.exists() && dir.isDirectory) {
            val items = dir.listFiles()?.map {
                FileItem(
                    name = it.name,
                    path = it.absolutePath,
                    isDirectory = it.isDirectory,
                    sizeBytes = if (it.isDirectory) 0 else it.length(),
                    lastModified = it.lastModified()
                )
            }?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()

            currentFilesList = if (fileSearchQuery.isNotBlank()) {
                items.filter { it.name.contains(fileSearchQuery, ignoreCase = true) }
            } else {
                items
            }
        }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        val newDir = File(currentFileDirPath, name)
        if (!newDir.exists()) {
            newDir.mkdirs()
            refreshFilesList()
        }
    }

    fun createFile(name: String, content: String) {
        if (name.isBlank()) return
        val newFile = File(currentFileDirPath, name)
        newFile.writeText(content)
        refreshFilesList()
    }

    fun deleteFile(item: FileItem) {
        val file = File(item.path)
        if (file.exists()) {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
            refreshFilesList()
        }
    }

    fun renameFile(item: FileItem, newName: String) {
        if (newName.isBlank()) return
        val src = File(item.path)
        val dst = File(src.parent, newName)
        if (src.exists() && !dst.exists()) {
            src.renameTo(dst)
            refreshFilesList()
        }
    }

    fun copyFileSelected(item: FileItem, isMove: Boolean) {
        fileSelectedForCopy = item
        isMoveOperation = isMove
    }

    fun pasteFile() {
        val srcItem = fileSelectedForCopy ?: return
        val srcFile = File(srcItem.path)
        val dstFile = File(currentFileDirPath, srcFile.name)

        if (srcFile.exists() && srcFile.absolutePath != dstFile.absolutePath) {
            viewModelScope.launch(Dispatchers.IO) {
                if (srcFile.isDirectory) {
                    srcFile.copyRecursively(dstFile, overwrite = true)
                    if (isMoveOperation) srcFile.deleteRecursively()
                } else {
                    srcFile.copyTo(dstFile, overwrite = true)
                    if (isMoveOperation) srcFile.delete()
                }
                withContext(Dispatchers.Main) {
                    fileSelectedForCopy = null
                    isMoveOperation = false
                    refreshFilesList()
                }
            }
        }
    }

    // PHOTO EDITOR METHODS
    fun initPhotoVideoStudio() {
        val sunset = createSunsetBitmap()
        val neon = createNeonCyberpunkBitmap()
        val mountain = createEmeraldMountainBitmap()
        val aurora = createCosmicAuroraBitmap()
        val appArt = createAppIconArtBitmap()

        photoEditorGallery = listOf(sunset, neon, mountain, aurora, appArt)
        photoEditorGalleryNames = listOf("Sunset Glow", "Neon Dream", "Emerald Forest", "Cosmic Aurora", "Icon Art")
        selectedGalleryIndex = 0
        photoEditorBitmap = sunset

        // Pre-populate video timeline
        videoTimelinePhotos = listOf(sunset, mountain, aurora)
    }

    fun selectGalleryPhoto(index: Int) {
        if (index in photoEditorGallery.indices) {
            selectedGalleryIndex = index
            photoEditorBitmap = photoEditorGallery[index]
            resetPhotoEdits()
        }
    }

    fun addPhotoToGallery(bitmap: Bitmap, name: String) {
        photoEditorGallery = photoEditorGallery + bitmap
        photoEditorGalleryNames = photoEditorGalleryNames + name
        selectedGalleryIndex = photoEditorGallery.lastIndex
        photoEditorBitmap = bitmap
        resetPhotoEdits()
    }

    fun simulateCameraCapture() {
        viewModelScope.launch(Dispatchers.IO) {
            val b = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(b)
            val paint = android.graphics.Paint()
            
            val sec = (System.currentTimeMillis() / 1000) % 60
            val color1 = 0xFF000000.toInt() or (0xFF5588 * sec.toInt())
            val color2 = 0xFF221144.toInt() or (0x880033 * (60 - sec.toInt()))
            
            val gradient = android.graphics.LinearGradient(
                0f, 0f, 800f, 800f,
                color1, color2,
                android.graphics.Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, 800f, 800f, paint)
            paint.shader = null
            
            paint.color = 0xFFFFFCF4.toInt()
            canvas.drawCircle(400f, 400f, 80f, paint)
            
            paint.color = 0xAA000000.toInt()
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 6f
            val path = android.graphics.Path()
            path.moveTo(150f, 200f)
            path.quadTo(200f, 150f, 250f, 200f)
            path.quadTo(300f, 150f, 350f, 200f)
            canvas.drawPath(path, paint)
            
            withContext(Dispatchers.Main) {
                val stamp = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                addPhotoToGallery(b, "Camera Capture $stamp")
            }
        }
    }

    fun addPhotoToVideoTimeline(bitmap: Bitmap) {
        videoTimelinePhotos = videoTimelinePhotos + bitmap
    }

    fun removePhotoFromVideoTimeline(index: Int) {
        if (index in videoTimelinePhotos.indices) {
            val list = videoTimelinePhotos.toMutableList()
            list.removeAt(index)
            videoTimelinePhotos = list
            if (currentVideoFrameIndex >= videoTimelinePhotos.size) {
                currentVideoFrameIndex = maxOf(0, videoTimelinePhotos.size - 1)
            }
        }
    }

    fun startVideoPlayback() {
        if (videoTimelinePhotos.isEmpty()) return
        isVideoPlaying = true
        videoPlayJob?.cancel()
        videoPlayJob = viewModelScope.launch {
            while (isVideoPlaying) {
                for (i in videoTimelinePhotos.indices) {
                    if (!isVideoPlaying) break
                    currentVideoFrameIndex = i
                    val speedMs = when (videoSpeed) {
                        "0.5x" -> 2000L
                        "1.0x" -> 1000L
                        "1.5x" -> 700L
                        "2.0x" -> 500L
                        else -> 1000L
                    }
                    kotlinx.coroutines.delay(speedMs)
                }
                if (videoTimelinePhotos.size <= 1) break
            }
        }
    }

    fun stopVideoPlayback() {
        isVideoPlaying = false
        videoPlayJob?.cancel()
        videoPlayJob = null
    }

    fun createSunsetBitmap(): Bitmap {
        val b = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(b)
        val paint = android.graphics.Paint()
        val gradient = android.graphics.RadialGradient(
            400f, 400f, 600f,
            intArrayOf(0xFFFF7E5F.toInt(), 0xFFFEB47B.toInt(), 0xFF86A8E7.toInt()),
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, 800f, 800f, paint)
        paint.shader = null
        paint.color = 0xFFFFFCF4.toInt()
        canvas.drawCircle(400f, 480f, 130f, paint)
        paint.color = 0x4DFFFFFF
        canvas.drawRoundRect(250f, 450f, 450f, 490f, 20f, 20f, paint)
        canvas.drawRoundRect(350f, 410f, 550f, 450f, 20f, 20f, paint)
        return b
    }

    fun createNeonCyberpunkBitmap(): Bitmap {
        val b = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(b)
        val paint = android.graphics.Paint()
        val gradient = android.graphics.LinearGradient(
            0f, 0f, 800f, 800f,
            0xFF0D0C1D.toInt(), 0xFF1B1A36.toInt(),
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, 800f, 800f, paint)
        paint.shader = null
        paint.color = 0x2200FFFF
        paint.strokeWidth = 2f
        for (i in 0..8) {
            val pos = i * 100f
            canvas.drawLine(pos, 0f, pos, 800f, paint)
            canvas.drawLine(0f, pos, 800f, pos, paint)
        }
        paint.style = android.graphics.Paint.Style.STROKE
        paint.strokeWidth = 8f
        paint.color = 0xFFFF007F.toInt()
        canvas.drawCircle(400f, 400f, 200f, paint)
        paint.color = 0xFF00FFFF.toInt()
        canvas.drawCircle(400f, 400f, 150f, paint)
        paint.style = android.graphics.Paint.Style.FILL
        return b
    }

    fun createEmeraldMountainBitmap(): Bitmap {
        val b = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(b)
        val paint = android.graphics.Paint()
        val gradient = android.graphics.LinearGradient(
            0f, 0f, 0f, 800f,
            0xFF0F2027.toInt(), 0xFF203A43.toInt(),
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, 800f, 800f, paint)
        paint.shader = null
        paint.color = 0xAAFFFFFF.toInt()
        val random = java.util.Random(42)
        for (i in 0..60) {
            val x = random.nextFloat() * 800f
            val y = random.nextFloat() * 500f
            canvas.drawCircle(x, y, 2f + random.nextFloat() * 3f, paint)
        }
        val path = android.graphics.Path()
        paint.color = 0xFF0D5C3A.toInt()
        path.moveTo(100f, 800f)
        path.lineTo(350f, 400f)
        path.lineTo(600f, 800f)
        path.close()
        canvas.drawPath(path, paint)
        val path2 = android.graphics.Path()
        paint.color = 0xFF147E53.toInt()
        path2.moveTo(300f, 800f)
        path2.lineTo(550f, 450f)
        path2.lineTo(800f, 800f)
        path2.close()
        canvas.drawPath(path2, paint)
        return b
    }

    fun createCosmicAuroraBitmap(): Bitmap {
        val b = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(b)
        val paint = android.graphics.Paint()
        canvas.drawColor(0xFF03001E.toInt())
        paint.style = android.graphics.Paint.Style.FILL
        paint.shader = android.graphics.RadialGradient(
            200f, 300f, 400f,
            intArrayOf(0xBB00FF87.toInt(), 0x0000FF87.toInt()),
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawCircle(200f, 300f, 400f, paint)
        paint.shader = android.graphics.RadialGradient(
            600f, 500f, 500f,
            intArrayOf(0xBB60EFFF.toInt(), 0x0060EFFF.toInt()),
            null,
            android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawCircle(600f, 500f, 500f, paint)
        paint.shader = null
        return b
    }

    fun createAppIconArtBitmap(): Bitmap {
        val b = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(b)
        val paint = android.graphics.Paint()
        val gradient = android.graphics.LinearGradient(
            0f, 0f, 800f, 800f,
            0xFFF2994A.toInt(), 0xFFF2C94C.toInt(),
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, 800f, 800f, paint)
        paint.shader = null
        paint.color = 0xFF1C1B1F.toInt()
        canvas.drawRoundRect(150f, 250f, 650f, 650f, 40f, 40f, paint)
        paint.color = 0xFFD0BCFF.toInt()
        canvas.drawCircle(400f, 450f, 100f, paint)
        return b
    }

    fun rotatePhoto() {
        photoEditRotation = (photoEditRotation + 90f) % 360f
    }

    fun applyPhotoAiEnhancement() {
        if (photoEditAiPrompt.isBlank()) return
        isAiEnhancing = true
        viewModelScope.launch {
            val desc = GeminiClient.aiEnhancePhotoPrompt(photoEditAiPrompt)
            withContext(Dispatchers.Main) {
                photoEditAiResult = desc
                isAiEnhancing = false
                // Simulate change of filter parameters to achieve this
                photoEditBrightness = 15f
                photoEditContrast = 1.25f
                photoEditSaturation = 1.3f
                photoEditFilter = "Warm"
            }
        }
    }

    fun resetPhotoEdits() {
        photoEditBrightness = 0f
        photoEditContrast = 1f
        photoEditSaturation = 1f
        photoEditRotation = 0f
        photoEditFilter = "None"
        photoEditAiResult = null
    }

    // VIDEO EDITOR METHODS
    fun triggerVideoExport() {
        isVideoExporting = true
        videoExportProgress = 0f
        viewModelScope.launch {
            for (i in 1..10) {
                kotlinx.coroutines.delay(300)
                videoExportProgress = i * 0.1f
            }
            isVideoExporting = false
            videoExportProgress = 1.0f
        }
    }

    suspend fun generateStudioInspiration(prompt: String): String {
        return GeminiClient.generateStudioInspiration(prompt)
    }
}
