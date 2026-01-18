package com.example.android.tvz.hr.mrrise

import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.android.tvz.hr.mrrise.data.database.AlarmEntity
import com.example.android.tvz.hr.mrrise.databinding.ActivityAddAlarmBinding
import com.example.android.tvz.hr.mrrise.ui.alarms.AlarmViewModel

class AddAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlarmBinding
    private lateinit var viewModel: AlarmViewModel

    private var editingAlarmId: Int = -1
    private var isEditMode = false

    private var selectedSound: String = "DEFAULT"
    private var soundPreviewPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        setupSoundSpinner()

        editingAlarmId = intent.getIntExtra("ALARM_ID", -1)
        isEditMode = editingAlarmId != -1

        if (isEditMode) {
            loadAlarmData()
        }

        binding.btnSave.setOnClickListener {
            saveAlarm()
        }
    }

    private fun loadAlarmData() {
        viewModel.allAlarms.observe(this) { alarms ->
            val alarm = alarms.find { it.id == editingAlarmId }
            if (alarm != null) {
                populateFields(alarm)
            }
        }
    }

    private fun populateFields(alarm: AlarmEntity) {
        binding.timePicker.hour = alarm.hour
        binding.timePicker.minute = alarm.minute

        binding.etLabel.setText(alarm.label)

        binding.chipMonday.isChecked = alarm.monday
        binding.chipTuesday.isChecked = alarm.tuesday
        binding.chipWednesday.isChecked = alarm.wednesday
        binding.chipThursday.isChecked = alarm.thursday
        binding.chipFriday.isChecked = alarm.friday
        binding.chipSaturday.isChecked = alarm.saturday
        binding.chipSunday.isChecked = alarm.sunday

        when (alarm.puzzleType) {
            "SIMON_SAYS" -> binding.rbSimonSays.isChecked = true
            "MATH" -> binding.rbMath.isChecked = true
            "QR_CODE" -> binding.rbQRCode.isChecked = true
        }

        val soundKeys = listOf("DEFAULT", "VIBRATE", "RINGTONE")
        val soundPosition = soundKeys.indexOf(alarm.alarmSound).takeIf { it >= 0 } ?: 0
        binding.spinnerSound.setSelection(soundPosition)
        selectedSound = alarm.alarmSound

        binding.btnSave.text = getString(R.string.update_alarm)
    }

    private fun getSoundUri(soundType: String): android.net.Uri {
        return when (soundType) {
            "DEFAULT" -> android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            "RINGTONE" -> android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
            "VIBRATE" -> android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
            else -> android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
        } ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
    }

    private fun previewSound() {
        soundPreviewPlayer?.apply {
            try {
                if (isPlaying) stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            release()
        }
        soundPreviewPlayer = null

        if (selectedSound == "VIBRATE") {
            Toast.makeText(this, "Preview disabled for vibration", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val soundUri = getSoundUri(selectedSound)
            soundPreviewPlayer = MediaPlayer().apply {
                setDataSource(this@AddAlarmActivity, soundUri)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )

                setOnCompletionListener {
                    it.release()
                    soundPreviewPlayer = null
                }

                setOnErrorListener { mp, what, extra ->
                    mp.release()
                    soundPreviewPlayer = null
                    true
                }

                prepare()
                start()

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        if (isPlaying) {
                            stop()
                        }
                        release()
                        soundPreviewPlayer = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 3000)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not play sound", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            soundPreviewPlayer?.release()
            soundPreviewPlayer = null
        }
    }

    private val soundPickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedSound = "CUSTOM"
                val soundKeys = listOf("DEFAULT", "RINGTONE", "CUSTOM")
                binding.spinnerSound.setSelection(soundKeys.indexOf("CUSTOM"))

                Toast.makeText(this, "Custom sound selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openSoundPicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_ALL)
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        }
        soundPickerLauncher.launch(intent)
    }

    private fun setupSoundSpinner() {
        val sounds = listOf(
            getString(R.string.sound_default),
            getString(R.string.sound_vibrate),
            getString(R.string.sound_ringtone)
        )

        val soundKeys = listOf("DEFAULT", "VIBRATE", "RINGTONE")

        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, sounds)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSound.adapter = adapter

        binding.spinnerSound.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedSound = soundKeys[position]

                binding.btnPreviewSound.isEnabled = (selectedSound != "VIBRATE")
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedSound = "DEFAULT"
            }
        }

        binding.btnPreviewSound.setOnClickListener {
            previewSound()
        }
    }
    private fun saveAlarm() {
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute

        val label = binding.etLabel.text.toString().ifEmpty { "Alarm" }

        val monday = binding.chipMonday.isChecked
        val tuesday = binding.chipTuesday.isChecked
        val wednesday = binding.chipWednesday.isChecked
        val thursday = binding.chipThursday.isChecked
        val friday = binding.chipFriday.isChecked
        val saturday = binding.chipSaturday.isChecked
        val sunday = binding.chipSunday.isChecked

        val puzzleType = when (binding.rgPuzzleType.checkedRadioButtonId) {
            R.id.rbSimonSays -> "SIMON_SAYS"
            R.id.rbMath -> "MATH"
            R.id.rbQRCode -> "QR_CODE"
            else -> "SIMON_SAYS"
        }

        val alarm = AlarmEntity(
            id = if (isEditMode) editingAlarmId else 0,
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = true,
            monday = monday,
            tuesday = tuesday,
            wednesday = wednesday,
            thursday = thursday,
            friday = friday,
            saturday = saturday,
            sunday = sunday,
            puzzleType = puzzleType,
            alarmSound = selectedSound

        )
        android.util.Log.d("AddAlarmActivity", "Saving alarm with sound: $selectedSound")

        if (isEditMode) {
            viewModel.updateAlarm(alarm)
            Toast.makeText(this, getString(R.string.alarm_updated), Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertAlarm(alarm)
            Toast.makeText(this, getString(R.string.alarm_saved), Toast.LENGTH_SHORT).show()
        }

        finish()
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            soundPreviewPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        soundPreviewPlayer = null
    }

    override fun onPause() {
        super.onPause()
        try {
            soundPreviewPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            soundPreviewPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}