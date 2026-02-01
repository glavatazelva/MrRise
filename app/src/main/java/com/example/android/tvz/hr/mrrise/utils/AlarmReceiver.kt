package com.example.android.tvz.hr.mrrise.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.android.tvz.hr.mrrise.R
import com.example.android.tvz.hr.mrrise.ui.puzzle.AlarmRingingActivity

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID_BASE = 1000

        //postavit globalno tako da ne postoji sansa da ga ista ugasi
        var activeSoundManager: AlarmSoundManager? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "===== ALARM TRIGGERED =====")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or
                    PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    PowerManager.ON_AFTER_RELEASE,
            "MrRise::AlarmWakeLock"
        )
        wakeLock.acquire(60000)

        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val label = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val puzzleType = intent.getStringExtra("PUZZLE_TYPE") ?: "SIMON_SAYS"
        val alarmSound = intent.getStringExtra("ALARM_SOUND") ?: "DEFAULT"

        Log.d("AlarmReceiver", "Showing full-screen notification - ID: $alarmId, Sound: $alarmSound")

        activeSoundManager = AlarmSoundManager(context.applicationContext)
        activeSoundManager?.startAlarmSound(alarmSound)

        createNotificationChannel(context)
        showFullScreenNotification(context, alarmId, label, puzzleType, alarmSound)

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (wakeLock.isHeld) {
                wakeLock.release()
                Log.d("AlarmReceiver", "Wake lock released")
            }
        }, 5000)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Notifications"
            val descriptionText = "Notifications for alarm rings"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setBypassDnd(true)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showFullScreenNotification(
        context: Context,
        alarmId: Int,
        label: String,
        puzzleType: String,
        alarmSound: String
    ) {
        val fullScreenIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION)
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", label)
            putExtra("PUZZLE_TYPE", puzzleType)
            putExtra("ALARM_SOUND", alarmSound)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(label)
            .setContentText("Alarm is ringing")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500))

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_BASE + alarmId, notificationBuilder.build())

        Log.d("AlarmReceiver", "Full-screen notification posted")
    }
}