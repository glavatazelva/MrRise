package com.example.android.tvz.hr.mrrise.ui.alarms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.android.tvz.hr.mrrise.data.database.AlarmDatabase
import com.example.android.tvz.hr.mrrise.data.database.AlarmEntity
import com.example.android.tvz.hr.mrrise.data.repository.AlarmRepository
import kotlinx.coroutines.launch
import com.example.android.tvz.hr.mrrise.utils.AlarmScheduler


class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository
    val allAlarms: LiveData<List<AlarmEntity>>

    private val alarmScheduler = AlarmScheduler(application)

    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        allAlarms = repository.allAlarms
    }

    fun insertAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        val alarmId = repository.insertAlarm(alarm)

        if (alarm.isEnabled) {
            val savedAlarm = alarm.copy(id = alarmId.toInt())
            alarmScheduler.scheduleAlarm(savedAlarm)
        }
    }

    fun updateAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        repository.updateAlarm(alarm)

        if (alarm.isEnabled) {
            alarmScheduler.scheduleAlarm(alarm)
        } else {
            alarmScheduler.cancelAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        alarmScheduler.cancelAlarm(alarm)
        repository.deleteAlarm(alarm)
    }

    fun toggleAlarm(alarm: AlarmEntity) = viewModelScope.launch {
        val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
        repository.updateAlarm(updatedAlarm)

        if (updatedAlarm.isEnabled) {
            alarmScheduler.scheduleAlarm(updatedAlarm)
        } else {
            alarmScheduler.cancelAlarm(updatedAlarm)
        }
    }
}