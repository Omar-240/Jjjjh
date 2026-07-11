package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TaskReminderReceiver", "Alarm received!")
        
        // Schedule the next alarm in 5 minutes to keep it repeating
        TaskReminderManager.scheduleNextReminderIfEnabled(context)
        
        // Fetch tasks and post notification in background
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val incompleteTasks = db.taskDao().getIncompleteTasksSync()
                
                val title: String
                val message: String
                
                if (incompleteTasks.isNotEmpty()) {
                    // Pick the first incomplete task
                    val task = incompleteTasks.first()
                    title = "تذكير بمهمة: ${task.title}"
                    message = if (task.description.isNotBlank()) {
                        "${task.description} (${task.priority})"
                    } else {
                        "لديك مهمة معلقة لتنجزها! الأولوية: ${task.priority}"
                    }
                } else {
                    title = "تنظيم المهام اليومية"
                    message = "كل مهامك مكتملة! أضف مهاماً جديدة لتبقى منتجاً."
                }
                
                showNotification(context, title, message)
            } catch (e: Exception) {
                Log.e("TaskReminderReceiver", "Error sending task reminder", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminder_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "تذكير المهام",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات تذكيرية متكررة للمهام المعلقة"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Intent to open the app (specifically the tasks screen)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "NAVIGATE_TO_TASKS"
            putExtra("navigate_to", "tasks")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(1001, notification)
    }
}
