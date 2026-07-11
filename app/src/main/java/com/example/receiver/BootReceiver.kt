package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed. Checking if task reminder is enabled.")
            if (TaskReminderManager.isReminderEnabled(context)) {
                TaskReminderManager.scheduleNextReminderIfEnabled(context)
            }
        }
    }
}
