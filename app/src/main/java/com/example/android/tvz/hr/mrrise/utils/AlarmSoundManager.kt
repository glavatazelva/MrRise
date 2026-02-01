package com.example.android.tvz.hr.mrrise.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi

class AlarmSoundManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    fun startAlarmSound(soundType: String = "DEFAULT") {
        android.util.Log.d("AlarmSoundManager", "Starting alarm with soundType: $soundType")

        if (soundType == "VIBRATE") {
            android.util.Log.d("AlarmSoundManager", "Using vibration only")
            startVibration()
        } else {
            android.util.Log.d("AlarmSoundManager", "Playing sound")
            startSound(soundType)
        }
    }

    private fun startSound(soundType: String) {
        try {
            val alarmUri: Uri? = when (soundType) {
                "RINGTONE" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alarmUri!!)

                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVibration() {
        try {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (vibrator == null || !vibrator!!.hasVibrator()) {
                return
            }

            val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000, 500, 1000, 500)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, 0),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }

        } catch (e: Exception) {
            android.util.Log.e("AlarmSoundManager", "Vibration error: ${e.message}")
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null
    }
}