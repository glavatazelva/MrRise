package com.example.android.tvz.hr.mrrise.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {

    @Insert
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): LiveData<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Int): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>
}