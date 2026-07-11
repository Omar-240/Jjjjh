package com.example.data

import kotlinx.coroutines.flow.Flow

class ToolboxRepository(private val database: AppDatabase) {
    private val noteDao = database.noteDao()
    private val taskDao = database.taskDao()
    private val eventDao = database.eventDao()
    private val expenseDao = database.expenseDao()

    // Notes
    val activeNotes: Flow<List<Note>> = noteDao.getActiveNotes()
    val archivedNotes: Flow<List<Note>> = noteDao.getArchivedNotes()
    val trashNotes: Flow<List<Note>> = noteDao.getTrashNotes()

    suspend fun insertNote(note: Note) = noteDao.insert(note)
    suspend fun updateNote(note: Note) = noteDao.update(note)
    suspend fun deleteNotePermanently(id: Int) = noteDao.deletePermanently(id)
    suspend fun emptyTrashNotes() = noteDao.emptyTrash()

    // Tasks
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insertTask(task: Task) = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(id: Int) = taskDao.delete(id)

    // Events
    val allEvents: Flow<List<CalendarEvent>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: CalendarEvent) = eventDao.insert(event)
    suspend fun updateEvent(event: CalendarEvent) = eventDao.update(event)
    suspend fun deleteEvent(id: Int) = eventDao.delete(id)

    // Expenses
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(id: Int) = expenseDao.delete(id)
}
