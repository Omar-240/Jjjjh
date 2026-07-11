package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object TaskReminderManager {
    private const val ALARM_INTERVAL_MS = 5 * 60 * 1000 // 5 minutes

    fun startReminder(context: Context) {
        val sharedPrefs = context.getSharedPreferences("smart_toolbox_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_reminder_enabled", true).apply()
        
        scheduleAlarm(context, System.currentTimeMillis() + 5000) // Start in 5 seconds first time
    }

    fun stopReminder(context: Context) {
        val sharedPrefs = context.getSharedPreferences("smart_toolbox_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_reminder_enabled", false).apply()
        
        cancelAlarm(context)
    }

    fun isReminderEnabled(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences("smart_toolbox_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("is_reminder_enabled", false)
    }

    fun scheduleAlarm(context: Context, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            200,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
        Log.d("TaskReminderManager", "Alarm scheduled for trigger at $triggerTime")
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            200,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("TaskReminderManager", "Alarm cancelled.")
    }
    
    fun scheduleNextReminderIfEnabled(context: Context) {
        if (isReminderEnabled(context)) {
            val triggerTime = System.currentTimeMillis() + ALARM_INTERVAL_MS
            scheduleAlarm(context, triggerTime)
            Log.d("TaskReminderManager", "Scheduled next reminder in 5 minutes.")
        }
    }
}
