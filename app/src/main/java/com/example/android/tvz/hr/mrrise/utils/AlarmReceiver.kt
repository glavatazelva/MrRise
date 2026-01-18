package com.example.android.tvz.hr.mrrise.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.android.tvz.hr.mrrise.ui.puzzle.AlarmRingingActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val puzzleType = intent.getStringExtra("PUZZLE_TYPE") ?: "SIMON_SAYS"
        val alarmSound = intent.getStringExtra("ALARM_SOUND") ?: "DEFAULT"

        val alarmIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            putExtra("PUZZLE_TYPE", puzzleType)
            putExtra("ALARM_SOUND", alarmSound)
        }

        context.startActivity(alarmIntent)
    }

}