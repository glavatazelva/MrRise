package com.example.android.tvz.hr.mrrise.ui.puzzle

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.android.tvz.hr.mrrise.R
import com.example.android.tvz.hr.mrrise.databinding.ActivityAlarmRingingBinding
import com.example.android.tvz.hr.mrrise.utils.AlarmSoundManager

import java.text.SimpleDateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AlarmRingingActivity : AppCompatActivity() {

    private var qrPuzzle: QRCodePuzzle? = null
    private lateinit var binding: ActivityAlarmRingingBinding
    private var alarmId: Int = -1

    private var puzzleType: String = "SIMON_SAYS"
    private lateinit var soundManager: AlarmSoundManager
    private var alarmSound: String = "DEFAULT"


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        alarmId = intent.getIntExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        puzzleType = intent.getStringExtra("PUZZLE_TYPE") ?: "SIMON_SAYS"
        alarmSound = intent.getStringExtra("ALARM_SOUND") ?: "DEFAULT"

        soundManager = AlarmSoundManager(this)
        soundManager.startAlarmSound(alarmSound)

        binding.tvAlarmLabel.text = alarmLabel

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        binding.tvCurrentTime.text = currentTime

        loadPuzzle()
    }
    private fun loadPuzzle() {
        when (puzzleType) {
            "SIMON_SAYS" -> {
                binding.tvPuzzleInstruction.text = getString(R.string.repeat_pattern)
                SimonSaysPuzzle(binding.puzzleContainer) {
                    dismissAlarm()
                }
            }
            "MATH" -> {
                binding.tvPuzzleInstruction.text = getString(R.string.solve_xy)
                MathPuzzle(binding.puzzleContainer) {
                    dismissAlarm()
                }
            }
            "QR_CODE" -> {
                binding.tvPuzzleInstruction.text = getString(R.string.scan_qr_dismiss)
                qrPuzzle = QRCodePuzzle(binding.puzzleContainer, this) {
                    dismissAlarm()
                }
            }
        }

    }

    fun dismissAlarm() {

        soundManager.stopAlarmSound()

        if (alarmId != -1) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val database = com.example.android.tvz.hr.mrrise.data.database.AlarmDatabase.getDatabase(this@AlarmRingingActivity)
                val alarm = database.alarmDao().getAlarmById(alarmId)

                if (alarm != null) {
                    val disabledAlarm = alarm.copy(isEnabled = false)
                    database.alarmDao().updateAlarm(disabledAlarm)

                    val scheduler = com.example.android.tvz.hr.mrrise.utils.AlarmScheduler(this@AlarmRingingActivity)
                    scheduler.cancelAlarm(disabledAlarm)
                }
            }
        }

        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == QRCodePuzzle.CAMERA_PERMISSION_CODE) {
            qrPuzzle?.onPermissionResult(
                grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            )
        }
    }
}