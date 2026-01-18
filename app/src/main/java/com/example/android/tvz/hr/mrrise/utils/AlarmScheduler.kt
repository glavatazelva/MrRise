package com.example.android.tvz.hr.mrrise.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.android.tvz.hr.mrrise.data.database.AlarmEntity
import java.util.*

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: AlarmEntity) {
        if (!alarm.isEnabled) return

        Log.d("AlarmScheduler", "Scheduling alarm: ${alarm.label} for ${alarm.hour}:${alarm.minute}")

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("PUZZLE_TYPE", alarm.puzzleType)
            putExtra("ALARM_SOUND", alarm.alarmSound)
        }

        val selectedDays = mutableListOf<Int>()
        if (alarm.sunday) selectedDays.add(Calendar.SUNDAY)
        if (alarm.monday) selectedDays.add(Calendar.MONDAY)
        if (alarm.tuesday) selectedDays.add(Calendar.TUESDAY)
        if (alarm.wednesday) selectedDays.add(Calendar.WEDNESDAY)
        if (alarm.thursday) selectedDays.add(Calendar.THURSDAY)
        if (alarm.friday) selectedDays.add(Calendar.FRIDAY)
        if (alarm.saturday) selectedDays.add(Calendar.SATURDAY)

        if (selectedDays.isEmpty()) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d("AlarmScheduler", "Scheduled one-time alarm for: ${calendar.time}")
            return
        }

        selectedDays.forEachIndexed { index, dayOfWeek ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_WEEK, dayOfWeek)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val requestCode = alarm.id * 10 + index
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d("AlarmScheduler", "Scheduled for day $dayOfWeek at: ${calendar.time}")
        }
    }

    fun cancelAlarm(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)

        for (index in 0..6) {
            val requestCode = alarm.id * 10 + index
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}