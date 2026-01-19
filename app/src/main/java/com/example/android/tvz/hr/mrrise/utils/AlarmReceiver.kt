package com.example.android.tvz.hr.mrrise.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.example.android.tvz.hr.mrrise.ui.puzzle.AlarmRingingActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "===== ALARM TRIGGERED =====")

        // Acquire wake lock
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "MrRise::AlarmWakeLock"
        )
        wakeLock.acquire(60000)

        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val puzzleType = intent.getStringExtra("PUZZLE_TYPE") ?: "SIMON_SAYS"
        val alarmSound = intent.getStringExtra("ALARM_SOUND") ?: "DEFAULT"

        Log.d("AlarmReceiver", "Starting alarm activity - ID: $alarmId, Sound: $alarmSound")

        val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            putExtra("PUZZLE_TYPE", puzzleType)
            putExtra("ALARM_SOUND", alarmSound)
        }

        try {
            context.startActivity(alarmIntent)
            Log.d("AlarmReceiver", "Activity started successfully")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start activity: ${e.message}")
            e.printStackTrace()
        } finally {
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Log.d("AlarmReceiver", "Wake lock released")
                }
            }, 5000)
        }
    }
}