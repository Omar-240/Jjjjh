package com.example.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity

object AppShortcutsManager {
    fun createShortcuts(context: Context) {
        try {
            val shortcutNotes = ShortcutInfoCompat.Builder(context, "shortcut_notes")
                .setShortLabel("الملاحظات")
                .setLongLabel("فتح الملاحظات والمسودات")
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_edit))
                .setIntent(Intent(context, MainActivity::class.java).apply {
                    action = "NAVIGATE_TO_NOTES"
                    putExtra("navigate_to", "notes")
                })
                .build()

            val shortcutTasks = ShortcutInfoCompat.Builder(context, "shortcut_tasks")
                .setShortLabel("المهام اليومية")
                .setLongLabel("فتح قائمة المهام اليومية")
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_agenda))
                .setIntent(Intent(context, MainActivity::class.java).apply {
                    action = "NAVIGATE_TO_TASKS"
                    putExtra("navigate_to", "tasks")
                })
                .build()

            val shortcutExpenses = ShortcutInfoCompat.Builder(context, "shortcut_expenses")
                .setShortLabel("تتبع المصاريف")
                .setLongLabel("تتبع المصاريف والميزانية")
                .setIcon(IconCompat.createWithResource(context, android.R.drawable.ic_menu_compass))
                .setIntent(Intent(context, MainActivity::class.java).apply {
                    action = "NAVIGATE_TO_EXPENSES"
                    putExtra("navigate_to", "expenses")
                })
                .build()

            ShortcutManagerCompat.addDynamicShortcuts(context, listOf(shortcutNotes, shortcutTasks, shortcutExpenses))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
