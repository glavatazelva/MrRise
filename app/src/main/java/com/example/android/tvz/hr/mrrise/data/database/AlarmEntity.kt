package com.example.android.tvz.hr.mrrise.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean,


    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val sunday: Boolean,

    val puzzleType: String,
    val alarmSound: String = "DEFAULT"
)