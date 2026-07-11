package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalendarEvent
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: MainViewModel) {
    val events by viewModel.allEvents.collectAsStateWithLifecycle()
    val accentColor = viewModel.getAccentColor()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedDateCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    // Calendar logic
    val currentMonthCalendar = remember { Calendar.getInstance() }
    var year by remember { mutableStateOf(currentMonthCalendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(currentMonthCalendar.get(Calendar.MONTH)) }

    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    val calendarInstance = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val daysInMonth = calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = calendarInstance.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 1 = Monday, etc.

    val selectedDateFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US).format(selectedDateCalendar.time)

    // Events matching selected date
    val selectedDayEvents = events.filter { event ->
        val eventCal = Calendar.getInstance().apply { timeInMillis = event.eventDate }
        eventCal.get(Calendar.YEAR) == selectedDateCalendar.get(Calendar.YEAR) &&
                eventCal.get(Calendar.MONTH) == selectedDateCalendar.get(Calendar.MONTH) &&
                eventCal.get(Calendar.DAY_OF_MONTH) == selectedDateCalendar.get(Calendar.DAY_OF_MONTH)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar Events", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.currentScreen = "dashboard" }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("New Event", color = Color.White) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White) },
                onClick = {
                    viewModel.selectedEvent = null
                    viewModel.eventTitleInput = ""
                    viewModel.eventDescInput = ""
                    viewModel.eventDateInput = selectedDateCalendar.timeInMillis
                    viewModel.eventColorInput = "#2196F3"
                    viewModel.eventIsHolidayInput = false
                    showAddEventDialog = true
                },
                containerColor = accentColor,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_event_fab")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // MONTH NAVIGATION BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (month == 0) {
                        month = 11
                        year--
                    } else {
                        month--
                    }
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = "${monthNames[month]} $year",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = {
                    if (month == 11) {
                        month = 0
                        year++
                    } else {
                        month++
                    }
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // DAY NAMES HEADER
            Row(modifier = Modifier.fillMaxWidth()) {
                val dayNames = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // MONTHLY GRID CALENDAR
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0..6) {
                            val cellIndex = r * 7 + c
                            if (cellIndex < firstDayOfWeek || cellIndex >= totalCells) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val dayNumber = cellIndex - firstDayOfWeek + 1
                                val isSelected = selectedDateCalendar.get(Calendar.DAY_OF_MONTH) == dayNumber &&
                                        selectedDateCalendar.get(Calendar.MONTH) == month &&
                                        selectedDateCalendar.get(Calendar.YEAR) == year

                                val hasEvents = events.any { e ->
                                    val eCal = Calendar.getInstance().apply { timeInMillis = e.eventDate }
                                    eCal.get(Calendar.DAY_OF_MONTH) == dayNumber &&
                                            eCal.get(Calendar.MONTH) == month &&
                                            eCal.get(Calendar.YEAR) == year
                                }

                                val isToday = Calendar.getInstance().let {
                                    it.get(Calendar.DAY_OF_MONTH) == dayNumber &&
                                            it.get(Calendar.MONTH) == month &&
                                            it.get(Calendar.YEAR) == year
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> accentColor
                                                isToday -> accentColor.copy(alpha = 0.15f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            selectedDateCalendar = Calendar
                                                .getInstance()
                                                .apply {
                                                    set(Calendar.YEAR, year)
                                                    set(Calendar.MONTH, month)
                                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                                }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNumber.toString(),
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp,
                                            color = when {
                                                isSelected -> Color.White
                                                isToday -> accentColor
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        if (hasEvents && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(accentColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AGENDA FOR SELECTED DATE
            Text(
                text = selectedDateFormatted,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = accentColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (selectedDayEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No events scheduled for this day.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    selectedDayEvents.forEach { event ->
                        EventRowItem(
                            event = event,
                            accentColor = accentColor,
                            onEdit = {
                                viewModel.selectedEvent = event
                                viewModel.eventTitleInput = event.title
                                viewModel.eventDescInput = event.description
                                viewModel.eventDateInput = event.eventDate
                                viewModel.eventColorInput = event.colorHex
                                viewModel.eventIsHolidayInput = event.isHoliday
                                showAddEventDialog = true
                            },
                            onDelete = { viewModel.deleteEvent(event.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // ADD / EDIT EVENT DIALOG MODAL
    if (showAddEventDialog) {
        val eventColors = listOf("#2196F3", "#EF4444", "#F59E0B", "#10B981", "#6366F1", "#EC4899")
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = {
                Text(
                    text = if (viewModel.selectedEvent == null) "Schedule Event" else "Edit Event",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = viewModel.eventTitleInput,
                        onValueChange = { viewModel.eventTitleInput = it },
                        label = { Text("Event Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_title_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor)
                    )

                    OutlinedTextField(
                        value = viewModel.eventDescInput,
                        onValueChange = { viewModel.eventDescInput = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor),
                        maxLines = 3
                    )

                    // HOLIDAY SWITCH
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mark as Holiday", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Switch(
                            checked = viewModel.eventIsHolidayInput,
                            onCheckedChange = { viewModel.eventIsHolidayInput = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = accentColor)
                        )
                    }

                    // COLORS
                    Text("Tag Color", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        eventColors.forEach { colorHex ->
                            val c = Color(android.graphics.Color.parseColor(colorHex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (viewModel.eventColorInput == colorHex) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.eventColorInput = colorHex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveEvent()
                        showAddEventDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier.testTag("save_event_button")
                ) {
                    Text("Save Event")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun EventRowItem(
    event: CalendarEvent,
    accentColor: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val tagColor = remember(event.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(event.colorHex))
        } catch (e: Exception) {
            accentColor
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_item_${event.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = tagColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, tagColor.copy(alpha = 0.3f))
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
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tagColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (event.isHoliday) Icons.Default.BeachAccess else Icons.Default.Event,
                        contentDescription = "Event icon",
                        tint = tagColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (event.description.isNotBlank()) {
                        Text(
                            text = event.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
