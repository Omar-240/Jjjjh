package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Personal",
    val colorHex: String = "#FF9800", // default orange
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isTrash: Boolean = false
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val priority: String = "Medium", // Low, Medium, High
    val dueDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val eventDate: Long = System.currentTimeMillis(),
    val colorHex: String = "#2196F3", // default blue
    val isHoliday: Boolean = false,
    val isReminder: Boolean = false
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val isExpense: Boolean = true, // true = Expense, false = Income
    val category: String = "Food",
    val date: Long = System.currentTimeMillis(),
    val wallet: String = "Cash"
)
