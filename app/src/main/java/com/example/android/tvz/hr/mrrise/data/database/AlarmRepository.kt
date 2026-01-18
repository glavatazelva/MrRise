package com.example.android.tvz.hr.mrrise.data.repository

import androidx.lifecycle.LiveData
import com.example.android.tvz.hr.mrrise.data.database.AlarmDao
import com.example.android.tvz.hr.mrrise.data.database.AlarmEntity

class AlarmRepository(private val alarmDao: AlarmDao) {

    val allAlarms: LiveData<List<AlarmEntity>> = alarmDao.getAllAlarms()

    suspend fun insertAlarm(alarm: AlarmEntity): Long {
        return alarmDao.insertAlarm(alarm)
    }

    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.updateAlarm(alarm)
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmDao.deleteAlarm(alarm)
    }

    suspend fun getAlarmById(alarmId: Int): AlarmEntity? {
        return alarmDao.getAlarmById(alarmId)
    }

    suspend fun getEnabledAlarms(): List<AlarmEntity> {
        return alarmDao.getEnabledAlarms()
    }
}